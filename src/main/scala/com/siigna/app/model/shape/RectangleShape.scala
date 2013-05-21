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

package com.siigna.app.model.shape

import com.siigna.util.geom._
import com.siigna.util.collection.Attributes
import com.siigna.app.model.selection._
import com.siigna.app.Siigna
import com.siigna.util.geom.ComplexRectangle2D
import scala.Some
import collection.immutable.BitSet

case class RectangleShape(center : Vector2D, width : Double, height : Double, rotation : Double, attributes : Attributes) extends ClosedShape {

  type T = RectangleShape

  val geometry = ComplexRectangle2D(center,width,height,rotation)

  val p1 = geometry.bottomLeft //BitSet 3
  val p2 = geometry.topRight  //BitSet 2
  val p3 = geometry.bottomRight //BitSet 1
  val p4 = geometry.topLeft //BitSet 0

  def delete(part : ShapeSelector) = part match {
    case BitSetShapeSelector(_) | FullShapeSelector => Nil
    case _ => Seq(this)
  }

  def getPart(part : ShapeSelector) = part match {
    case ShapeSelector(0) => Some(new PartialShape(this, (t : TransformationMatrix) => {
      LineShape(p1.transform(t), p4, attributes)
      LineShape(p1.transform(t), p2, attributes)
    }))
    case ShapeSelector(1) => Some(new PartialShape(this, (t : TransformationMatrix) => {
      LineShape(p1, p2.transform(t), attributes)
      LineShape(p3, p2.transform(t), attributes)
    }))
    case ShapeSelector(2) => Some(new PartialShape(this, (t : TransformationMatrix) => {
      LineShape(p2, p3.transform(t), attributes)
      LineShape(p4, p3.transform(t), attributes)
    }))
    case ShapeSelector(3) => Some(new PartialShape(this, (t : TransformationMatrix) => {
      LineShape(p3, p4.transform(t), attributes)
      LineShape(p1, p4.transform(t), attributes)
    }))
    case FullShapeSelector => Some(new PartialShape(this, transform))
    case _ => None
  }

  def getSelector(p : Vector2D) = {
    val selectionDistance = Siigna.selectionDistance

    //find out if a point in the rectangle is close. if so, return true and the point's bit value
    def isCloseToPoint(s : Segment2D, b : BitSet) : (Boolean, BitSet) = {
      val points = List(s.p1, s.p2)
      var hasClosePoint = false
      var bit = BitSet()
      //evaluate is one of the two points of a segment is close
      for(i <- 0 until points.size) {
        if(points(0).distanceTo(p) <= selectionDistance) {
          hasClosePoint = true
          bit = BitSet(b.head)
        }
        else if (points(1).distanceTo(p) <= selectionDistance) {
          hasClosePoint = true
          bit = BitSet(b.last)
          }
        }
      (hasClosePoint, bit)
    }

    //find out if a segment of the rectangle is close. if so, return true, the segment, and the segment's bit value
    def isCloseToSegment : (Boolean, Option[Segment2D], BitSet) = {
      val l = List(Segment2D(p1,p2),Segment2D(p2,p3),Segment2D(p3,p4), Segment2D(p4,p1))
      var closeSegment : Option[Segment2D] = None
      var hasCloseSegment = false
      var bit = BitSet()
      for(i <- 0 until l.size) {
        if(p.distanceTo(l(i)) <= selectionDistance) {
          hasCloseSegment = true
          closeSegment = Some(l(i))
          bit = BitSet(i, if (i == 3) 0 else i + 1)
        }
      }
      (hasCloseSegment, closeSegment, bit)
    }

    //if the distance to the rectangle is more than the selection distance:
    if (geometry.distanceTo(p) > selectionDistance) {
      //If shape is not within selection distance of point, return Empty selector
      EmptyShapeSelector
      //if not either the whole rectangle, a segment, or a point should be selected:
    } else {
      //if the point is in range of one of the segments of the rectangle... :
      if(isCloseToSegment._1 == true){
        val segment = isCloseToSegment._2.get
        val segmentBitSet = isCloseToSegment._3
        //ok, the point is close to a segment. IF one of the endpoints are close, return its bit value:
        if(isCloseToPoint(segment, segmentBitSet)._1 == true) BitSetShapeSelector(isCloseToPoint(segment, segmentBitSet)._2)
        //if no point is close, return the bitset of the segment:
        else BitSetShapeSelector(isCloseToSegment._3)
      //if no point is close, return the segment:
      } else {
        EmptyShapeSelector
      }
    }
  }
  //needed for box selections?
  //TODO: is this right?
  def getSelector(r : SimpleRectangle2D) : ShapeSelector = {
    if (r.intersects(boundary)) {
      val cond1 = r.contains(p1)
      val cond2 = r.contains(p2)
      val cond3 = r.contains(p3)
      val cond4 = r.contains(p4)
      if (cond1 && cond2 && cond3 && cond4) {
        FullShapeSelector
      } else if (cond1) {
        ShapeSelector(0)
      } else if (cond2) {
        ShapeSelector(1)
      } else if (cond3) {
        ShapeSelector(2)
      } else if (cond4) {
        ShapeSelector(3)
      } else EmptyShapeSelector
    } else EmptyShapeSelector
  }

  //TODO: is this right? and when is a complex rectangle ever used to make a selection??
  def getSelector(r : ComplexRectangle2D) : ShapeSelector = {
    if (r.intersects(boundary)) {
      val cond1 = r.contains(p1)
      val cond2 = r.contains(p2)
      val cond3 = r.contains(p3)
      val cond4 = r.contains(p4)
      if (cond1 && cond2 && cond3 && cond4) {
        FullShapeSelector
      } else if (cond1) {
        ShapeSelector(0)
      } else if (cond2) {
        ShapeSelector(1)
      } else if (cond3) {
        ShapeSelector(2)
      } else if (cond4) {
        ShapeSelector(3)
      } else EmptyShapeSelector
    } else EmptyShapeSelector
  }

  def getShape(s : ShapeSelector) = s match {
    case FullShapeSelector => Some(this)
    case _ => None
  }

  //TODO: expand to allow for all combinations of selections of the four vertices.
  def getVertices(selector: ShapeSelector) = {

    selector match {
      case FullShapeSelector => geometry.vertices
      case ShapeSelector(0) => Seq(p1)
      case ShapeSelector(1) => Seq(p2)
      case ShapeSelector(2) => Seq(p3)
      case ShapeSelector(3) => Seq(p4)
      case _ => Seq()
    }
  }

  def setAttributes(attributes : Attributes) = RectangleShape(center, width,height,rotation, attributes)

  def transform(t : TransformationMatrix) =
    RectangleShape(center transform(t), width * t.scale, height * t.scale, rotation, attributes)

}
