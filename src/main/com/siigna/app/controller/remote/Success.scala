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

/**
 * A [[com.siigna.app.controller.command.Command]] sent by the remote to indicate success of a previous
 * sent Command.
 */
case class Success(command : RemoteCommand) extends RemoteCommand {

  def client = command.client

}
