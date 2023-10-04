package org.mpashka.test.scala.guice

import com.google.inject.name.Names
import com.google.inject.Module
import com.google.inject.util.Modules
import com.google.inject.{AbstractModule, Key, Provides, Singleton}

import javax.inject.Named

class MyContext(
               val myObject: MyObject
               ) extends AbstractModule {


  @Provides
  @Singleton
  @Named("obj2")
  def obj2Val(): MyObject = {
    new MyObject("obj2")
  }

  override def configure(): Unit = {
    bind(classOf[MyObject]).annotatedWith(Names.named("obj1")).toInstance(myObject)
  }
}

object MyContext {
  val obj1: Key[MyObject] = Key.get(classOf[MyObject], Names.named("obj1"))
  val obj2: Key[MyObject] = Key.get(classOf[MyObject], Names.named("obj2"))

  def module(
              myObject: MyObject
            ): Module = {
//    new MyContext(myObject)
    Modules.combine(new MyContext(myObject))
  }
}
