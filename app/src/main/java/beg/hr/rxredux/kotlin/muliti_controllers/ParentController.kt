package beg.hr.rxredux.kotlin.muliti_controllers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import beg.hr.kotlindesarrolladorandroid.common.dagger2.ActivityObjectGraph
import beg.hr.rxredux.R
import beg.hr.rxredux.kotlin.dummy.dummyController
import beg.hr.rxredux.kotlin.timer.timerController
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.rxlifecycle.RxController
import javax.inject.Inject

/**
 * Created by juraj on 03/04/2017.
 */
class ParentController(args: Bundle? = null) : RxController(args) {
  
  @Inject lateinit var objectGraph: ActivityObjectGraph
  
  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View =
      inflater.inflate(R.layout.view_multi, container, false)
  
  override fun onAttach(view: View) {
    super.onAttach(view)
    addChild(getChildRouter(view.container(R.id.container_1)), timerController(objectGraph))
    addChild(getChildRouter(view.container(R.id.container_2)), dummyController(objectGraph))
  }
  
  private fun addChild(childRouter: Router, controller: Controller) {
    if (!childRouter.hasRootController()) {
      childRouter.setRoot(RouterTransaction.with(controller))
    }
  }
  
  // extension function for view
  fun View.container(id: Int): ViewGroup = this.findViewById(id) as ViewGroup
}