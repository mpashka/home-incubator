package org.mpashka.test.scala.core

import com.typesafe.scalalogging.slf4j.StrictLogging
import org.scalactic.source
import org.scalatest.WordSpecLike

import scala.util.Try;

class MyFirstTest extends WordSpecLike with StrictLogging {

  def aaa(implicit pos: source.Position): Unit = {
    logger.info(s"Pos: $pos")
  }

  "my understanding" when {
    "empty" should {
      "have size 0" in {
        assert(Set.empty.size == 0)
        aaa
      }

      "produce NoSuchElementException when head is invoked" in {
        assertThrows[NoSuchElementException] {
          Set.empty.head
        }
      }
    }
  }

  "try test 2" must {
    logger.info("try test")
    val result = Try {
      throw new RuntimeException("Hello exception")
    } recover {
      case iae: IllegalArgumentException => logger.info("IllegalArgumentException")
      case e => logger.info("Other")
        "This is new result"
    }
    logger.info(s"Result: $result")
  }
}
