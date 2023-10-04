package org.mpashka.test.scala.core

import com.typesafe.scalalogging.slf4j.StrictLogging

class StringAddon2(x: String) extends StrictLogging {
  def printMe = logger.info(s"On $x")
}

