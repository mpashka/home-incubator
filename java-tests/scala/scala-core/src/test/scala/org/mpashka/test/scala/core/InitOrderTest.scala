package org.mpashka.test.scala.core

import com.typesafe.scalalogging.slf4j.StrictLogging
import org.scalatest.WordSpecLike

class InitOrderTest extends WordSpecLike with StrictLogging {

  trait MyTrait {
    protected val myVal:String
    protected val myValConst:String = s"^${myVal}|"
    private lazy val myValLazy: String = myVal
    private lazy val myValDLazy: String = computeDerivative(myVal)
    private lazy val myValLazyD: String = computeDerivative(myValLazy)

    def result:Map[String,String] = {
      Map("v" -> myVal, "vc" -> myValConst, "vl" -> myValLazy, "vdl" -> myValDLazy, "vld" -> myValLazyD)
    }

    private def computeDerivative(in:String) = "_" + in + "_"
  }

  abstract class MyAbstractClass {
  }

  class MyClass extends MyTrait {
    override protected val myVal: String = "my_val"
  }

  class MyClassParam(override protected val myVal: String) extends MyTrait {
  }

  class MyClassParamD(val myInputVal: String) extends MyTrait {
    override protected val myVal: String = s"Deriv: $myInputVal"
  }

  class MyClassParamDEarly(val myInputVal: String) extends {
    override protected val myVal: String = s"EarlyDeriv'$myInputVal''"
  } with MyTrait

  "check init order" must {
    {
      val m = new MyClass
      logger.info(s"MyClass: ${m.result}")
    }

    {
      val mParam = new MyClassParam("my_param")
      logger.info(s"MyClassParam: ${mParam.result}")
    }

    {
      val mParamD = new MyClassParamD("my_param")
      logger.info(s"MyClassParamDerivative: ${mParamD.result}")
    }

    {
      val mParamE = new MyClassParamDEarly("my_param")
      logger.info(s"MyClassEarly: ${mParamE.result}")
    }

  }
}
