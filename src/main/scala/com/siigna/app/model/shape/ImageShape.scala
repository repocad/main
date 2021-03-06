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

package com.siigna.app.model.shape

import java.awt.{Color, Graphics2D, Image, RenderingHints, Toolkit}
import java.awt.image.{BufferedImage, MemoryImageSource, PixelGrabber}

import com.siigna.util.collection.Attributes
import com.siigna.util.geom.{ComplexRectangle2D, SimpleRectangle2D, TransformationMatrix, Vector2D}
import com.siigna.app.model.selection.{EmptyShapeSelector, FullShapeSelector, ShapeSelector}
import com.siigna.app.Siigna

/**
 * An ImageShape.
 * TODO: Refactor and incoorporate a specific width and height.
 *
 * @param image  An image given in an array of pixel data
 * @param p1     The first point in a rectangle, defining the virtual coordinates of the image
 * @param p2     The second point in a rectangle, defining the virtual coordinates of the image
 * @param width  The actual dimension of the image; should equal the width of the pixel-array
 * @param height  The actual dimension of the image; should equal the height of the pixel-array
 * @param attributes  The attributes of the shape
 *
 * TODO: Refactor the imageDimension to another type than rectangle. Preferably one that can rotate.
 */
case class ImageShape(image : Array[Int], p1 : Vector2D, p2 : Vector2D, width : Int, height : Int, attributes : Attributes) extends Shape {

  type T = ImageShape

  val geometry = SimpleRectangle2D(p1.x,p1.y, p2.x,p2.y)

  val points = Iterable(p1, p2)

  def setAttributes(attributes : Attributes) = new ImageShape(image, p1, p2, width, height, attributes)

  def delete(part: ShapeSelector) = part match {
    case FullShapeSelector => Nil
    case _ => Seq(this)
  }

  def getPart(part : ShapeSelector) = Some(new PartialShape(this, transform))

  def getSelectedAndUnselectedParts(part : ShapeSelector) = (Traversable(new PartialShape(this, transform)),Traversable())

  def getSelector(rect: SimpleRectangle2D) = {
    if (rect.intersects(geometry)) {
      ShapeSelector(1)
      FullShapeSelector
    }
    else {
      ShapeSelector(0)
      EmptyShapeSelector
    }
  }

  def getSelector(point: Vector2D) = {
    val selectionDistance = Siigna.selectionDistance
    if (distanceTo(point) < selectionDistance) {
      ShapeSelector(1)
      FullShapeSelector
    }
    else {
      ShapeSelector(0)
      EmptyShapeSelector
    }
  }

  def getShape(s : ShapeSelector) = Some(this)

  def getVertices(selector: ShapeSelector) = Nil


  /**
   * Retrieves the ImageShape as an Image.
   */
  def toImage : Image = {
    val imageSource = new MemoryImageSource(width, height, image, 0, width)
    val toolkit     = Toolkit.getDefaultToolkit()
    toolkit.createImage(imageSource)
  }

  def transform(transformation : TransformationMatrix) : ImageShape =
  {
    ImageShape(image,
               p1 transform(transformation),
               p2 transform(transformation),
               width, height,
               attributes)
  }

  def fromImage(image : Image, p1 : Vector2D, p2 : Vector2D) : ImageShape =
    //fromImage(image, p1, p2, image.getWidth(null), image.getHeight(null))
    fromImage(image,p1,p2)

  //def fromImage(image : Image, p1 : Vector2D, p2 : Vector2D, width : Int, height : Int) : ImageShape =
  //  fromImage(image, p1, p2, width, height, Attributes())

}

object ImageShape {

  /**
   * Creates an ImageShape from a given image.
   */
  def fromImage(highResImage : Image, p1 : Vector2D, p2 : Vector2D, width : Int, height : Int, attributes : Attributes) : ImageShape = {
    val widthToHeightRatio = width.toDouble / height.toDouble // VERY important that this is a double
    val newWidth  = if (width > 0 && width <= 50) width else 50
    val newHeight = if (width > 0 && width <= 50) height else (newWidth.toDouble / widthToHeightRatio).toInt // r = w/h <=> r * h = w <=> h = w / r

    val image = highResImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH)
    var pixels = (1 to newWidth * newHeight).toArray
    val grabber = new PixelGrabber(image, 0, 0, newWidth, newHeight, pixels, 0, newWidth)

    // Get the pixels and save them in pixels.
    grabber.grabPixels

    // Return an ImageShape
    new ImageShape(pixels, p1, p2, newWidth, newHeight, attributes)
  }
  def apply(image : BufferedImage, p1 : Vector2D, p2 : Vector2D) : ImageShape =
    fromImage(image, p1, p2, image.getWidth, image.getHeight, Attributes())

  def apply(image : Array[Int], p1 : Vector2D, p2 : Vector2D, width : Int, height : Int) : ImageShape =
    ImageShape(image, p1, p2, width, height, Attributes())

}




  /*

  /**
   * Creates an ImageShape from a given shape by server it with the <code>Graphics</code>
   * class and saving the result.
   * TODO: Refactor!
   */
  def fromShape(rawShape : HasAttributes) = {
    // Force the shape to draw from (0, 0) on the canvas.
    // Uses bottomLeft, since the shape is being flipped when drawn.
    val distanceToZero = - rawShape.boundary.bottomLeft
    val shape          = rawShape.transform(TransformationMatrix(distanceToZero, 1))
    val boundary       = shape boundary

    // Gets the images to paint on.
    val bufferedImage  = new BufferedImage(boundary.width.toInt, boundary.height.toInt, BufferedImage TYPE_INT_RGB)
    val graphics2D     = bufferedImage.getGraphics.asInstanceOf[Graphics2D]
    val graphics       = new Graphics(graphics2D)

    // Enable anti-alising.
    graphics2D setRenderingHint(RenderingHints KEY_ANTIALIASING, RenderingHints VALUE_ANTIALIAS_ON)

    // Clear the server area.
    graphics2D setBackground(Color white)
    graphics2D clearRect(0, 0, boundary.width.toInt, boundary.height.toInt)

    // Draw the shape.
    graphics draw shape

    fromImage(bufferedImage, rawShape.boundary.topLeft, rawShape.boundary.bottomRight, boundary.width.toInt, boundary.height.toInt, shape.attributes)
  }
  */
