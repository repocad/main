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

package com.siigna.app.model

import action.{Transform, AddAttributes}
import com.siigna.app.model.shape.Shape
import scala.reflect.runtime.universe._
import com.siigna.app.model.selection._
import com.siigna.util.geom.SimpleRectangle2D

/**
 * <p>
 *   A trait that interfaces with the [[com.siigna.app.model.selection.Selection]] of the model (or as we call it
 *   internally, the "dynamic" or "mutable" layer) that, if not empty, represents parts or subsets of one or more
 *   selected shapes.
 *</p>
 *
 * <p>
 *   The SelectableModel is a way to indirectly manipulate the underlying [[com.siigna.app.model.ImmutableModel]]
 *   through the temporary "dynamic layer" that can be changed and updated without any effect on the actual
 *   shapes, but with visual feedback to the user. This can be very useful (and gives enormous performance benefits)
 *   when you need to alter shapes many times before storing any final changes.
 * </p>
 *
 * <p>
 *   To create a selection the <code>select</code> method should be used. This method creates a selection of the given
 *   shapes and [[com.siigna.app.model.selection.ShapePart]]s and adds it to the current selection, if any. The
 *   manipulations done by the user are not stored before it has been deselected via the method <code>deselect</code>.
 *   The method collects all the changes stored in the selection and applies them to the model.
 *   <br>
 *   FYI: The actual selection consists of a map of Ints and [[com.siigna.app.model.selection.ShapePart]]s.
 * </p>
 *
 * <h2>Use cases</h2>
 * <p>
 *   If a users finds any shapes he/she would like to manipulate, they can be altered many times via the
 *   [[com.siigna.app.model.selection.Selection]], giving the user a chance to see the output of the transformations
 *   before storing them. The first example covers a simple way to retrieve shapes from the
 *   [[com.siigna.app.model.Drawing]], setting their attributes and moving them 100 units to the right. Afterwards
 *   we <code>deselect</code> the selection, to store the changes permanently.
 * </p>
 * {{{
 *   // Get some random shapes from the Drawing (this finds shapes close to (0, 0))
 *   val shapes = Drawing( Vector2D( 0, 0) )
 *
 *   // Select them
 *   Drawing.select(shapes)
 *
 *   // Set the color to white
 *   Drawing.selection.addAttribute("Color" -> "#FFFFFF".color)
 *
 *   // Move the selection 100 units to the right
 *   Drawing.selection.transform( TransformationMatrix( Vector2D(100, 0) ) )
 *
 * }}}
 *
 * @see [[com.siigna.app.model.Drawing]], [[com.siigna.app.model.Model]], [[com.siigna.app.model.selection.Selection]],
 *     [[com.siigna.app.model.selection.ShapePart]]
 */
trait SelectableModel {

  /**
   * The MutableModel on which the selections can be performed.
   */
  protected def model : Model

  /**
   * Deletes the current selection without applying the changes to the [[com.siigna.app.model.Drawing]]. This can
   * be used if the user regrets the changes and wishes to annul the selection instead of saving it. Synonym to
   * setting the selection to [[com.siigna.app.model.selection.Selection.empty]].
   * @return  The new (empty) [[com.siigna.app.model.selection.Selection]].
   */
  def clearSelection() = {
    selection = Selection.empty
    selection
  }

  /**
   * Deselects the [[com.siigna.app.model.selection.Selection]] in the Model by setting the selection to None, and
   * applies the changes executed on the selection since it was selected.
   * <br>
   * This is import to remember so the changes from the selection can be made into
   * [[com.siigna.app.model.action.Action]]s that can be saved in the [[com.siigna.app.model.Model]].
   * @return  The new (empty) selection.
   */
  def deselect() = {
    executeChanges(selection)
    selection = Selection.empty
    selection
  }

