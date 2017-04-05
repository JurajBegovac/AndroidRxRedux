package beg.hr.rxredux.kotlin.dummy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import beg.hr.rxredux.R
import beg.hr.rxredux.kotlin.Store
import beg.hr.rxredux.kotlin.goBack
import beg.hr.rxredux.kotlin.util.BaseConductorActivity
import com.bluelinelabs.conductor.rxlifecycle.ControllerEvent
import com.bluelinelabs.conductor.rxlifecycle.RxController
import com.jakewharton.rxbinding.view.RxView
import javax.inject.Inject

/**
 * Created by juraj on 30/03/2017.
 */
class DummyController(args: Bundle? = null) : RxController(args) {
  
  @Inject lateinit var store: Store
  
  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View =
      inflater.inflate(R.layout.view_dummy, container, false)
  
  override fun onAttach(view: View) {
    super.onAttach(view)
    val dummyView: DummyView = view as DummyView
    RxView.clicks(dummyView.back())
        .compose(bindUntilEvent(ControllerEvent.DETACH))
        .subscribe {
          val baseConductorActivity = activity as BaseConductorActivity
          baseConductorActivity.objectGraph.store().dispatch(goBack())
        }
  }
}
