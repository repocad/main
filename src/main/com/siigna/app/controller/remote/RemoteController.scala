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

import actors.remote.RemoteActor._
import actors.remote.{Node, RemoteActor}
import com.siigna.app.controller.{Session}
import com.siigna.app.Siigna
import com.siigna.util.logging.Log
import actors.Actor
import collection.mutable.BitSet
import RemoteConstants._

/**
 * Controls any remote connection(s).
 * If the client is not online or no connection could be made we simply wait until a connection can be
 * re-established before pushing all the received events/requests in the given order.
 */
protected[controller] object RemoteController extends Actor {

  // Set remote class loader
  RemoteActor.classLoader = getClass.getClassLoader

  // Start the actor
  start()

  // The session for this client.
  protected var session : Session = try {
    Session(SiignaDrawing.attributes.long("id").get, Siigna.user)
  } catch {
    case _ => throw new ExceptionInInitializerError("No id found for the drawing.")
  }

  // A boolean flag to indicate if this controller has been successfully registered with the server.
  protected var isConnected = false

  // All the ids of the actions that have been executed on the client
  protected val localActions = BitSet()

  // A map of local ids mapped to their remote counterparts
  protected var localIdMap : Map[Int, Int] = Map()

  // Ping-time in ms
  var pingTime = 2000

  // The remote server
  protected var remote = select(Node("62.243.118.234", 20004), 'siigna)
  // protected val remote = select(Node("localhost", 20004), 'siigna)

  // Timeout to the server
  var timeout = 1000

  val SiignaDrawing = com.siigna.app.model.Drawing // Use the right namespace
  // The local sink, receiving actions from the remote sink
  //SiignaDrawing.setAttribute("id",11L)

  /**
   * The acting part of the RemoteController.
   */
  def act() {
    // The time of the most recent ping
    var lastPing = System.currentTimeMillis()
    
    loop {
      
      if (System.currentTimeMillis() > lastPing + pingTime) {
        // Any outstanding actions?
        if (isOutstandingAction) {
          // TODO
        }

        synchronous(Get(ActionId, None, session), _ match {
          case Set(ActionId, id : Int, _) => {
  
          }
        })
        lastPing = System.currentTimeMillis()
      }
      
      react {
        // Set an action to the server
        case local : RemoteAction => {
          synchronous(Set(Action, local, session), _ match {
            case Error(code, message, _) => {
              Log.error("Remote: Error when sending action - retrying: " + message)
              // TODO: Correctly handle errors
            }
            case Set(ActionId, id : Int, _) => {
              localActions + id;
              Log.success("Remote: Received and updated action id")
            }
          })
        }

        // Execute other commands (or not?)
        case command : RemoteCommand => {
          synchronous(command, _ match {
            case Error(code, message, _) => {
              Log.error("Remote error Code " + code + ": " + message)
            }
            case any => Log.error("Remote: Received unknown value from server: " + any)
          })
        }
        
        /*case Set(typ, value, _) => {
          Log.info("Remote: Received Set[" + typ + " -> " + value + "]")
          typ match {
            case ActionId => {
              localActions += value.get.asInstanceOf[Int]
            }
            case DrawingId => {
              SiignaDrawing.addAttribute("id", value.get)
            }
            case Drawing => {
              //SiignaDrawing.shapes = value.getAsInstanceOf[RemoteModel].shapes
              value.get match {
                case rem: RemoteModel => {

                  val baos = new ByteArrayOutputStream()
                  val oos = new ObjectOutputStream(baos)

                  rem.writeExternal(oos)
                  val modelData = baos.toByteArray

                  val drawData = new ObjectInputStream(new ByteArrayInputStream(modelData))

                  SiignaDrawing.readExternal(drawData)
                }
                case e => Log.error("Remote: Got a weird drawing "+e)
              }
            }

            case _ =>
          }
        }*/

        case message => {
          Log.debug("Remote: Unable to handle local input: " + message)
        }
      }
    }
  }

  /**
   * Dequeues the enqueued commands in the queue.
   * If the action is local (see [[com.siigna.app.model.action.Action.isLocal]])
   * we query the server for ids so we can be certain everything is synchronized with the server.
   * param client  The client to authorize the commands.
   */
  /*protected def dequeue(client : Session) { if (!queue.isEmpty ) {
    Log.debug("Remote: Sending queue of size: " + queue.size)

    // Send and dequeue the enqueued messages
    queue = queue.foldLeft(queue)((q : Seq[Session => RemoteCommand], f : (Session => RemoteCommand)) => {

      // Retrieve the command
      val command : RemoteCommand = f(client) match {
        case remoteAction : RemoteAction => {
          val action = remoteAction.action

          // Query for remote ids if the action is local
          if (action.isLocal) {
            val localIds = action.ids.filter(_ < 0).toSeq

            // Map the ids with existing key-pairs
            val ids = localIds.map(i => localIdMap.getOrElse(i, i))

            // Do we still have local ids?
            val updatedAction = if (ids.exists(_ < 0)) {
              // Find the local ids
              val localIds = ids.filter(_ < 0)

              // .. Then we need to query the server for ids
              remote !? Get(ShapeIdentifier, Some(localIds.size), client) match {
                case Set(ShapeIdentifier, Some(i : Range), _) => {

                  // Find out how the ids map to the action
                  val map = for (n <- 0 until localIds.size) yield localIds(n) -> i(n)

                  // Update the map in the remote controller
                  localIdMap ++= map

                  // Update the model
                  SiignaDrawing.execute(UpdateLocalActions(localIdMap))

                  // Return the updated action
                  action.update(localIdMap)
                }
                case e => throw new UnknownError("Remote: Expected Set(ShapeIdentifier, _, _), got " + e)
              }
            } else { // Else give the action the new ids
              action.update(localIds.zip(ids).toMap)
            }

            // Dispatch the remote command
            RemoteAction(client, updatedAction, remoteAction.undo)
          } else { // Else simply just dispatch
            remoteAction
          }
        }
        case cmd => cmd // Just return
      }

      // Send it to the server
      remote.send(command, local)
      q.tail
    })
  } }*/

  /**
   * Retrieves actions that are missing in the continuum between the index of the first received action
   * to the given index - i. e. if we are missing any actions between the first action the client has
   * received and the last action. If that is the case we need to fetch these actions to make sure the
   * client has a coherent model.
   * @param last  The index of the last action, can be defined if an index is received from the server. Defaults to localActions.last
   * @return A BitSet containing the outstanding/missing actions.
   */
  protected def getOutstandingActions(last : Int = localActions.last) = {
    val xs = BitSet()
    for (i <- localActions.head until last) {
      // Add it to the set if it is not found
      if (!localActions(i)) xs + i
    }
    xs // Return
  }

  /**
   * Examines if there are any outstanding actions to be handled. False is good.
   * @return False is none could be found, true otherwise
   */
  protected def isOutstandingAction : Boolean = {
    if (!localActions.isEmpty) {
      for (i <- localActions.head until localActions.last) {
        // Return true (and break the loop) if it is not the last action and the number is not found
        if (!localActions(i)) return true
      }
    }
    false
  }

  /**
   * Defines whether the client is connected to a remote server or not.
   * @return true if connected, false if not.
   */
  def isOnline = isConnected

  /**
   * A method that sends a remote command synchronously with an associated callback function
   * with side effects. The method repeats the procedure until something is received.
   * @param message  The message to send
   * @param f  The callback function to execute when data is successfully retrieved
   */
  protected def synchronous(message : RemoteCommand, f : Any => Unit) {
    remote.!?(timeout, message) match {
      case Some(data) => { // Call the callback function
        try { f(data) } catch {
          case e => Log.error("Remote: Error when treating remote data: " + e )
        }
      }
      case None      => synchronous(message, f) // Retry
    }
  }

}
