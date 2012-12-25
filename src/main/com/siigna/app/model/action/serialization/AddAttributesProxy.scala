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

package com.siigna.app.model.action.serialization

import java.io.{ObjectInput, ObjectOutput}
import com.siigna.util.collection.Attributes
import com.siigna.app.model.action.AddAttributes

/**
 * A proxy class used to serialize and de-serialize instances of AddAttributes.
 *
 * @see Effective Java 2nd Edition, item 78.
 * @param shapes  The ids and the shapes that are associated with the ids.
 */
@SerialVersionUID(-435878939)
sealed protected[action] class AddAttributesProxy(shapes : Map[Int, Attributes], attributes : Attributes)
  extends SerializationProxy(() => AddAttributes(AAP.shapes, AAP.attributes)) {

  /**
   * A public, empty constructor required by the Externalizable trait.
   * @return  An empty AddAttributesProxy
   */
  def this() = this(Map(), Attributes())

  def writeExternal(out: ObjectOutput) {
    out.writeInt(shapes.size)
    shapes.foreach{ case (i : Int, a : Attributes) => {
      out.writeInt(i)
      out.writeObject(a)
    }}
    out.writeObject(attributes)
  }

  def readExternal(in: ObjectInput) {
    val size = in.readInt()
    AAP.shapes = new Array[(Int, Attributes)](size).map(_ => {
      in.readInt() -> in.readObject().asInstanceOf[Attributes]
    }).toMap
    AAP.attributes = in.readObject().asInstanceOf[Attributes]
  }

}

// An object for persistant storage
private[serialization] object AAP {
  var shapes : Map[Int, Attributes] = Map()
  var attributes = Attributes()
}