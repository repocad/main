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

import com.siigna.app.model.shape._
import com.siigna.util.geom.{TransformationMatrix, Vector2D}
import com.siigna.app.view.{Graphics, View}
import com.siigna.app.Siigna
import java.awt.Color
import com.siigna.app.model.{Model, Drawing}

/**
 * A hook for parsing points that snaps to intersections between objects.
 */
case object IntersectionPointSnap extends EventSnap {

  val colorAttr = "Color" -> new Color(0.10f, 0.95f, 0.95f, 0.40f)
  val strokeAttr = "StrokeWidth" -> 0.4
  var snapPoint : Option[Vector2D] = None
  def snapCircle (p : Vector2D) = {
    val r = 0.5 * Siigna.selectionDistance
    CircleShape(p,r)
  }
  def parse(event : Event, model : Traversable[Shape]) = event match {
    case MouseDown(point, a, b)  => MouseDown(snap(point, model), a, b)
    case MouseDrag(point, a, b)  => MouseDrag(snap(point, model), a, b)
    case MouseMove(point, a, b)  => MouseMove(snap(point, model), a, b)
    case MouseUp(point, a, b)    => MouseUp(snap(point, model), a, b)
    case some => some
  }

  /**
   *
   * @param q the point entering Snap is in DeviceCoordinates. (See View.deviceTransformation for explanation)
   *          it is changed to drawingCoordinates in order to be able to evaluate its position
   *          relative to shapes in the drawing
   * @param model If snap is in range, the mouse is moved to to sit on top of the closest end point
   * @return the new mouse point,in deviceCoordinates.

   */
  def snap(q : Vector2D, model : Traversable[Shape]) : Vector2D = {

    //the point is transformed to match the coordinate system of the drawing
    val point = q.transform(View.deviceTransformation)

    //find the intersection closest to the mouse
    def nearestInt (v : List[Vector2D], p : Vector2D) : Vector2D = {
      val list = v.sortBy(_.distanceTo(p))
      list.head //return closest point
    }

    //a function to limit intersection evaluation to shapes within the bounding box of the shape near the mouse.
    def shapesInRange(s : Shape, p : Vector2D) : Map[Int,Shape] = {
      val evalDist = (s.geometry.boundary.topLeft - point).length * 2
      //get all shapes which potentially intersect with the current nearest shape
      val shapes = Drawing(point,evalDist)
      shapes
    }

    if (!model.isEmpty) {

      val res = model.map(_ match {

        case s : ArcShape       => {
          val shapes = (shapesInRange(s, point)) //get the potentially intersecting shapes
          val shapeGeometries = shapes.map(s => s._2.geometry) //make a list of their geometries
          val ints = shapeGeometries.flatMap(g => g.intersections(s.geometry)) //get intersections to evaluate
          if(!ints.isEmpty) nearestInt(ints.toList,point) else q //return nearest intersection or the unparsed point q if no int.
        }

        case s : CircleShape    => {
          val shapes = (shapesInRange(s, point)) //get the potentially intersecting shapes
          val shapeGeometries = shapes.map(s => s._2.geometry) //make a list of their geometries
          val ints = shapeGeometries.flatMap(g => g.intersections(s.geometry)) //get intersections to evaluate
          if(!ints.isEmpty) nearestInt(ints.toList,point) else q //return nearest intersection or the unparsed point q if no int.
        }

        case s : LineShape        => {
          val shapes = (shapesInRange(s, point)) //get the potentially intersecting shapes
          val shapeGeometries = shapes.map(s => s._2.geometry) //make a list of their geometries
          val ints = shapeGeometries.flatMap(g => g.intersections(s.geometry)) //get intersections to evaluate
          if(!ints.isEmpty) nearestInt(ints.toList,point) else q //return nearest intersection or the unparsed point q if no int.
        }

        case s : PolylineShape  => {
          val shapes = (shapesInRange(s, point)) //get the potentially intersecting shapes
          val shapeGeometries = shapes.map(s => s._2.geometry) //make a list of their geometries
          val ints = shapeGeometries.flatMap(g => g.intersections(s.geometry)) //get intersections to evaluate
          if(!ints.isEmpty) nearestInt(ints.toList,point) else q //return nearest intersection or the unparsed point q if no int.
        }

        //TODO: no intersections are found?
        case s : RectangleShape => {
          val shapes = (shapesInRange(s, point)) //get the potentially intersecting shapes
          val shapeGeometries = shapes.map(s => s._2.geometry) //make a list of their geometries
          val ints = shapeGeometries.flatMap(g => {
              val t = g.intersections(s.geometry)
              t
            }) //get intersections to evaluate
          if(!ints.isEmpty) nearestInt(ints.toList,point) else q //return nearest intersection or the unparsed point q if no int.
        }
        case e => point
      })
      //
      val closestPoint = res.toVector.head

      if (closestPoint.distanceTo(point) < Siigna.selectionDistance) {
        //the snapPoint variable is set, so that it can be used to draw visual feedback:
        snapPoint = Some(closestPoint)
        //RETURN: the snapped (moved) point, transformed back to the drawing coordinates is returned:
        closestPoint.transform(View.drawingTransformation)
      } else {
        //no snap in range, return the point
        snapPoint = None
        q
      }
    } else {
      snapPoint = None
      q
    }
  }

  override def paint(g : Graphics, t : TransformationMatrix) {
    //show the snappoints
    if(snapPoint.isDefined) snapPoint.foreach(p => {
      g.draw(snapCircle(p).transform(t).addAttributes(colorAttr,strokeAttr))
    })
  }
}