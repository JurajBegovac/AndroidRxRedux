package beg.hr.rxredux.kotlin.util

import android.os.Bundle
import android.support.v4.util.Pair
import beg.hr.kotlindesarrolladorandroid.common.dagger2.ActivityModule
import beg.hr.kotlindesarrolladorandroid.common.dagger2.ActivityObjectGraph
import beg.hr.rxredux.R
import beg.hr.rxredux.kotlin.Direction
import beg.hr.rxredux.kotlin.MyApp.Companion.appObjectGraph
import beg.hr.rxredux.kotlin.goBack
import beg.hr.rxredux.kotlin.replace
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.trello.rxlifecycle.android.ActivityEvent
import com.trello.rxlifecycle.components.support.RxAppCompatActivity
import kotlinx.android.synthetic.main.root.*
import rx.Observable
import rx.android.schedulers.AndroidSchedulers

abstract class BaseConductorActivity : RxAppCompatActivity() {
  
  lateinit protected var router: Router
  lateinit  var objectGraph: ActivityObjectGraph
  
  abstract fun getRouterTransaction(navigationKey: String): RouterTransaction
  
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    objectGraph = appObjectGraph
        .activityObjectGraphBuilder()
        .module(ActivityModule(this))
        .build()
    
    setContentView(R.layout.root)
  
    val store = objectGraph.store()
    
    router = Conductor.attachRouter(this, controller_container, savedInstanceState)
    
    Observable.combineLatest(Observable.just(savedInstanceState), store
        .observeNavigation(), { t1, t2 -> Pair.create(t1, t2) })
        .compose(bindUntilEvent(ActivityEvent.DESTROY))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
          if (isFinishing || isDestroyed) return@subscribe
          val savedState = it.first
          val (navigationKeys, direction) = it.second
          when (direction) {
            Direction.REPLACE -> {
              if (savedState == null)
                router.setBackstack(navigationKeys.map { getRouterTransaction(it) }, null)
            }
            Direction.FORWARD -> {
              val last = navigationKeys.last()
              router.pushController(getRouterTransaction(last))
            }
            Direction.BACK -> if (!router.handleBack()) super.onBackPressed()
          }
        }
  }
  
  override fun onBackPressed() {
    objectGraph.store().dispatch(goBack())
  }
  
  override fun onDestroy() {
    val store = objectGraph.store()
    store.dispatch(replace(store.getState().navigation.navigationKeys))
    super.onDestroy()
  }
}
