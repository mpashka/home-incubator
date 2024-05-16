package org.mpashka.test.scala.scalatra

import com.typesafe.scalalogging.slf4j.StrictLogging
import org.eclipse.jetty.server.handler.HandlerCollection
import org.eclipse.jetty.server.{ConnectionFactory, Connector, Handler, HttpConnectionFactory, Server, ServerConnector}
import org.eclipse.jetty.servlet.DefaultServlet
import org.eclipse.jetty.util.thread.QueuedThreadPool
import org.eclipse.jetty.webapp.WebAppContext
import org.scalatra.LifeCycle
import org.scalatra.servlet.ScalatraListener

import scala.collection.JavaConversions

class MyMain extends StrictLogging {

  private def start(): Unit = {
    logger.info("Starting scalatra servlet...")
    logger.info("Class path")
    JavaConversions.enumerationAsScalaIterator(getClass.getClassLoader.getResources(".")).foreach(c =>
      logger.info(s"    ${c.toString}"))


    val serverContexts = List(
      createWebContext(classOf[ScalatraBootstrap])
    )
    server(serverContexts)
  }

  private def createWebContext(bootstrapClass: Class[_ <: LifeCycle]): WebAppContext = {
    val context = new WebAppContext()
    context.setContextPath("/")
    context.setResourceBase("src/main/webapp")
    context.setDisplayName("agent")
    context.setExtractWAR(false)

    context.setInitParameter(ScalatraListener.LifeCycleKey, bootstrapClass.getName)
    context.addEventListener(new ScalatraListener)
    context.addServlet(classOf[DefaultServlet], "/")
    context
  }

  private def server(contexts: List[WebAppContext]): Unit = {
    val pool = new QueuedThreadPool(5, 3, 3000)
    val server: Server = new Server(pool)
    val port = 8080
    val connector: ServerConnector = new ServerConnector(server)

    connector.setPort(port)
    connector.setReuseAddress(true)

    val connectors = Array[Connector](connector)
    server.setConnectors(connectors)

    val hc = new HandlerCollection()
    val handlers: Array[Handler] = contexts.toArray
    hc.setHandlers(handlers)
    server.setHandler(hc)

    server.start()
  }

}

object MyMain {
  def main(args: Array[String]): Unit = {
    (new MyMain).start()
  }
}