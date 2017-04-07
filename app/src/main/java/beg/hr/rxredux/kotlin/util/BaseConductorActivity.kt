package beg.hr.rxredux.kotlin.util

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import beg.hr.rxredux.R
import beg.hr.rxredux.kotlin.MyApp
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import kotlinx.android.synthetic.main.root.*

/**
 * Created by juraj on 07/04/2017.
 */
abstract class BaseConductorActivity : AppCompatActivity(), Flow {
  
  abstract fun initKey(): String
  abstract fun getRouterTransaction(key: String): RouterTransaction
  
  lateinit protected var router: Router
  
  lateinit var flowObjectGraph: FlowObjectGraph
  
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    flowObjectGraph = MyApp.appObjectGraph
        .flowObjectGraphBuilder()
        .module(FlowModule(this, this))
        .build()
    
    setContentView(R.layout.root)
    
    router = Conductor.attachRouter(this, controller_container, savedInstanceState)
    if (!router.hasRootController()) {
      router.setRoot(getRouterTransaction(initKey()))
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