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

package com.siigna.util.io.version

import com.siigna.util.io.{SiignaOutputStream, SiignaInputStream}
import reflect.runtime.universe.TypeTag

/**
 * A specific version of the IO used to write and read bytes as it was done in one particular version.
 * Please refer to the companion object [[com.siigna.util.io.version.IOVersion]] for retrieving the IOVersion
 * implementation for a given version.
 */
trait IOVersion {

  /**
   * Attempts to read a siigna object from the given input stream.
   * @param in  The input stream to read from.
   * @param members  The amount of members in the expected object.
   * @tparam E  The expected type of the object to read.
   * @return  The object that have been read from the underlying input stream.
   * @throws  ClassCastException  If the found object could not be cast to type E.
   * @throws  UBJFormatException  If the format could not be matched.
   */
  def readSiignaObject[E : TypeTag](in : SiignaInputStream, members : Int) : E

  /**
   * Write an object from the Siigna domain to the output stream.
   * @param out The output stream to write to.
   * @param obj  The object to write.
   * @throws  IllegalArgumentException  If the object could not be recognized
   */
  def writeSiignaObject(out : SiignaOutputStream, obj : Any)

}

/**
 * Used to retrieve an instance of a [[com.siigna.util.io.version.IOVersion]] given a version number like so:
 * {{{
 *   val versionNumber = 1
 *   val versionIO     = IOVersion(1)
 * }}}
 *
 * Version numbers can be retrieved like so:
 * {{{
 *   IOVersion.One     // Version One
 *   IOVersion.Current //
 * }}}
 */
object IOVersion {

  // Version one
  val One   = 1.asInstanceOf[Byte]
  val Two   = 2.asInstanceOf[Byte]
//val Three = 3.asInstanceOf[Byte]
//And so on...

  // The current working version
  val Current = Two

  /**
   * Attempts to retrieve the I/O implementation for the given version number.
   * @param version  The version to retrieve.
   */
  def apply(version : Int) : IOVersion = version match {
    case 1 => IOVersion1
    case 2 => IOVersion2
    case _ => throw new IllegalArgumentException("IOVersion: Could not find an implementation for version: " + version)
  }

}
