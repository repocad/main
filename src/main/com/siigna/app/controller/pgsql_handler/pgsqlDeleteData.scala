  package com.siigna.app.controller.pgsql_handler

  /**
   * Created by IntelliJ IDEA.
   * User: Niels Egholm
   * Date: 20-02-12
   * Time: 19:42
   * To change this template use File | Settings | File Templates.
   */

  import java.sql._
  import com.siigna.app.model.Model
  import com.siigna.app.model.shape.{PolylineShape, LineShape}
  import com.siigna.util.geom.Vector2D
  import com.siigna.util.collection.Attributes


  //import java.lang.String

  object pgsqlDeleteData {

    def allDrawings () {

      val databaseConnection: Connection = DriverManager.getConnection("jdbc:postgresql://siigna.com/siigna_world","siigna_world_user","s11gn@TUR")
      val createStatement: Statement = databaseConnection.createStatement()
      println ("Sletter alt fra databasen")
      var queryString: String = "delete from drawing"
      createStatement.execute(queryString)
      queryString = "delete from drawing_shape_relation"
      createStatement.execute(queryString)
      queryString = "delete from shape"
      createStatement.execute(queryString)
      queryString = "delete from shape_property_int_relation"
      createStatement.execute(queryString)
       queryString = "delete from property_int"
      createStatement.execute(queryString)
       queryString = "delete from point"
      createStatement.execute(queryString)
       queryString = "delete from shape_point_relation"
      createStatement.execute(queryString)

        //Luk forbindelsen
        databaseConnection.close()

        //Data, der returneres

    }
  }