  /**
   * Executes the changes stored in the given selection as [[com.siigna.app.model.action.Action]]s, if any changes
   * are found.
   * @param selection The [[com.siigna.app.model.selection.Selection]] containing changes to be executed.
   * @return  A Map of the previous selected ids, paired with the shapes and parts.
   */
  private def executeChanges(selection : Selection) : Selection = {
    selection match {
      case s : NonEmptySelection => {
        val a = s.attributes
        val t = s.transformation

        // Transform the selection
        Transform(s, t)

        // Assign the attributes from the selection
        AddAttributes(s.keys, a)
      }
      case _ =>
    }
    selection
  }

  /**
   * Selects an entire shape based on its id. Equivalent to calling select(id, FullShapePart)
   * @param id  The id of the shape to select.
   * @throws  NoSuchElementException  If the shape with the given ID did not exist in the [[com.siigna.app.model.Model]].
   * @return  The new selection with the added shape.
   */
  def select(id : Int) {
    select(id, FullShapeSelector)
    selection
  }

  /**
   * Selects several whole shapes based on their ids.
   * @param ids  The id's of the shapes to select.
   * @throws NoSuchElementException If one or more of the ids could not be found in the [[com.siigna.app.model.Model]].
   * @return  The new selection with the added shapes.
   */
  def select(ids : Traversable[Int]) {
    val shapes = ids.map(i => i -> Drawing(i))

    if (!shapes.isEmpty) {
      // First execute the changes
      executeChanges(selection)

      // Then create a new selection
      selection = selection.add(shapes.map(s => s._1 -> s._2.getPart(FullShapeSelector)).toMap[Int, ShapePart[Shape]])
    }

    selection
  }

  /**
   * Selects a part of a shape based on its id. If the ShapePart is a FullShapePart then the
   * entire shape is selected, if the part is empty or no shape with the given id could be found in the model,
   * nothing happens.
   * @param id  The id of the shape
   * @param selector  The selector of the shape describing how the shape should be selected.
   * @tparam T  The type of the shape to select.
   * @return  The new selection after the selection.
   */
  def select[T <: Shape : TypeTag](id : Int, selector : ShapeSelector[T]) : Selection = {
    def selectType[U <: Shape : TypeTag](shape : U) {
      shape match {
        case x if (typeOf[U] <:< typeOf[T]) => {
          // First execute the changes
          executeChanges(selection)

          // Then create a new selection
          selection = selection.add(id, x.getPart(selector.asInstanceOf[ShapeSelector[x.T]]))
        }
        case _ =>
      }
    }

    selector match {
      case EmptyShapeSelector =>
      case _ => Drawing.get(id).foreach(selectType(_))
    }

    selection
  }

  def select(rectangle : SimpleRectangle2D, entireShapes : Boolean = true) : Selection = {
    val shapes = if (!entireShapes) {
      model(rectangle).map(t => t._1 -> t._2.getPart(rectangle))
    } else {
      // TODO: Write a method that can take t._2.geometry and NOT it's boundary...
      model(rectangle).collect {
        case t if (rectangle.intersects(t._2.geometry.boundary)) => (t._1 -> t._2.getPart(FullShapeSelector))
      }
    }
    select(Selection(shapes.toMap[Int, ShapePart[Shape]]))
    selection
  }

  /**
   * Add the given selection to the current selection by merging the already selected shape-parts with the given
   * shape-parts.
   * @param selection  The Selection representing the selection to add to the current selection.
   * @return  The new active selection.
   */
  def select(selection : Selection) : Selection = {
    this.selection = selection.add(selection)
    selection
  }

  /**
   * Select every shape in the Model.
   */
  def selectAll() {
    selection = Selection(Drawing.map(i => i._1 -> i._2.getPart(FullShapeSelector)).toMap[Int, ShapePart[Shape]])
  }

  /**
   * The current selection represented by a [[com.siigna.app.model.selection.Selection]] where shapes can and can not
   * be set.
   * @return  Some[Selection] if a selection is active or None if nothing has been selected
   */
  def selection : Selection = model.selection

  /**
   * Overrides (removes) the current selection and sets it to the given selection instead. This will remove all
   * the changes made to the previous selection.
   * @param selection  The new selection to use.
   */
  def selection_=(selection : Selection) { model.selection = selection }

}