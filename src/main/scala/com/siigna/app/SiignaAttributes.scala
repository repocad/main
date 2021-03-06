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

package com.siigna.app

import com.siigna.util.collection.AttributesLike
import collection.mutable
import com.siigna.util.Implicits._
import java.awt.{Color, Dimension}
import concurrent._
import concurrent.ExecutionContext.Implicits.global

/**
 * <p>
 *   Attributes that can change the behaviour of Siigna such as selection distance, anti-aliasing etc.
 *   They are accessible via the [[com.siigna.app.Siigna]] object.
 * </p>
 *
 * <p>
 *   The class stores a number of pre-set values into a mutable map which can be changed actively
 *   while Siigna is running. Remember that tampering with some of the constants might not bring
 *   the desired effects. So read the documentation. But do not worry about breaking things.
 *   Siigna is a very sturdy piece of software. Of course :-)
 * </p>
 *
 * @define siignaAttributes
 * Currently the following Attributes of [[com.siigna.app.Siigna]] is defined:
 * {{{
 *  activeColor
 *    A Color indicating the default color for shapes
 *  activeLineWidth
 *    A Double indicating the default width of new shapes
 *  antiAliasing
 *    A boolean value signalling if anti-aliasing should be on for the shapes in the Model.
 *    The modules are always drawn with anti-aliasing. Defaults to true.
 *  autoScaling
 *    a Boolean specifying if the paper scale is calculated automatically or set manually.
 *  scale
 *    An Int: the scale factor of the paper, if provided - else 1 (unused if autoScaling is true)
 *  backgroundTileSize
 *    The size of the square tiles drawn behind the actual drawable canvas, given in pixels. Defaults to 12.
 *  colorBackground
 *    The background color for the drawable canvas. Defaults to #F9F9F9 in hex.
 *  colorBackgroundLight
 *    The background color for the light squares in the background checkers-pattern. Defaults to #E9E9E9 in hex.
 *  colorBackgroundDark
 *    The background color for the dark squares in the background checkers-pattern. Defaults to #DADADA in hex.
 *  colorDraw
 *    The color every shapes are drawn with by default. Defaults to #000000 (black) in hex.
 *  colorOpennessCopy
 *    The color used to signal the openness level: Copy
 *  colorOpennessOpen
 *    The color used to signal the openness level: Open
 *  colorOpennessPrint
 *    The color used to signal the openness level: Print
 *  colorSelected
 *    The color given to selected elements. Defaults to #7777FF in hex.
 *  defaultScreenSize
 *    The default size of the screen given as a [[java.awt.Dimension]]. Defaults to (600 x 400).
 *  fontGlobalFamily
 *    The default font for all text. Defaults to "Lucida Sans Typewriter".
 *  fontGlobalScale
 *    The global scale of fonts, defaults to 1. Can be set if you want to scale every text in the drawing.
 *  isLive
 *    Whether or not Siigna should broadcast to the central server. If this is set to false the changes to the
 *    drawing will not be stored! Defaults to false.
 *  printMargin
 *    The margin on pages when printing the content in Siigna, given in mm. Defaults to 13. 
 *  printFormatMin
 *    The minimum format when printing, given in mm. Defaults to the width of a standard A4-size: 210.
 *  printFormatMax
 *    The maximum format when printing, given in mm. Defaults to the height of a standard A4-size: 297.
 *  selectionDistance
 *    The distance of which single-point selection happens, given in units. The actual distance changes based on
 *    the zoom-level and this value of course. Defaults to 5.
 *  snap
 *    A boolean flag signalling whether snapping (forcing mouse-events to 'glue' to certain points) is active or not
 *  track
 *    A boolean flag signalling whether tracking (tracing liner and points on any axis) is active or not
 *  trackDistance
 *    Sensitivity of track.
 *  trackGuideColor
 *    The color to paint the track guides - the horizontal and vertical helper-lines when the user is "tasting"
 *    a point. Defaults to #00FFFF in hex.
 *  version
 *    The version string of the current running instance of Siigna.
 *  versionStability
 *    The stability of the version. Can currently be set to stable or nightly.
 *  zoomSpeed
 *    The speed with which the client zooms, given in percentages. Defaults to 0.5.
 * }}}
 */
