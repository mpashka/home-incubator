package org.mpashka.test.scala.core

import com.typesafe.scalalogging.slf4j.StrictLogging
import org.mpashka.test.scala.core.FunctionConverters.:=>
import org.mpashka.test.scala.core.TransitionUtils.For
import org.scalatest.WordSpecLike

import scala.reflect.ClassTag

class MySecondTest extends WordSpecLike with StrictLogging {

  type StateToUnit = (String) => Unit

  protected val set: String :=> String = {
    case newState =>
      logger.info(s"set called: $newState")
      "after_set"
  }
  protected val toActive: StateToUnit = state => {
    logger.info(s"toActive $state")
  }

/*
  "check for, partial" must {
    logger.info("try test")
    For("for_arg")(set andThen toActive)
  }
*/

  "check java implicit" must {
    val myTrait2: MyTrait2 = new MyTrait2Impl
    logger.info(s"var1 = ${myTrait2.myVar}")
    myTrait2.myVar = "aaa"
    logger.info(s"var2 = ${myTrait2.myVar}")
  }

}

object TransitionUtils {
  @inline
  def For[T: ClassTag](state: T)(f: PartialFunction[T, Unit]): Unit = f(state)
}

object FunctionConverters {
  type :=>[A, B] = PartialFunction[A, B]
}
