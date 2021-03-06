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
package com.siigna.util.geom

/**
 * An endless mathematically representation of a line with infinite length. 
 */
trait Line

/**
 * A companion object to the Line trait.
 */
object Line {

  /**
   * Creates a 2D line.
   */
  def apply(p1 : Vector2D, p2 : Vector2D) = new Line2D(p1, p2)

}

/**
 * A mathematically representation of a line with infinite length.
 * TODO: Can this be represented as a equation?
 */
case class Line2D(p1 : Vector2D, p2 : Vector2D) extends Line with Geometry2D {

  type T = Line2D

  def boundary = Rectangle2D(p1, p2)

  /**
   * Calculates the closest point on the line from a given vector.
   */
  def closestPoint(point : Vector2D) = {
    // Found this at: http://local.wasp.uwa.edu.au/~pbourke/geometry/pointline/
    // We take advantage of the fact that the slope of two orthogonal lines
    // multiplied together = -1.
    val lineVector = p2 - p1
    val constant   = ((point.x - p1.x) * (p2.x - p1.x) + (point.y - p1.y) * (p2.y - p1.y)) / (lineVector.length * lineVector.length)

    // Use the determinant to calculate the point.
    val x = p1.x + constant * (p2.x - p1.x)
    val y = p1.y + constant * (p2.y - p1.y)

    // Return the point.
    Vector2D(x, y)
  }

  def distanceTo(geom : Geometry2D) = geom match {
    case point : Vector2D => if (p1 != p2) {
      // Calculate the distance from the projection on the line to the point.
      (closestPoint(point) - point).length
    } else {
      (p1 - point).length
    }
    case g => Double.PositiveInfinity
  }

  def intersects(geom : Geometry2D) = false

  def intersections(geom : Geometry2D) : Set[Vector2D] = geom match {
    /**
     * Returns a list of points, defined as the intersection(s) between the
     * line and the circle.
     */
    case circle : Circle2D => {
      val f           = p2 - p1
      val g           = p1 - circle.center
      val determinant = (f * g) * (f * g) - (f * f) * (g * g - circle.radius * circle.radius)

      val t : Set[Double] =
        if (determinant < 0)
          Set()
        else if (determinant > 0)
          Set((-(f * g) - scala.math.sqrt(determinant)) / (f * f),
            (-(f * g) + scala.math.sqrt(determinant)) / (f * f))
        else
          Set(-(f * g) / (f * f))

      t map(f * _ + p1)
    }
    /**
     * Returns a set of points, defined as the intersection(s) between the
     * two endless lines.
     */
    case line : Line2D => {
      // The lines are defined by the equations:
      //   L1 = A*u + C
      //   L2 = B*v + D
      val A = p2 - p1
      val B = line.p2 - line.p1
      val C = p1
      val D = line.p1

      // To findRecursive the intersection, set the two above equations equal:
      //   L1 = L2  <=>  A*u + C = B*v + D
      // The intersection is defined by solving (u,v) in the linear system:
      //   A*u - B*v = D - C
      // Calculate the determinant:
      //   det = | A  -B | = | B  A |
      // If det = 0 there are no solutions (the lines are parallel). Now we findRecursive
      // the solution for the first line, L1:
      //   u = | (D - C)  -B | / det = | B  (D - C) | / det
      val det = Vector2D.determinant(B, A)
      val u   = Vector2D.determinant(B, D - C) / det

      // To calculate the point of intersection we insert u in the line equation.
      // If the determinant = 0, the lines are parallel
      if (det == 0)
        Set()
      else
        Set[Vector2D](A * u + C)
    }

    //catches both ComplexRectangle2D and SimpleRectangle2D
    case r : Rectangle2D => {
      val i = r.segments.flatMap(s => s.intersections(this)).toSet
      i
    }

    //Segment2D - Line2D intersections
    case segment : Segment2D => {
      val x1 = this.p1.x
      val y1 = this.p1.y
      val x2 = this.p2.x
      val y2 = this.p2.y
      val x3 = segment.p1.x
      val y3 = segment.p1.y
      val x4 = segment.p2.x
      val y4 = segment.p2.y

      val bx = x2 - x1
      val by = y2 - y1
      val dx = x4 - x3
      val dy = y4 - y3
      val dot = bx * dy - by * dx

      if(dot == 0) Set[Vector2D]()
      else {
        val cx = x3 - x1
        val cy = y3 - y1
        val t = (cx * dy - cy * dx) / dot
        val u = (cx * by - cy * bx) / dot

        //check that U is between 0 and 1. In reality the absolute tolerance epsilon is used to prevent rounding errors
        //that filter intersections which should exist.
        if (u >= -epsilon && u <= (1 + epsilon)) Set(Vector2D(x1+t*bx, y1+t*by)) else Set[Vector2D]()
      }
    }

    case g => Set()
  }

  def transform(t : TransformationMatrix) = {
    Line2D(p1.transform(t), p2.transform(t))
  }

  def vertices = Seq(p1, p2)

}
object Line2D {

  /**
   * Transforms a list of points into a list of lines, that doesn't
   * close the whole figure... Example:
   * <pre>
   * *    *        *----*
   *           --> |    |
   * *    *        *    *
   *
   * @return List[Line] A list of endless lines
   */
  def linesOnOpenPathOfPoints(points : List[Vector2D]) : List[Line] =
  {
    points match {
      case Nil => Nil
      case point :: Nil => Nil
      case point1 :: point2 :: tail =>
        List(new Line2D(point1, point2)) ::: linesOnOpenPathOfPoints(point2 :: tail)
    }
  }

  /**
   * Transforms a list of points into a list of lines, that
   * closes the whole figure... Example:
   * <pre>
   * *    *        *----*
   *           --> |    |
   * *    *        *----*
   * </pre>@param points  A list of vectors
   * @return List[Line]  A list of endless lines
   */
  def linesOnClosedPathOfPoints(points : List[Vector2D]) : List[Line] =
  {
    points match {
      case Nil => Nil
      case list => linesOnOpenPathOfPoints(list ::: List(list.head))
    }
  }

}