trait SiignaAttributes extends mutable.Map[String, Any] with AttributesLike {

  /**
   * The attributes of Siigna.
   */
  def self = toMap

  // Set the values
  this("activeLineWidth")       = 0.18
  this("antiAliasing")          = true
  this("autoScaling")           = true
  this("scaling")               = 1.0
  this("backgroundTileSize")    = 12
  this("colorBackground")       = "#F9F9F9".color
  this("colorBackgroundBorder") = "#99A199".color
  this("colorBackgroundLight")  = "#E9E9E9".color
  this("colorBackgroundDark")   = "#DADADA".color
  this("colorDraw")             = "#000000".color
  this("colorHover")            = "#22FFFF".color
  this("colorOpennessCopy")     = new Color(0.25f, 0.85f, 0.25f, 0.20f)
  this("colorOpennessOpen")     = new Color(0.85f, 0.85f, 0.25f, 0.20f)
  this("colorOpennessPrivate")  = new Color(0.85f, 0.25f, 0.25f, 0.20f)
  this("colorSelected")         = "#7777FF".color
  this("defaultScreenSize")     = new Dimension(852, 480)
  this("isLive")                = true
  this("printMargin")           = 13.0
  this("printFormatMin")        = 210.0 //default paper short side length
  this("printFormatMax")        = 297.0 //default paper long side length
  this("selectionDistance")     = 7.0
  this("snap")                  = true
  this("tooltips")              = true
  this("track")                 = true
  this("trackDistance")         = 4.0
  this("trackGuideColor")       = "#00FFFF".color
  this("version")               = "beta - Xenophanes"
  this("zoomSpeed")             = 0.5

  future {
    this("fontMiso") = getClass.getResource("/miso-light.otf").toString
  }

  /**
   * Examines whether snapping is enabled.
   * @see [[com.siigna.app.SiignaAttributes.snapToggle]]
   * @return  True if snapping is on, false otherwise.
   */
  def isSnapEnabled : Boolean = boolean("snap").getOrElse(true)

  /**
   * Examines whether tracking is enabled.
   * @see [[com.siigna.app.SiignaAttributes.trackToggle]]
   * @return  True if tracking is on, false otherwise.
   */
  def isTrackEnabled: Boolean = boolean("track").getOrElse(true)

  /**
   * Examines whether tooltops are enabled.
   * @see [[com.siigna.app.SiignaAttributes.tooltipToggle]]
   * @return  True if tooltips are on, false otherwise.
   */
  def areTooltipsEnabled: Boolean = boolean("tooltips").getOrElse(true)

  /**
   * Toggles a boolean value or sets it to true if it does not exist. If there already is a
   * non-boolean value assigned to that name, nothing happens.
   */
  def toggle(key : String) {
    val bool = boolean(key)
    if (bool.isDefined) {
      update(key, !bool.get)
    } else if (!isDefinedAt(key)) {
      this.+(key -> true)
    }
  }

  /**
   * Toggles snapping on or off and returns the new value.
   * @return True if snap was disabled before but activated now, false otherwise.
   */
  def snapToggle : Boolean = {
    val value = boolean("snap") match {
      case Some(b) => !b
      case _ => true
    }
    update("snap", value)
    value
  }

  /**
   * Toggles tooltips on or off and returns the new value.
   * @return True if tooltips were disabled before but activated now, false otherwise.
   */
  def tooltipToggle : Boolean = {
    val value = boolean("tooltips") match {
      case Some(b) => !b
      case _ => true
    }
    update("tooltips", value)
    value
  }

  /**
   * Toggles tracking on or off and returns the new value.
   * @return True if track was disabled before but activated now, false otherwise.
   */
  def trackToggle : Boolean = {
    val value = boolean("track") match {
      case Some(b) => !b
      case _ => true
    }
    update("track", value)
    value
  }

}
