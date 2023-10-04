package org.mpashka.test.scala.guice

import com.google.inject.Guice
import com.google.inject.util.Modules
import com.typesafe.scalalogging.slf4j.StrictLogging

object MyApp extends StrictLogging {
  def main(args: Array[String]): Unit = {
/*
    Modules.`override`(Modules.combine(
    )).`with`(
    )
*/

    val module = MyContext.module(new MyObject("name1"))
    val injector = Guice.createInjector(
      module)

//    logger.info(s"Object: ${injector.getInstance(classOf[MyObject])}")
    logger.info(s"Object(obj1): ${injector.getInstance(MyContext.obj1)}")
    logger.info(s"Object(obj2): ${injector.getInstance(MyContext.obj2)}")
  }
}
