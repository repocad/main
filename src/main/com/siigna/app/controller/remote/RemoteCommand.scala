/*
 * Copyright (c) 2008-2013. Siigna is released under the creative common license by-nc-sa. You are free
 * to Share — to copy, distribute and transmit the work,
 * to Remix — to adapt the work
 *
 * Under the following conditions:
 * Attribution —  You must attribute the work to http://siigna.com in the manner specified by the author or licensor (but not in any way that suggests that they endorse you or your use of the work).
 * Noncommercial — You may not use this work for commercial purposes.
 * Share Alike — If you alter, transform, or build upon this work, you may distribute the resulting work only under the same or similar license to this one.
 */

package com.siigna.app.controller.remote

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

import com.siigna.app.controller.remote.RemoteConstants._

/**
 * A [[com.siigna.app.controller.command.Command]] that can be sent over the network to the Siigna Universe,
 * containing a Session which tells the server which user that sends the command and on what drawing.
 */
trait RemoteCommand extends Serializable {

  /**
   * The session who are sending this command.
   * @return The client associated with the command.
   */
  def session : Session
  
}

/**
 * A RemoteCommand capable of retrieving a given attribute from the remote server.
 * @param name  The name of the value to get
 * @param value  A parameter to send with the request
 * @param session  A session to authenticate the request
 */
@SerialVersionUID(2134932846)
sealed case class Get(name : RemoteConstant, value : Array[Byte], session : Session) extends RemoteCommand

/**
 * A RemoteCommand signalling that some error occurred on the server side.
 * @param code  The HTTP code of the error
 * @param message  The message from the server
 * @param session  A session to authenticate the request
 */
@SerialVersionUID(-1573685469)
sealed case class Error(code : Int, message : String, session : Session) extends RemoteCommand

/**
 * A RemoteCommand capable of setting a given attribute to a given value.
 * @param name  The name of the value to set
 * @param value  A parameter value to send with the request
 * @param session  A session to authenticate the request
 */
@SerialVersionUID(1178866764)
sealed case class Set(name : RemoteConstant, value : Array[Byte], session : Session) extends RemoteCommand

