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

import com.siigna.app.view.{View, Graphics}
import com.siigna.util.geom.{Line2D, Vector2D, TransformationMatrix}
import com.siigna.util.collection.Attributes
import com.siigna.app.model.Drawing
import com.siigna.app.Siigna
import scala.Some
import com.siigna.app.model.shape.{LineShape, Shape}
import java.awt.Color

object Track extends EventTrack {

  //the active guide, if any. (Used in shap)
  var trackGuide : Option[Line2D] = None

  //evaluate if the shape exists (used to clear the track points if the shape is deleted):
  protected var activeShape : Map[Int, Shape] = Map()

  protected var counter = 0

  //A flag to see if horizontal or vertical guides are active:
  protected var horizontalGuideActive: Boolean = false
  protected var verticalGuideActive: Boolean = false
  protected var trackedPoint: Option[Vector2D] = None

  // Get the track color
  // Must be lazy to avoid instantiation errors!
  protected lazy val color = "Color" -> Siigna.color("trackGuideColor").getOrElse(new Color(0, 255, 255))

  // Define the attributes of the track lines
  // Must be lazy to avoid instantiation errors!
  protected lazy val attributes = Attributes("Infinite" -> true, color)

  //a placeholder for shapes not yet in the model.
  //TODO: hack because the TrackModel is reset constantly.
  protected var trackShapes = Traversable[Shape]()

  // Private value to register any current trackings
  private var _isTracking: Boolean = false

  def isTracking : Boolean = _isTracking

  // Code to get the horizontal guide from a point
  def horizontalGuide(p : Vector2D) : Line2D = Line2D(p, Vector2D(p.x + 1, p.y))

  // Code to get the vertical guide from a point
  def verticalGuide(p : Vector2D) : Line2D = Line2D(p, Vector2D(p.x, p.y + 1))

  // Points to track from
  var pointOne : Option[Vector2D] = None
  var pointTwo : Option[Vector2D] = None

  /**
   * Find a point from a distance, assuming there's a track active.  
   * @param dist The distance to go in the line of the track.
   * @return  Some[Vector2D] if track is active, otherwise None.
   */
  def getPointFromDistance(dist : Double) : Option[Vector2D] = {

    /** Get the best fitting line (horizontal or vertical)
      * @return A line and a boolean indicating if the line is horizonal (false) or vertical (true)
      */
    def getTrackPoint(p : Vector2D) : Vector2D = {
      val horiz = horizontalGuide(p)
      val vert  = verticalGuide(p)

      // Horizontal is closest to mouse position:
      if (horiz.distanceTo(View.mousePositionDrawing) < vert.distanceTo(View.mousePositionDrawing)) {
        //The point on the horizontal line, that is closest to the mouse position:
        val closestPoint = horiz.closestPoint(View.mousePositionDrawing)
        if (closestPoint.x < p.x) Vector2D(p.x - dist, p.y)
        else                      Vector2D(p.x + dist, p.y)
        // Vertical is closest to mouse position:
      } else {
        //The point on the vertical line, that is closest to the mouse position:
        val closestPoint = vert.closestPoint(View.mousePositionDrawing)
        if (closestPoint.y < p.y) Vector2D(p.x, p.y - dist)
        else                      Vector2D(p.x, p.y + dist)
      }
    }

    if (pointOne.isDefined) {
      Some(getTrackPoint(pointOne.get))
    } else None
  }

