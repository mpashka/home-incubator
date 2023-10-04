package org.mpashka.test.scala.scalatra;

import org.json4s._
import org.scalatra.{NoContent, Ok, ScalatraServlet}
import org.scalatra.json.JacksonJsonSupport

import scala.collection.mutable.ArrayBuffer
class MyServlet extends ScalatraServlet with JacksonJsonSupport {
  protected implicit lazy val jsonFormats: Formats = DefaultFormats

  before() {
    contentType = formats("json")
  }

  get("/hello0") {
    <h1>Hello, world0</h1>
  }
  <aaa></aaa>

  get("/") {
    <h1>Hello, world</h1>
  }

  get("/hello1") {
    <h1>Hello,
      {params("name")}
    </h1>
  }

  get("/hello_bytes") {
    val bytes = "Hello world".getBytes().toBuffer
    bytes ++= " and me".getBytes()
    contentType = "application/x-solomon-pb"
    bytes.toArray
  }

  get("/hello_bytes2") {
    contentType = "application/x-solomon-pb"
    response.getOutputStream.write("Hello world".getBytes())
  }


/*
  get("/hello2") {
    views.html.hello()
  }
*/
}
