package beg.hr.rxredux.kotlin

import beg.hr.rxredux.kotlin.dummy.dummyController
import beg.hr.rxredux.kotlin.muliti_controllers.parentController
import beg.hr.rxredux.kotlin.timer.timerController
import beg.hr.rxredux.kotlin.util.BaseConductorActivity
import beg.hr.rxredux.kotlin.util.leftRight
import com.bluelinelabs.conductor.RouterTransaction

class MainActivityKotlin : BaseConductorActivity() {
  
  override fun initKey(): String {
    return "Parent"
  }
  
  override fun getRouterTransaction(key: String): RouterTransaction =
      when (key) {
        "Parent" -> RouterTransaction.with(parentController())
        "Dummy" -> RouterTransaction.with(dummyController()).leftRight()
        "Timer" -> RouterTransaction.with(timerController())
        else -> throw IllegalArgumentException("Don't know how to handle key: $key")
      }
}
