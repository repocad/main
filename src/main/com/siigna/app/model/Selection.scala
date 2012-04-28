package com.siigna.app.model

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

import action.{Delete, Action}
import com.siigna.util.collection.Attributes
import com.siigna.util.geom.{TransformationMatrix, Vector2D}
import shape.{PartialShape, Shape, ShapeSelector, ShapeLike}

/**
 * A Selection is a mutable wrapper for a regular Shape(s).
 * When altered, the selection saves the action required to alter the shape(s) in the static layer, so the changes
 * can be made to the static version later on - when the shape(s) are "demoted" back into the static layer.
 *
 * @param parts  The ids of the wrapped shape(s).
 * @see [[com.siigna.app.model.MutableModel]]
 */
case class Selection(var parts: Map[Int, ShapeSelector]) extends ShapeLike {

  type T = Selection

  /**
   * The underlying action with which this Selection has been changed since creation, if any.
   */
  var action: Option[Action] = None

  /**
   * Stores a private transformation matrix that indicates the translation applied to the
   * Dynamic ShapeLike since creation.
   */
  private var transformation: TransformationMatrix = TransformationMatrix()

  /**
   * The attributes of the underlying ImmutableShapes.
   */
  def attributes = Attributes()

  /**
   * The boundary of the underlying ImmutableShapes.
   * @return A Rectangle2D.
   */
  def boundary = parts.map(s => Model(s._1)).foldLeft(Model(parts.head._1).boundary)((a, b) => a.expand(b.boundary))

  /**
   * Deletes the [[com.siigna.app.model.shape.ShapeSelector]] associated with the given id, if it exists and remove the
   * part from the Model with a [[com.siigna.app.model.action.DeleteShapePart]] action.
   * @param id  The id of the shape.
   */
  def delete(id : Int) {
    if (parts.contains(id)) {
      val part = parts(id)
      // Execute action
      Delete(id, part)
      parts = parts.-(id)
    }
  }

  /**
   * Calculates the distance from the vector and to the underlying Shape.
   * @param point  The point to calculate the distance to.
   * @param scale  The scale in which we are calculating.
   * @return  The length from the closest point of this shape to the point.
   */
  def distanceTo(point: Vector2D, scale: Double) = parts.map(s => Model(s._1).distanceTo(point)).reduceLeft((a, b) => if (a < b) a else b) * scale

  /**
   * Returns the current transformation applied to the shape.
   * @return  The transformation as a [[com.siigna.util.geom.TransformationMatrix]].
   */
  def getTransformation = transformation

  /**
   * Retrieves the current shapes of the selection.
   * @return A map containing the ids and the shapes used in the current selection.
   */
  def shapes : Map[Int, Shape] = {
    parts.map((t : (Int, ShapeSelector)) => {
      (t._1 -> Model(t._1))
    })
  }

  def setAttributes(attributes: Attributes) = this // TODO: Create some kind of (set/create/update)attribute action

  /**
   * Transforms the underlying Shape by adding a TransformShape action to the list of actions
   * applied to this Selection
   * @param transformation  The TransformationMatrix to apply to the shape.
   */
  def transform(transformation: TransformationMatrix) = {
    // Store the transformation
    this.transformation = transformation
    // Return this
    this
  }
}

/**
 * Companion-object for the Selection class.
 */
object Selection {

  /**
   * A method to create a Selection with only one id.
   */
  def apply(id: Int, part : ShapeSelector) = {
    new Selection(Map(id -> part))
  }

}