package org.mpashka.test.scala.scalatra;

import com.typesafe.scalalogging.slf4j.StrictLogging
import org.json4s._
import org.scalatra.json.JacksonJsonSupport
import org.scalatra.{MatchedRoute, ScalatraServlet}
class MyServlet extends ScalatraServlet with JacksonJsonSupport with StrictLogging {
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

  get("/my/:hello") {
    logger.info(s"My called: ${params("hello")}")
    logger.info(s"Routes: $routes")
    logger.info(s"Request: $request")
    logger.info(s"Servlet URI: ${request.getServletPath}")
    logger.info(s"Request URI: ${request.getRequestURI}")
    logger.info(s"Path: $requestPath")
/*
    request.get("org.scalatra.MatchedRoute") match {
      case Some(r: MatchedRoute) =>
        logger.info(s"Route: $r")
        r.multiParams.foreach()
    }
*/
    var uri = request.getRequestURI
    // Remove parameters values from uri
    request.get("org.scalatra.MatchedRoute") match {
      case Some(r: MatchedRoute) =>
        r.multiParams.foreach(param => {
          val (key: String, values: Seq[String]) = param
          values.foreach(value => uri = uri.replace(value, s":$key"))
        })
    }
    logger.info(s"Transformer uri: $uri")
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


  get("/hello3/") {
    logger.info(s"Request: $request")
    logger.info(s"Servlet URI: ${request.getServletPath}")
    logger.info(s"Request URI: ${request.getRequestURI}")
    logger.info(s"Path: $requestPath")
  }
}
