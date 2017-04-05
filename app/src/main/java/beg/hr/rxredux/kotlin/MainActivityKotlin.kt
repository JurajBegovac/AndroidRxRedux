package beg.hr.rxredux.kotlin

import beg.hr.rxredux.kotlin.dummy.dummyController
import beg.hr.rxredux.kotlin.muliti_controllers.ParentController
import beg.hr.rxredux.kotlin.timer.timerController
import beg.hr.rxredux.kotlin.util.BaseConductorActivity
import beg.hr.rxredux.kotlin.util.leftRight
import com.bluelinelabs.conductor.RouterTransaction

class MainActivityKotlin : BaseConductorActivity() {
  override fun getRouterTransaction(navigationKey: String): RouterTransaction {
    if (navigationKey == "Parent") {
      val parentController = ParentController(null)
      objectGraph.inject(parentController)
      return RouterTransaction.with(parentController).tag(
          navigationKey)
    } else if (navigationKey == "Dummy") return RouterTransaction.with(dummyController(objectGraph)).leftRight().tag(
        navigationKey)
    else if (navigationKey == "Timer") return RouterTransaction.with(timerController(objectGraph)).tag(
        navigationKey)
    else throw IllegalArgumentException("Don't know how to handle key")
  }
}
