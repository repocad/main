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
package com.siigna.app.model.action

import com.siigna.app.model.shape.{CollectionShape, Shape}
import com.siigna.app.model.{Drawing, Model}
import com.siigna.util.Log

/**
* An object that allows you to create one or more shapes.
*/
object Create {

  /**
   * Creates a single shape.
   * @param shape  The shape to create.
   */
  def apply(shape : Shape) {
    val id = Drawing.getId
    com.siigna.app.Siigna.latestID = Some(id)
    Drawing.execute(CreateShape(id, shape))
  }

  /**
   * Creates a single collection shape. This method is created to avoid collision with shapes : Traversable[Shape]
   * @param collection  The CollectionShape to create
   * @tparam T  The type of the entries in the shape.
   */
  def apply[T <: Shape](collection : CollectionShape[T]) {
    val id = Drawing.getId
    com.siigna.app.Siigna.latestID = Some(id)
    Drawing.execute(CreateShape(id, collection))
  }

  /**
   * Creates several shapes.
   * @param shape1  The first shape to create.
   * @param shape2  The second shape to create.
   * @param shapes  Any other shapes (optional).
   */
  def apply(shape1 : Shape, shape2 : Shape, shapes : Shape*) {
    apply(Iterable(shape1, shape2) ++ shapes)
  }

  /**
   * Creates a number of shapes.
   * @param shapes  The shapes to create.
   */
  def apply(shapes : Traversable[Shape]) {
    if (shapes.size > 1) {
      Drawing.execute(CreateShapes(Drawing.getIds(shapes)))
    } else if (shapes.size == 1) {
      apply(shapes.head)
    } else {
      Log.info("Create: Create was called with an empty collection of shapes.")
    }
  }

}

/**
 * A super class to all actions that create shapes.
 * @author csp <csp@siigna.com>
 */
trait CreateAction extends Action

/**
 * <p>Creates a shape with an associated id.</p>
 *
 * @define actionFactory [[com.siigna.app.model.action.Create]]
 * $actionDescription
 */
case class CreateShape(id : Int, shape : Shape) extends CreateAction {
  
  def execute(model : Model) = model add (id, shape)

  def ids = Traversable(id)

  def undo(model : Model) = model remove id

  def update(map : Map[Int, Int]) = CreateShape(map.getOrElse(id, id), shape)

}

/**
 * Creates several shapes.
 * @define actionFactory [[com.siigna.app.model.action.Create]]
 * $actionDescription
 */
case class CreateShapes(shapes : Map[Int, Shape]) extends CreateAction {

  require(shapes.size > 0, "Cannot initialize CreateShapes with zero shapes.")
  
  def execute(model : Model) = model add shapes
  
  def ids = shapes.keys

  def undo(model : Model) = model remove shapes.keys
  
  def update(map : Map[Int, Int]) = copy(shapes.map(t => map.getOrElse(t._1, t._1) -> t._2))
  
}