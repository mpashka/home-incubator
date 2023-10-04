package org.mpashka.test.scala.core

import com.typesafe.scalalogging.slf4j.StrictLogging
import org.scalactic.source

object MyFirstScala extends StrictLogging with StringAddon {

  def aaa(implicit pos: source.Position): Unit = {
    logger.info(s"Pos: $pos")
  }

  def bbb(str: String): Unit = logger.info(s"Fn bbb $str")
  def bbb2: Unit = logger.info(s"Fn bbb2")

  def strTest(): Unit = {
    logger.info(s"This: $this")
    myStrMethod()
  }

  def main(args: Array[String]): Unit = {
    logger.info("Main!")
    aaa
    bbb("Var1")
//    bbb "Var2"
    bbb2
    strTest()

    "aaa".printMe
  }

}
