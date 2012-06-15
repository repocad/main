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
package com.siigna.app.model.action

import com.siigna.app.model.shape.{ShapeSelector, Shape}
import com.siigna.app.model.{Selection, Model}

object Delete {
  
  def apply(id : Int) {
    Model execute DeleteShape(id, Model(id))
  }
  
  def apply(id : Int, part : ShapeSelector) {
    apply(Map[Int, ShapeSelector](id -> part))
  }
  
  def apply(shapes : Map[Int, ShapeSelector]) {
    val oldShapes = shapes.map(t => t._1 -> Model(t._1))
    val newShapes = shapes.map(t => Model(t._1).delete(t._2)).flatten
    
    // Does the deletion result in new shapes?
    if (newShapes.isEmpty) { // No - that's easy!
      Model execute DeleteShapes(oldShapes)
    } else { // Yes - now we need the magic
      Model.executeWithIds(newShapes, DeleteShapeParts(oldShapes, _))
    }
  }
  
  def apply(ids : Traversable[Int]) {
    Model execute DeleteShapes(ids.map(i => i -> Model(i)).toMap)
  }
  
  def apply(selection : Selection) {
    Model deselect()
    apply(selection.parts)
  }
  
}

/**
 * Deletes a shape.
 */
@SerialVersionUID(320024820)
sealed case class DeleteShape(id : Int, shape : Shape) extends Action {

  def execute(model : Model) = model remove id

  def undo(model : Model) = model.add(id, shape)

}

/**
 * Deletes a ShapeSelector.
 */
@SerialVersionUID(-1303124189)
sealed case class DeleteShapePart(id : Int, shape : Shape, part : ShapeSelector) extends Action {
  
  val parts = shape.delete(part); 
  var partIds = Seq[Int]()
  
  def execute(model : Model) = {
    if (parts.size == 0) {
      // Remove the shape if no parts result from the deletion
      model.remove(id)
    } else if (parts.size == 1) {
      // Replace the shape if the deletion result in one shape
      model.add(id, parts(0))
    } else {
      // Create the new shapes through a CreateAction
      // since we don't know if there are enough local id's
      Create(parts)

      // Remove the shape
      model.remove(id)
    }
  }
  
  def undo(model : Model) = {
    if (parts.size <= 1) {
      model.add(id, shape)
    } else {
      model.remove(id)
    }
  }
  
}

/**
 * Deletes a part of a shape represented as a shape selector.
 */
@SerialVersionUID(1143887988)
case class DeleteShapeParts(oldShapes : Map[Int, Shape], newShapes : Map[Int, Shape]) extends Action {
  
  def execute(model : Model) = 
    model.remove(oldShapes.keys).add(newShapes)
  
  def undo(model : Model) = 
    model.remove(newShapes.keys).add(oldShapes)
  
}

/**
 * Deletes a number of shapes.
 */
@SerialVersionUID(-113196732)
case class DeleteShapes(shapes : Map[Int, Shape]) extends Action {

  def execute(model : Model) = model remove shapes.keys

  def undo(model : Model) = model add shapes
}