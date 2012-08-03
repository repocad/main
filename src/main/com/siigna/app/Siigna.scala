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
package com.siigna.app

import com.siigna.app.controller.{Controller, CommandController, Client}
import com.siigna.app.model.server.User
import model._
import view._
import com.siigna.util.geom._
import event.Track
import collection.mutable.HashMap
import java.awt.{Dimension, Cursor}

/**
 * <p>The Siigna object provides access to various core elements of the software.</p>
 *
 * <p>It also functions as the first <code>Interface</code> connected
 * to the [[com.siigna.app.view.View]].</p>
 *
 * <p>See {http://github.com/siigna/main/wiki} for more information</p>
 */
object Siigna extends HashMap[String, Any] with Interface with SiignaAttributes {

  /**
   * The active display, if any.
   */
  protected var display : Option[Display] = None

  /**
   * The active ModuleInterface.
   */
  private var interface : Option[ModuleInterface] = None

  /**
   * The current position of the mouse. This does not take snap and track into account. Use
   * [[com.siigna.module.Module.mousePosition]] if you are searching for the mouse coordinate
   * used by the given Module.
   */
  var mousePosition = Vector2D(0, 0)

  /**
   * If navigation is turned off the canvas of Siigna stops moving. In other words the
   * <code>Model</code> remains where it is while the modules still receives the events and
   * are still able to react.
   */
  var navigation = true

  /**
   * The current user logged in to Siigna, if any.
   */
   //var user : Option[User] = None

   var user : Option[User] = Some(new User("csp"))

  /**
   * The version of Siigna.
   */
  val version = "v1.0 beta"

  /**
   * Clears the display. NOT the interface. The interface can only be cleared by
   * terminating the module. ModuleInterfaces can be <code>reset()</code> though.
   */
  def clearDisplay() { display = None }

  /**
   * Saves a given display.
   */
  def display(display : Display) { this.display = Some(display) }
  
  /**
   * Saves a given display.
   */
  def display(display : Option[Display]) { this.display = display }

  /**
   * A shorthand reference to display a popup.
   */
  def display(string : String) { display(new Popup(string)) }

  /**
   * Returns the active ModuleInterface that's been placed highest in the interface-hierarchy.
   * This is the interface Siigna calls to paint on first. If any interfaces lies before in the
   * module-chain (i. e. if the active interface doesn't belong to the Default module), they won't
   * get painted.
   */
  def getInterface = interface

  /**
   * Examines whether the user has been logged in from the Siigna website.
   * @return True if the user is logged in, false otherwise.
   */
  def isLoggedIn = user.isDefined

  /**
   * Examines whether this client is connected with the server.
   * @return True if the connection has been established correctly, false otherwise.
   */
  def isOnline = Controller.isOnline

  /**
   * The entrance to the paint-functions of the interfaces, i. e. the modules, and the
   * [[com.siigna.app.view.Display]]. For the modules the matrix is forwarded in case the module
   * needs to use/reverse some of the transformations that already have been applied to the view.
   * <br />
   * The painting eludes the normal event-based thread communication, since the <code>Controller<code>
   * mechanism runs on it's own thread and we'd like to make sure that the painting happens instantly.
   */
  def paint(graphics : Graphics, transformation : TransformationMatrix) {
    // Paint the tracking - if needed
    Track.paint(graphics, transformation)

    // Paint the interface
    if (interface.isDefined) interface.get.paint(graphics, transformation)

    // Paint the display or remove it if needed
    if (display.isDefined && display.get.isEnabled)
      display.get paint graphics
    else
      display = None
  }

  /**
   * Returns the paper scale of the current model.
   */
  def paperScale : Int = Drawing.boundaryScale

  /**
   * The selection-distance.
   */
  def selectionDistance : Double = int("selectionDistance").getOrElse(1) * View.zoom

  /**
   * Set's the current cursor of Siigna. Overrides the current value.
   */
  def setCursor(cursor : Cursor) {
    View setCursor cursor
  }

  /**
   * Sets the currently active ModuleInterface.
   * This is the interface Siigna calls to paint on first. If any interfaces lies before in the
   * module-chain (i. e. if the active interface doesn't belong to the Default module), they won't
   * get painted.
   * <br />
   * In other words don't meddle with this if you don't know what you're doing.
   */
  def setInterface(interface : ModuleInterface) {
    this.interface = Some(interface)

    setCursor(interface.getCursor)
  }

}