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

import com.siigna.app.model.action.Action
import com.siigna.app.controller.Client

/**
 * An action meant to be sent over the network.
 *
 * @param client  The client sending the action.
 * @param action  The action to send.
 * @param undo  A boolean flag indicating if this action was done (false) or undone (true).
 */
@SerialVersionUID(413435921)
case class RemoteAction(client : Client, action : Action, undo : Boolean = false) extends RemoteCommand
