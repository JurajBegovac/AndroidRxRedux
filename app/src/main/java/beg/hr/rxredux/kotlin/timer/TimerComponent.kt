package beg.hr.rxredux.kotlin.timer

import beg.hr.rxredux.kotlin.MyApp.Companion.appObjectGraph
import beg.hr.rxredux.kotlin.timer.State.*
import beg.hr.rxredux.kotlin.util.reduxWithFeedback
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import java.io.Serializable
import java.util.concurrent.TimeUnit

/**
 * Created by juraj on 24/03/2017.
 */
sealed class State : Serializable {
  companion object
  class Idle : State()
  class Counting(val count: Int) : State()
  class Paused(val count: Int) : State()
  class Navigation(val key: String) : State()
}

sealed class Command {
  class Start : Command()
  class Stop : Command()
  class Resume : Command()
  class Pause : Command()
  class Count : Command()
  class Navigation(val key: String) : Command()
}

// reducer
fun State.Companion.reduce(state: State, command: Command): State {
  when (command) {
    is Command.Start -> if (state is Counting) {
      return state
    } else return Counting(0)
    is Command.Stop -> return Idle()
    is Command.Resume -> if (state is Counting) return state else if (state is Paused) return Counting(
        state.count) else return Counting(0)
    is Command.Pause -> if (state is Counting) return Paused(state.count) else return state
    is Command.Count -> if (state is Counting) return Counting(state.count + 1) else throw IllegalArgumentException(
        "Here should only be counting")
    is Command.Navigation -> return Navigation(command.key)
    else -> throw IllegalArgumentException("Wrong command")
  }
}

fun State.isCounting(): Unit? = if (this is Counting) Unit else null

fun State.isIdle(): Unit? = if (this is Idle) Unit else null

fun State.Companion.initialize(commands: Observable<Command>, initState: State): Observable<State> {
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
  
  val autoStartFeedback: (Observable<State>) -> Observable<Command> = {
    it.map(State::isIdle)
        .distinctUntilChanged()
        .switchMap {
          if (it == null) Observable.empty()
          else
            appObjectGraph.timerService().autoStart().map { Command.Start() }.share()
        }
  }
  
  return commands.reduxWithFeedback(
      initState,
      this::reduce,
      AndroidSchedulers.mainThread(),
      countFeedBack, autoStartFeedback
  )
}

fun timerController() = TimerController(null)
