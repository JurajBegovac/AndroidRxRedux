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
      val parentController = ParentController(null)
      flowObjectGraph.inject(parentController)
      router.setRoot(RouterTransaction.with(parentController))
    }
  }
  
  override fun onBackPressed() {
    if (!router.handleBack()) super.onBackPressed()
  }
  
  override fun go(key: String) {
    when (key) {
      "Dummy" -> pushDummy()
    }
  }
  
  override fun goBack() {
    onBackPressed()
  }
  
  private fun pushDummy() {
    router.pushController(RouterTransaction.with(dummyController(flowObjectGraph)).leftRight())
  }
  
}
