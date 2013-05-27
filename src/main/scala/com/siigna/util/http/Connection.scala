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

package com.siigna.util.http

import java.net.{HttpURLConnection, URL}
import java.io.{InputStreamReader, BufferedReader}
import com.siigna.app.controller.remote.RemoteCommand

//

/**
 * Class used for communicating with the siigna HTTP server
 */
class Connection(val url: String) {

  val destination = new URL(url)

  def send(message:Array[Byte]) = {
    val con = destination.openConnection.asInstanceOf[HttpURLConnection]

    con.setDoInput(true)
    con.setDoOutput(true)
    con.setUseCaches(false)

    val out = con.getOutputStream

    try{
      out.write(message)
      out.flush

      if (con.getResponseCode == 200)
        con.getInputStream
      else
        con.getErrorStream

    } finally {
      out.close
    }
  }

}

object Connection{
  def decode(stream: java.io.InputStream):Array[Byte] = {

    try {

      val reader = new BufferedReader(new InputStreamReader(stream))
      val request = reader.readLine() // Read the request line
      // val token = request.substring(6, request.size) // Cut off the initial 6 characters (do they mean anyting??)
      //      raw      parsed   unmarshalled
      println(com.siigna.util.io.Unmarshal[RemoteCommand](request.getBytes("UTF-8")))
      request.getBytes("UTF-8")

    } catch {
      case e : Throwable => println("Fail: " + e)
      new Array[Byte](0)
    }
  }
 /*   var in = Array[Byte]()

    var data = stream.read(in).asInstanceOf[Byte]

    while( data != -1){
      in = data +: in
      data = stream.read().asInstanceOf[Byte]
    }

    return in
  }                */
}
