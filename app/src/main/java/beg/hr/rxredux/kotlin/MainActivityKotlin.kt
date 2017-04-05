package beg.hr.rxredux.kotlin

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import beg.hr.rxredux.R
import beg.hr.rxredux.kotlin.MyApp.Companion.appObjectGraph
import beg.hr.rxredux.kotlin.dummy.dummyController
import beg.hr.rxredux.kotlin.muliti_controllers.ParentController
import beg.hr.rxredux.kotlin.util.Flow
import beg.hr.rxredux.kotlin.util.FlowModule
import beg.hr.rxredux.kotlin.util.FlowObjectGraph
import beg.hr.rxredux.kotlin.util.leftRight
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import kotlinx.android.synthetic.main.root.*

class MainActivityKotlin : AppCompatActivity(), Flow {
  
  lateinit var router: Router
  lateinit var flowObjectGraph: FlowObjectGraph
  
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    flowObjectGraph = appObjectGraph
        .flowObjectGraphBuilder()
        .module(FlowModule(this, this))
        .build()
    
    setContentView(R.layout.root)
    
    router = Conductor.attachRouter(this, controller_container, savedInstanceState)
    if (!router.hasRootController()) {
      router.setRoot(getRouterTransaction(initKey()))
    }
  }
  
  private fun initKey(): String {
    return "Parent"
  }
  
  private fun getRouterTransaction(key: String): RouterTransaction {
    when (key) {
      "Parent" -> {
        val parentController = ParentController(null)
        flowObjectGraph.inject(parentController)
        return RouterTransaction.with(parentController)
      }
      "Dummy" -> {
        return RouterTransaction.with(dummyController(flowObjectGraph)).leftRight()
      }
      else -> throw IllegalArgumentException("Don't know how to handle key: $key")
    }
    
  }
  
  override fun onBackPressed() {
    if (!router.handleBack()) finish()
  }
  
  override fun go(key: String) {
    router.pushController(getRouterTransaction(key))
  }
  
  override fun goBack() {
    onBackPressed()
  }
  
  override fun replace(keys: List<String>) {
    router.setBackstack(keys.map { getRouterTransaction(it) }, null)
  }
}
