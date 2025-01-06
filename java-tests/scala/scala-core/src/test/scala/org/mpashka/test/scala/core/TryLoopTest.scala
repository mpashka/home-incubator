package org.mpashka.test.scala.core

import com.typesafe.scalalogging.slf4j.StrictLogging
import org.scalatest.WordSpecLike

import scala.util.{Failure, Try}

class TryLoopTest extends WordSpecLike with StrictLogging {

  "check loop" must {
    def myMethod(in: String): String = {
      logger.info(s"Call myMethod with $in")
      "out:" + in
      throw new Exception("Err from " + in)
    }

    type :=>[A, B] = PartialFunction[A, B]

    def myRecover[T](): Throwable :=> Failure[T] = {
      case e => logger.info("Recover from", e)
        Failure[T](e)
    }


      val res = (for {
        var1 <- Try(myMethod("in1"))
        var2 <- Try(myMethod("in2"))
      } yield ()).recoverWith(myRecover())

      logger.info(s"res: $res")
      res.get
  }


}
