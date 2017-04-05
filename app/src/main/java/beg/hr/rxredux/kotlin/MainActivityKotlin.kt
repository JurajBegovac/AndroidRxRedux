package beg.hr.rxredux.kotlin

import android.os.Bundle
import beg.hr.kotlindesarrolladorandroid.common.dagger2.ActivityModule
import beg.hr.kotlindesarrolladorandroid.common.dagger2.ActivityObjectGraph
import beg.hr.rxredux.R
import beg.hr.rxredux.kotlin.MyApp.Companion.appObjectGraph
import beg.hr.rxredux.kotlin.dummy.dummyController
import beg.hr.rxredux.kotlin.muliti_controllers.ParentController
import beg.hr.rxredux.kotlin.timer.timerController
import beg.hr.rxredux.kotlin.util.leftRight
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.trello.rxlifecycle.android.ActivityEvent
import com.trello.rxlifecycle.components.support.RxAppCompatActivity
import kotlinx.android.synthetic.main.root.*
import rx.android.schedulers.AndroidSchedulers

class MainActivityKotlin : RxAppCompatActivity() {
  
  lateinit var router: Router
  lateinit var objectGraph: ActivityObjectGraph
  
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    objectGraph = appObjectGraph
        .activityObjectGraphBuilder()
        .module(ActivityModule(this))
        .build()
    
    setContentView(R.layout.root)
    
    router = Conductor.attachRouter(this, controller_container, savedInstanceState)
    
    if (savedInstanceState == null)
      router.setBackstack(appObjectGraph.store().getState().navigation.map {
        getRouterTransaction(it.navigationKey)
      }, null)
    
    appObjectGraph.store()
        .observeNavigation()
        .compose(bindUntilEvent(ActivityEvent.DESTROY))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
          val navigationKey = it.navigationKey
          if (navigationKey == "Last") {
            super.onBackPressed()
            return@subscribe
          }
          val controller = router.getControllerWithTag(navigationKey)
          if (controller != null) {
            router.popToTag(navigationKey)
          } else {
            router.pushController(getRouterTransaction(navigationKey))
          }
        }
  }
  
  private fun getRouterTransaction(navigationKey: String): RouterTransaction {
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
  
  override fun onBackPressed() {
    appObjectGraph.store().dispatch(goBack())
  }
}
