/*
 * Copyright (c) 2008-2013, Selftie Software. Siigna is released under the
 * creative common license by-nc-sa. You are free
 *   to Share — to copy, distribute and transmit the work,
 *   to Remix — to adapt the work
 *
 * Under the following conditions:
 *   Attribution —   You must attribute the work to http://siigna.com in
 *                    the manner specified by the author or licensor (but
 *                    not in any way that suggests that they endorse you
 *                    or your use of the work).
 *   Noncommercial — You may not use this work for commercial purposes.
 *   Share Alike   — If you alter, transform, or build upon this work, you
 *                    may distribute the resulting work only under the
 *                    same or similar license to this one.
 *
 * Read more at http://siigna.com and https://github.com/siigna/main
 */

package com.siigna.util.event

import java.awt.event.{KeyEvent => AWTKey}

/**
 * KeyEvents.
 */
trait KeyEvent extends Event
{

  /**
   * The character associated with a given KeyEvent.
   */
  def code : Int

  /**
   * The ModifierKeys for a given event.
   */
  def keys : ModifierKeys

}

/**
 * An event that triggers when any key is pressed.
 */
case class KeyDown(code : Int, keys : ModifierKeys) extends KeyEvent { val symbol = 'KeyDown }

/**
 * An event that triggers when any key is released.
 */
case class KeyUp  (code : Int, keys : ModifierKeys) extends KeyEvent { val symbol = 'KeyUp   }

/**
 * Shortcuts to AWT's <code>KeyEvent</code> constants. Useful for matching on
 * KeyEvents.
 */
case object Key {
  val Alt, alt                        = AWTKey.VK_ALT
  val AltGR, AltGr, altgr             = AWTKey.VK_ALT_GRAPH
  val ArrowDown, arrowDown            = AWTKey.VK_DOWN
  val ArrowLeft, arrowLeft            = AWTKey.VK_LEFT
  val ArrowRight, arrowRight          = AWTKey.VK_RIGHT
  val ArrowUp, arrowUp                = AWTKey.VK_UP
  val BackSpace, Backspace, backspace = AWTKey.VK_BACK_SPACE
  val Control, control                = AWTKey.VK_CONTROL
  val Delete, delete                  = AWTKey.VK_DELETE
  val Enter, enter                    = AWTKey.VK_ENTER
  val Escape, Esc, escape, esc        = AWTKey.VK_ESCAPE
  val Minus, minus, min               = AWTKey.VK_MINUS
  val Plus, plus                      = AWTKey.VK_PLUS
  val Shift, shift                    = AWTKey.VK_SHIFT
  val Space, space                    = AWTKey.VK_SPACE
  val Tabulator, Tab, tab, tabulator  = AWTKey.VK_TAB
}