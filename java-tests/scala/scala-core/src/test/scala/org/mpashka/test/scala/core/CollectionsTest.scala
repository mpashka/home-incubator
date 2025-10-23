package org.mpashka.test.scala.core

import com.typesafe.scalalogging.slf4j.StrictLogging
import org.scalatest.WordSpecLike

class CollectionsTest extends WordSpecLike with StrictLogging {
  "collection" must {
    val original = Map("aaa"-> List("a1", "a2"))
    val swapped = original.flatMap((tuple: (String, List[String])) => {
        val key = tuple._1
        val values = tuple._2
        values.map(v => (v, key))
      })
    logger.info("Swapped: {}", swapped)
  }
}
