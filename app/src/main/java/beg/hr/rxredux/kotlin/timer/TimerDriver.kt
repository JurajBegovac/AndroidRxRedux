package beg.hr.rxredux.kotlin.timer

import beg.hr.rxredux.kotlin.util.ui.ViewDriver
import com.jakewharton.rxbinding.view.RxView
import com.trello.rxlifecycle.LifecycleProvider
import com.trello.rxlifecycle.android.ActivityEvent
import rx.Observable

/**
 * Created by juraj on 29/03/2017.
 */
class TimerDriver(view: TimerView, lifecycleProvider: LifecycleProvider<ActivityEvent>) : ViewDriver<TimerView, State, Command>(view, lifecycleProvider) {

    override fun render(state: State) {
        when (state) {
            is State.Idle -> view.timer().text = 0.toString()
            is State.Counting -> view.timer().text = state.count.toString()
            is State.Paused -> view.timer().text = state.count.toString()
        }
    }

    override fun commands(): Observable<Command> {
        val startCommand: Observable<Command> = RxView.clicks(view.start()).share().map { Command.Start() }
        val stopCommand: Observable<Command> = RxView.clicks(view.stop()).share().map { Command.Stop() }
        val resumeCommand: Observable<Command> = RxView.clicks(view.resume()).share().map { Command.Resume() }
        val pauseCommand: Observable<Command> = RxView.clicks(view.pause()).share().map { Command.Pause() }
        return Observable.merge(startCommand, stopCommand, resumeCommand, pauseCommand)
    }
}
