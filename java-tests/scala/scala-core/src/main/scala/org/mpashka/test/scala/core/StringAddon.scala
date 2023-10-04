package org.mpashka.test.scala.core

import com.typesafe.scalalogging.slf4j.StrictLogging

trait StringAddon extends StrictLogging { thisAaaSuite =>
  implicit def stringToString(s: String) = new StringAddon2(s)
  def myStrMethod(): Unit = {
    logger.info(s"This: $thisAaaSuite")
    logger.info(s"Another this: $this")
  }
}
