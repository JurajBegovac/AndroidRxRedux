package beg.hr.rxredux.kotlin.timer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import beg.hr.rxredux.R
import beg.hr.rxredux.kotlin.util.BaseConductorActivity
import beg.hr.rxredux.kotlin.util.Flow
import com.bluelinelabs.conductor.rxlifecycle.ControllerEvent
import com.bluelinelabs.conductor.rxlifecycle.RxController
import com.jakewharton.rxbinding.view.RxView
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import javax.inject.Inject

/**
 * Created by juraj on 30/03/2017.
 */
class TimerController(args: Bundle? = null) : RxController(args) {
  
  companion object {
    val KEY = "key:timercontroller"
  }
  
  @Inject lateinit var flow: Flow
  
  var state: State = State.Idle()
  
  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
    (activity as BaseConductorActivity).flowObjectGraph.inject(this)
    return inflater.inflate(R.layout.view_timer, container, false)
  }
  
  override fun onAttach(view: View) {
    super.onAttach(view)
    State.initialize(commands(), state)
        .compose(bindUntilEvent(ControllerEvent.DETACH))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
          if (it is State.Navigation) flow.go(it.key)
          else render(it)
        }
  }
  
  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putSerializable(KEY, state)
  }
  
  override fun onRestoreInstanceState(savedInstanceState: Bundle) {
    super.onRestoreInstanceState(savedInstanceState)
    state = savedInstanceState.getSerializable(KEY) as State
  }
  
  private fun commands(): Observable<Command> {
    val timerView = view as TimerView
    val startCommand: Observable<Command> = RxView.clicks(timerView.start()).share().map { Command.Start() }
    val stopCommand: Observable<Command> = RxView.clicks(timerView.stop()).share().map { Command.Stop() }
    val resumeCommand: Observable<Command> = RxView.clicks(timerView.resume()).share().map { Command.Resume() }
    val pauseCommand: Observable<Command> = RxView.clicks(timerView.pause()).share().map { Command.Pause() }
    val dummyCommand: Observable<Command> = RxView.clicks(timerView.dummy()).share().map {
      Command.Navigation("Dummy")
    }
    return Observable.merge(startCommand, stopCommand, resumeCommand, pauseCommand, dummyCommand)
  }
  
  private fun render(state: State) {
    this.state = state
    val timerView = view as TimerView
    when (state) {
      is State.Idle -> timerView.timer().text = 0.toString()
      is State.Counting -> timerView.timer().text = state.count.toString()
      is State.Paused -> timerView.timer().text = state.count.toString()
    }
  }
}
