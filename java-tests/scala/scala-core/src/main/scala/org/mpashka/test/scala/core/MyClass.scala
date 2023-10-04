package org.mpashka.test.scala.core

import com.typesafe.scalalogging.slf4j.StrictLogging

import scala.reflect.runtime.universe.typeOf
import scala.util.Try

class MyClass extends MyTrait with StrictLogging {

  def testPartialFunctions() {
    Try {

    } recover new PartialFunction[Throwable, Unit] {
      override def isDefinedAt(x: Throwable): Boolean = true

      override def apply(v1: Throwable): Unit = {}
    }

    Try {

    } recover {
      case _ =>
    }

    val x: PartialFunction[Any, Unit] = {
      case _ =>
    }
    logger.info(s"x: $x")
    val y = x(3)
    logger.info(s"y: $y")

  }
}

object MyClass extends StrictLogging {
  def main(args: Array[String]): Unit = {
    val clazz = new MyClass
    logger.info(s"Type of clazz: $clazz, ${clazz.getClass}")
    new MyClass().testPartialFunctions()
  }
}

/*
Illegal inheritance, self-type MyClass2 does not conform to MyClass

class MyClass2 extends MyTrait {

}
*/
