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

package com.siigna.util.geom

import org.scalatest.matchers.ShouldMatchers
import com.siigna.app.model.Model
import collection.parallel.immutable.{ParHashMap}
import com.siigna.app.model.shape.{LineShape}
import org.scalatest.FunSpec

/**
 * Test the immutable model to manipulate and contain a map of ints and shapes.
 */
class ImmutableModelSpec extends FunSpec with ShouldMatchers {

  val model = new Model()

  describe("An immutable model") {
    val v1 = Vector2D(0, 0)
    val v2 = Vector2D(10, 10)
    val l1 = LineShape(v1, v2)
    val l2 = LineShape(v2, v1)
    val lines = Map(0 -> l1, 1 -> l2)

    it("can add one shape") {
      // Single shape
      model.add(0, l1).shapes should equal(ParHashMap(0 -> l1))
      model.shapes.size should equal(0)

      // Single shape, weird ids
      model.add(-500, l1).shapes should equal(ParHashMap(-500 -> l1))
      model.add(Int.MaxValue + 1, l1).shapes should equal(ParHashMap(Int.MinValue -> l1))
    }

    it("can add several shapes") {
      // Several shapes
      model.add(lines).shapes should equal(ParHashMap(0 -> LineShape(v1, v2), 1 -> LineShape(v2, v1)))
    }

    it("can remove a shape") {
      // Single shape
      model.add(0, l1).remove(0).shapes should equal(ParHashMap())

      // Non-existing
      model.remove(0).shapes should equal(ParHashMap())
    }

    it("can remove several shapes") {
      model.add(lines).remove(Seq(0, 1)).shapes should equal(ParHashMap())
      model.add(lines).remove(1).shapes should equal(ParHashMap(0 -> l1))
      model.add(lines).remove(0).shapes should equal(ParHashMap(1 -> l2))
    }

    it("can update a shape") {
      model.add(0, l1).add(0, l2).shapes should equal(ParHashMap(0 -> l2))
    }

  }
}