package org.mpashka.test.scala.scalatra

import com.typesafe.scalalogging.slf4j.StrictLogging
import org.scalatra._

import javax.servlet.ServletContext

class ScalatraBootstrap extends LifeCycle with StrictLogging {
  override def init(context: ServletContext) {
    logger.info("Init")
    context mount(new MyServlet, "/*", "myServlet")
  }
}
