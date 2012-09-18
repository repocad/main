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

import com.siigna.app.controller.remote.RemoteConstants.RemoteConstant
import com.siigna.app.controller.{Controller, Session}

/**
 * A RemoteCommand capable of retrieving a given attribute from the remote server.
 */
@SerialVersionUID(-348100723)
sealed case class Get(name : RemoteConstant, value : Option[Any], client : Session) extends RemoteCommand

/**
 * Companion object for the Get class.
 */
object Get {

  /**
   * Constructs a Get command and forwards it to the Controller
   * @param name  The type of the request.
   * @param value  The value, if any.
   */
  def apply(name : RemoteConstant, value : Option[Any]) {
    Controller ! ((c : Session) => Get(name, value, c))
  }

}