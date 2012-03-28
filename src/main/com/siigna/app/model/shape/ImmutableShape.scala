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

package com.siigna.app.model.shape

import com.siigna.util.collection.Attributes
import com.siigna.util.dxf.DXFSection
import com.siigna.util.geom._

/**
 * The trait for a Shape.
 * Every shapes are immutable and thus cannot be changed directly. To directly manipulate a shape, you have
 * to utilize <code>Actions</code>.
 * TODO: Implement dynamic shapes.
 */
trait ImmutableShape extends Shape {

  type T <: ImmutableShape

  /**
   * Merge the new attributes in with the existing ones, eventually overwriting
   * attributes with new values.
   *
   * @param  attribute  the new attributes to merge in.
   * @return  a shape with the updated attributes.
   *
   * TODO: Refactor to addAttribute
   */
  override def addAttribute(attribute : (String, Any)) = setAttributes(attributes + attribute)

  /**
   * Merge the new attributes in with the existing ones, eventually overwriting
   * attributes with new values.
   *
   * @param  attributes  the new attributes to merge in.
   * @return  a shape with the updated attributes.
   *
   * TODO: Refactor to addAttributes
   */
  override def addAttributes(attributes : (String, Any)*) = setAttributes(this.attributes ++ attributes)

  /**
   * Calculates the closest distance to the shape in the given scale.
   */
  def distanceTo(point : Vector2D, scale : Double) = geometry.distanceTo(point) * scale

  /**
   * Returns a rectangle that includes the entire shape.
   */
  def boundary : Rectangle2D = geometry.boundary

  /**
   * The basic geometric object for the shape.
   */
  def geometry : Geometry2D

  /**
   * Selects the entire shape and wraps it into a DynamicShape, so it can be manipulated dynamically.
   * @return  The shape wrapped into a corresponding [[com.siigna.app.model.shape.DynamicShape]].
   */
  def select() : DynamicShape = DynamicShape(attributes.int("id").get, transform(_))

  /**
   * Selects a shape by a rectangle. If the rectangle encloses the entire shape then select everything, but if
   * only a single point is enclosed (for example) then select that point and that point only. If nothing is
   * enclosed, then return None. This comes in handy when a selection-box sweeps across the model.
   * @param rect  The rectangle to base the selection on.
   * @return  The shape (or parts of it - or nothing at all) wrapped in a [[com.siigna.app.model.shape.DynamicShape]].
   */
  def select(rect : Rectangle2D) : Option[DynamicShape]

  /**
   * Select a shape by a single point. The part of the shape that is closes to that point will be selected.
   * @param point  The point to base the selection on.
   * @return  The shape (or a part of it - or nothing at all) wrapped in a [[com.siigna.app.model.shape.DynamicShape]].
   */
  def select(point : Vector2D) : Option[DynamicShape] // select shape close to the point

  /**
   * Returns a setAttributes of the shape. In other words return a shape with a new id,
   * but otherwise the same attributes.
   */
  def setAttributes(attributes : Attributes) : T

  /**
   * Returns a DXFSection with the given shape represented.
   */
  def toDXF : DXFSection

  /**
   * Applies a transformation to the shape.
   */
  def transform(transformation : TransformationMatrix) : T

}

/**
 * A trait used for constructing Polylines when we need to match Lines and Arcs.
 * BasicShape is extended by two shapes: ArcShape and LineShape.
 */
trait BasicShape extends ImmutableShape {

  type T <: BasicShape

  /**
   * The basic geometric object for the shape.
   */
  def geometry : GeometryBasic2D

  /**
   * Returns a setAttributes of the shape. In other words return a shape with a new id,
   * but otherwise the same attributes.
   */
  def setAttributes(attributes : Attributes) : T

  /**
   * Applies a transformation to the BasicShape.
   */
  def transform(transformation : TransformationMatrix) : T

}

/**
 * A shape that's closed, that is to say a shape that encases a closed space.
 */
trait EnclosedShape extends ImmutableShape {

  type T <: EnclosedShape

  override def geometry : GeometryEnclosed2D

  /**
   * Returns a setAttributes of the shape. In other words return a shape with a new id,
   * but otherwise the same attributes.
   */
  def setAttributes(attributes : Attributes) : T

  /**
   * Applies a transformation to the BasicShape.
   */
  def transform(transformation : TransformationMatrix) : T

}