  // Track on the basis of a maximum of two tracking points.
  def parse(events : List[Event], shapes : Traversable[Shape], points : Traversable[Vector2D]) : Event = {
    if(Siigna.isTrackEnabled) {

      // Set isTracking to false
      _isTracking = false

      // Get mouse event
      // The events has been unchecked since this match cannot occur if the event-list is empty
      val (_, eventFunction : (Vector2D => Event)) = (events : @unchecked) match {
        case MouseEnter(p, a, b) :: tail => (p, (v : Vector2D) => MouseEnter(v, a, b))
        case MouseExit (p, a, b) :: tail => (p, (v : Vector2D) => MouseExit(v, a, b))
        case MouseMove (p, a, b) :: tail => (p, (v : Vector2D) => MouseMove(v, a, b))
        case MouseDrag (p, a, b) :: tail => (p, (v : Vector2D) => MouseDrag(v, a, b))
        case MouseDown (p, a, b) :: tail => (p, (v : Vector2D) => MouseDown(v, a, b))
        case MouseUp   (p, a, b) :: tail => (p, (v : Vector2D) => MouseUp  (v, a, b))
        case e :: tail => (View.mousePositionScreen, (v : Vector2D) => e)
      }

      val m : Vector2D = View.mousePositionDrawing

      // Only compute the nearest point if we have one.
      if (shapes.nonEmpty || points.nonEmpty) {

        //the following evaluations are required if Siigna should track to mid and int points for shapes in the making.
        val in = IntersectionPointSnap.closestInt //using the evaluation going on in IntSnap to see if we should track...
        val int = if(in.isDefined && in.get.distanceTo(m) < Siigna.selectionDistance) Some(in.get) else None

        val mi = MidPointSnap.closestMid //using the evaluation going on in MidPointSnap to see if we should track...
        val mid = if(mi.isDefined && mi.get.distanceTo(m) < Siigna.selectionDistance) Some(mi.get) else None

        //get the possible tracking points.
        val nearestPoint = {

          if(int.isDefined)  int.get //intersections if any
          else if(mid.isDefined) mid.get //midPoints if any

          //End-points (or other shape-handles) if any
          else if (shapes.nonEmpty && !int.isDefined && !mid.isDefined) {
            val nearest = shapes.reduceLeft((a, b) => if (a.geometry.distanceTo(m) < b.geometry.distanceTo(m)) a else b)
            (nearest.geometry.vertices ++ points).reduceLeft((a : Vector2D, b : Vector2D) => if (a.distanceTo(m) < b.distanceTo(m)) a else b)
          }
          else if(shapes.nonEmpty && int.isDefined || mid.isDefined) {
            if(int.isDefined)  int.get //intersections if any
            else if(mid.isDefined) mid.get //midPoints if any
            else m
          }

          else {
            // Locate the nearest point
            points.reduceLeft((a : Vector2D, b : Vector2D) => if (a.distanceTo(m) < b.distanceTo(m)) a else b)
          }
        }

        //if a tracking point is defined, and the mouse is placed on top of a second point
        if (pointOne.isDefined) {
          if (nearestPoint.distanceTo(m) < Siigna.trackDistance) {
            if  (!(pointOne.get.distanceTo(m) < Siigna.trackDistance)) pointTwo = pointOne
            pointOne = Some(nearestPoint)
          }
        } else {
          //if no tracking point is defined, set the first point.
          val i = if (nearestPoint.distanceTo(m) < Siigna.trackDistance) Some(nearestPoint) else None
          pointOne = i
        }
      }
      //**** evaluate if the shape exists (used to clear the track points if the shape is deleted:
      if(pointOne.isDefined) {
        if(!shapes.isEmpty) {
          counter = 0
          trackShapes = shapes
        } else counter = counter + 1 //TODO: OUUUH. OUCH! Veery ugly hack to prevent guides from resetting..

        activeShape = Drawing(pointOne.get,1)
        if(activeShape == Map() && counter > 6) {
          pointOne = None
          if(pointTwo.isDefined) pointTwo = None
        }
      }

      //Snap the event
      val mousePosition = (pointOne :: pointTwo :: Nil).foldLeft(m)((p : Vector2D, opt : Option[Vector2D]) => {
        opt match {
          case Some(snapPoint : Vector2D) => {
            val horizontal = horizontalGuide(snapPoint)
            val vertical = verticalGuide(snapPoint)
            val distHori = horizontal.distanceTo(p)
            val distVert = vertical.distanceTo(p)

            if (distHori <= distVert && distHori < Siigna.trackDistance) {
              _isTracking = true
              trackGuide = Some(horizontal)
              horizontal.closestPoint(p)

            } else if (distVert < distHori && distVert < Siigna.trackDistance) {
              _isTracking = true
              trackGuide = Some(vertical)
              vertical.closestPoint(p)
            } else {
              trackGuide = None
              p
            }
          }
          case None => {
            trackGuide = None
            p
          }
        }
      })

      // Return snapped coordinate
      eventFunction(mousePosition.transform(View.drawingTransformation))
    } else events.head
  }

  override def paint(g : Graphics, t : TransformationMatrix) {
    def paintOnePoint(p : Vector2D) {
      val horizontal = horizontalGuide(p)
      val vertical   = verticalGuide(p)
      val m = View.mousePositionDrawing

      //draw the vertical tracking guide
      if (vertical.distanceTo(m) < Siigna.trackDistance){
        g draw LineShape(vertical.p1, vertical.p2, attributes).transform(t)
      }

      //draw the horizontal tracking guide
      if (horizontal.distanceTo(m) < Siigna.trackDistance)
        g draw LineShape(horizontal.p1, horizontal.p2, attributes).transform(t)
      }

    def paintTwoPoints(p1 : Vector2D, p2 : Vector2D) {

      val horizontal1 = horizontalGuide(p1)
      val vertical1   = verticalGuide(p1)
      val horizontal2 = horizontalGuide(p2)
      val vertical2   = verticalGuide(p2)
      val m = View.mousePositionDrawing

      //draw the vertical tracking guide
      if (vertical1.distanceTo(m) < Siigna.trackDistance && vertical1.distanceTo(m) <= vertical2.distanceTo(m))
        g draw LineShape(vertical1.p1, vertical1.p2, attributes).transform(t)
      if (vertical2.distanceTo(m) < Siigna.trackDistance && vertical2.distanceTo(m) <= vertical1.distanceTo(m))
        g draw LineShape(vertical2.p1, vertical2.p2, attributes).transform(t)

      //draw the horizontal tracking guide
      if (horizontal1.distanceTo(m) < Siigna.trackDistance && horizontal1.distanceTo(m) <= horizontal2.distanceTo(m))
        g draw LineShape(horizontal1.p1, horizontal1.p2, attributes).transform(t)
      if (horizontal2.distanceTo(m) < Siigna.trackDistance && horizontal2.distanceTo(m) <= horizontal1.distanceTo(m))
        g draw LineShape(horizontal2.p1, horizontal2.p2, attributes).transform(t)
    }

    if (Siigna.isTrackEnabled) {
      //PAINT TRACKING POINT ONE
         if (pointOne.isDefined && pointTwo.isEmpty ) paintOnePoint(pointOne.get)

      //PAINT BOTH TRACKING POINTS, IF THEY ARE THERE:::
      if (pointTwo.isDefined) paintTwoPoints(pointOne.get, pointTwo.get)
    }
  }

}