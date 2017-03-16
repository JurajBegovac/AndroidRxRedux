package beg.hr.rxredux.kotlin

import android.os.Bundle
import beg.hr.rxredux.R
import beg.hr.rxredux.kotlin.TimerActivity.State.*
import com.jakewharton.rxbinding.view.RxView
import com.trello.rxlifecycle.android.ActivityEvent
import com.trello.rxlifecycle.components.support.RxAppCompatActivity
import com.trello.rxlifecycle.kotlin.bindUntilEvent
import kotlinx.android.synthetic.main.activity_main.*
import rx.Observable
import java.util.concurrent.TimeUnit

class TimerActivity : RxAppCompatActivity() {

    sealed class State {
        companion object
        class Idle : State()
        class Counting(val count: Int) : State()
        class Paused(val count: Int) : State()
    }

    sealed class Command {
        class Start : Command()
        class Stop : Command()
        class Resume : Command()
        class Pause : Command()
        class Count : Command()
    }

    // reducer
    private fun State.Companion.execute(state: State, command: Command): State {
        when (command) {
            is Command.Start -> if (state is Counting) return state else return Counting(0)
            is Command.Stop -> return Idle()
            is Command.Resume -> if (state is Counting) return state else if (state is Paused) return Counting(state.count) else return Counting(0)
            is Command.Pause -> if (state is Counting) return Paused(state.count) else return state
            is Command.Count -> if (state is Counting) return Counting(state.count + 1) else throw IllegalArgumentException("Here should only be counting")
            else -> throw IllegalArgumentException("Wrong command")
        }
    }

    private fun State.isCounting(): Unit? = if (this is Counting) Unit else null

    private fun State.Companion.initialize(commands: Observable<Command>): Observable<State> {
        val countFeedBack: (Observable<State>) -> Observable<Command> = {
            it.map { it.isCounting() }
                    .distinctUntilChanged()
                    .switchMap {
                        if (it == null)
                            Observable.empty()
                        else
                            Observable.interval(1, TimeUnit.SECONDS)
                                    .map { Command.Count() }
                                    .share()
                    }
        }

        return commands.reduxWithFeedback(initState = Idle(),
                reducer = { state, command -> State.execute(state, command) },
                feedback = countFeedBack)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()
        val userCommands: Observable<Command> = commands()

        val state: Observable<State> = State.initialize(userCommands).share()

        state
                .bindUntilEvent(this, ActivityEvent.STOP)
                .subscribe { render(it) }
    }

    private fun commands(): Observable<Command> {
        val startCommand: Observable<Command> = RxView.clicks(start).share().map { Command.Start() }
        val stopCommand: Observable<Command> = RxView.clicks(stop).share().map { Command.Stop() }
        val resumeCommand: Observable<Command> = RxView.clicks(resume).share().map { Command.Resume() }
        val pauseCommand: Observable<Command> = RxView.clicks(pause).share().map { Command.Pause() }
        return Observable.merge(startCommand, stopCommand, resumeCommand, pauseCommand)
    }

    private fun render(state: State) {
        when (state) {
            is Idle -> timer.text = 0.toString()
            is Counting -> timer.text = state.count.toString()
            is Paused -> timer.text = state.count.toString()
        }
    }
}
