/*
 * Copyright (c) 2011. Siigna is released under the creative common license by-nc-sa. You are free
 * to Share — to copy, distribute and transmit the work,
 * to Remix — to adapt the work
 *
 * Under the following conditions:
 * Attribution —  You must attribute the work to http://siigna.com in the manner specified by the author or licensor (but not in any way that suggests that they endorse you or your use of the work).
 * Noncommercial — You may not use this work for commercial purposes.
 * Share Alike — If you alter, transform, or build upon this work, you may distribute the resulting work only under the same or similar license to this one.
 */
package com.siigna.app.view.event

/**
 * Events the modules can use to signal to each other.
 */
trait ModuleEvent extends Event

/**
 * Messages that can be sent to and from modules containing any object.
 *
 * @param message  any object that the module wishes to forward.
 */
case class Message[T](message : T) extends ModuleEvent { val symbol = 'Message }