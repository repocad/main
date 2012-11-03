/*
 * Copyright (c) 2012. Siigna is released under the creative common license by-nc-sa. You are free
 * to Share — to copy, distribute and transmit the work,
 * to Remix — to adapt the work
 *
 * Under the following conditions:
 * Attribution —  You must attribute the work to http://siigna.com in the manner specified by the author or licensor (but not in any way that suggests that they endorse you or your use of the work).
 * Noncommercial — You may not use this work for commercial purposes.
 * Share Alike — If you alter, transform, or build upon this work, you may distribute the resulting work only under the same or similar license to this one.
 */

package com.siigna.app.controller.remote

import actors.remote.RemoteActor
import com.siigna.app.Siigna
import com.siigna.util.logging.Log
import actors.{TIMEOUT, DaemonActor, Actor}
import collection.mutable.BitSet
import RemoteConstants._
import com.siigna.app.model.action.{RemoteAction, LoadDrawing, Action}
import com.siigna.app.controller.remote.RemoteConstants.Action
import com.siigna.util.Serializer

/**
 * Controls any remote connection(s).
 * If the client is not online or no connection could be made we simply wait until a connection can be
 * re-established before pushing all the received events/requests in the given order.
 */
protected[controller] object RemoteController extends DaemonActor {

  // Set remote class loader
  //RemoteActor.classLoader = getClass.getClassLoader

  // All the ids of the actions that have been executed on the client
  protected val actionIndices = BitSet()

  // A map of local ids mapped to their remote counterparts
  protected var localIdMap : Map[Int, Int] = Map()

  // Ping-time in ms
  var pingTime = 2000

  // Timeout to the server
  var timeout = 4000

  // The remote server
  val remote = new Server("62.243.118.234", Mode.Production)
  // val remote = select(Node("localhost", 20004), 'siigna)

  val SiignaDrawing = com.siigna.app.model.Drawing // Use the right namespace

  /**
   * The acting part of the RemoteController.
   */
  def act() {
    // The time of the most recent ping
    var lastPing = System.currentTimeMillis()

    try {
      // First of all fetch the current drawing
      remote(Get(Drawing, SiignaDrawing.attributes.long("id"), session), handleGetDrawing)

      loop {

        // Query for new actions
        remote(Get(ActionId, null, session), handleGetActionId)

        reactWithin(pingTime) {
          // Set an action to the server
          case (action : Action, undo : Boolean) => {
            // Parse the local action to ensure all the ids are up to date
            val updatedAction = parseLocalAction(action, undo)

            // Dispatch the updated action
            remote(Set(Action, updatedAction, session), handleSetAction)
          }

          // Timeout
          case TIMEOUT =>

          // We can't handle any other commands actively...
          case message => {
            Log.warning("Remote: Unknown input '" + message + "', expected a remote action.")
          }
        }
      }
    } catch {
      case e : Error => Log.error("Remote: Error, shutting down.", e)
    }
  }

  /**
   * Defines whether the client is connected to a remote server or not.
   * @return true if connected, false if not.
   */
  def isOnline = remote.isConnected

  /**
   * Handles requests for action ids. These requests are performed once in a while to make sure the client
   * has received the latest actions from the server.
   * @param any  The result of the request.
   */
  protected def handleGetActionId(any : Any) {
    any match {
      case Set(ActionId, id : Int, _) => {
        // Store the id if it's the first we get
        if (actionIndices.isEmpty) {
          actionIndices += id
        // If the id is above the action indices then we have a gap to fill!
        } else if(id > actionIndices.last) {
          for (i <- actionIndices.last + 1 to id) { // Fetch actions one by one TODO: Implement Get(Actions, _, _)
            remote(Get(Action, Some(i), session), _ match {
              case Set(Action, array : Array[Byte], _) => {
                try {
                  val action = Serializer.readAction(array)
                  action.undo match {
                    case true  => SiignaDrawing.undo(action.action, false)
                    case false => SiignaDrawing.execute(action.action, false)
                  }
                  actionIndices + i
                } catch {
                  case e => Log.error("Remote: Error when reading data from server", e)
                }
              }
              case e : Error => Log.error("Remote: Unexpected format: " + e)
            })
          }
        }

        // After the check it should be fine to add the index to the set of action indices
        actionIndices + id
      }
      case e => {
        throw new IllegalArgumentException("Remote: Error when updating ActionId: Expected Set(ActionId, Int, _), got: " + any)
      }
    }
  }

  /**
   * Handles requests to set an action, initiated by the client. These requests store the actions made by the
   * clients on the server.
   * @param any  The data received from the server
   */
  protected def handleSetAction(any : Any) {
    any match {
      case Error(code, message, _) => {
        Log.error("Remote: Error when sending action - retrying: " + message)
        // TODO: Correctly handle errors
      }
      case Set(ActionId, id : Int, _) => {
        actionIndices + id
        Log.success("Remote: Received and updated action id")
      }
    }
  }

  /**
   * Handles the request for a drawing whose id is specified in the <code>session</code> of this client.
   */
  protected def handleGetDrawing(any : Any) {
    any match {
      case Set(Drawing, bytes : Array[Byte], _) => {
        // Read the bytes
        try {
          val model = Serializer.readDrawing(bytes)

          // Implement the model
          SiignaDrawing.execute(LoadDrawing(model), false)
          actionIndices + model.attributes.int("lastAction").getOrElse(0)
          Log.success("Remote: Successfully received drawing from server")
        } catch {
          case e => Log.error("Remote: Error when reading data from server", e)
        }
      }
    }
  }

  /**
   * Parses a given local action to a remote action by checking if there are any local ids that we need
   * to update. If so the necessary requests are made to the server and the local model is updated
   * with the new ids. In case of irreversible errors we throw an UnknownException. You are warned.
   *
   * @throws UnknownError  If the server returned something illegible
   * @return A RemoteAction with updated ids, if any.
   */
  protected def parseLocalAction(action : Action, undo : Boolean) : RemoteAction = {
    // Parse the action to an updated version
    val updated : Action = if (action.isLocal) {
      val localIds = action.ids.filter(_ < 0).toSeq

      // Map the ids with existing key-pairs
      val ids = localIds.map(i => localIdMap.getOrElse(i, i))

      // Do we still have local ids?
      if (ids.exists(_ < 0)) {
        // Find the local ids
        val localIds = ids.filter(_ < 0)

        def handleGetShapeId(any : Any) = {
          any match {
            case Set(ShapeId, i : Range, _) => {

              // Find out how the ids map to the action
              val map = for (n <- 0 until localIds.size) yield localIds(n) -> i(n)

              // Update the map in the remote controller
              localIdMap ++= map

              // Update the model
              SiignaDrawing.execute(UpdateLocalActions(localIdMap), false)

              // Return the updated action
              action.update(localIdMap)
            }
            case e => {
              throw new UnknownError("Remote: Expected Set(ShapeId, Range, _), got: " + e)
            }
          }
        }

        // .. Then we need to query the server for ids
        val result = remote[Action](Get(ShapeId, localIds.size, session), handleGetShapeId)

        if (result.isRight) result.right.get
        else throw new UnknownError("Remote: Error when retrieving action ids: " + result.left.get)
      } else { // Else give the action the new ids
        action.update(localIds.zip(ids).toMap)
      }
    } else { // No local ids
      action
    }

    // Return the updated action as a remote action
    RemoteAction(updated, undo)
  }

  /**
   * Attempts to fetch the session for the current client.
   * @return A session if possible.
   */
  protected def session : Session = try {
    Session(SiignaDrawing.attributes.long("id").get, Siigna.user)
  } catch {
    case _ => {
      Log.error("Remote: Cannot find a drawing id which is necessary to make a connection; shutting down.")
      exit("Remote: Cannot connect to server without a drawing id.")
      throw new ExceptionInInitializerError("Remote: No id found for the current drawing.")
    }
  }

}
