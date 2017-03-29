package beg.hr.rxredux.kotlin.timer

import beg.hr.rxredux.kotlin.timer.State.*
import beg.hr.rxredux.kotlin.util.reduxWithFeedback
import beg.hr.rxredux.kotlin.util.ui.ViewDriverComponent
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

/**
 * Created by juraj on 24/03/2017.
 */
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
fun reduce(state: State, command: Command): State {
    when (command) {
        is Command.Start -> if (state is Counting) {
            return state
        } else return Counting(0)
        is Command.Stop -> return Idle()
        is Command.Resume -> if (state is Counting) return state else if (state is Paused) return Counting(state.count) else return Counting(0)
        is Command.Pause -> if (state is Counting) return Paused(state.count) else return state
        is Command.Count -> if (state is Counting) return Counting(state.count + 1) else throw IllegalArgumentException("Here should only be counting")
        else -> throw IllegalArgumentException("Wrong command")
    }
}

fun State.isCounting(): Unit? = if (this is Counting) Unit else null

fun initialize(commands: Observable<Command>): Observable<State> {
    val countFeedBack: (Observable<State>) -> Observable<Command> = {
        it.map(State::isCounting)
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

    return commands.reduxWithFeedback(
            Idle(),
            ::reduce,
            AndroidSchedulers.mainThread(),
            countFeedBack
    )
}

class TimerComponent(driver: TimerDriver) : ViewDriverComponent<State, TimerView, Command, TimerDriver>(driver) {
    init {
        driver.loadEvent().subscribe { onAttach() }
    }

    private fun onAttach() {
        initialize(driver.commands())
                .compose(driver.bindUntilDetach())
                .subscribe { driver.render(it) }
    }
}
