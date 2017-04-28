package beg.hr.rxredux.kotlin.timer

import android.util.Log
import beg.hr.rxredux.kotlin.MyApp.Companion.appObjectGraph
import beg.hr.rxredux.kotlin.timer.State.*
import beg.hr.rxredux.kotlin.util.system
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

fun State.Companion.reduce(state: State, command: Command): State =
    when (command) {
      is Command.Start -> state as? Counting ?: Counting(0)
      is Command.Stop -> Idle()
      is Command.Resume -> state as? Counting ?: if (state is Paused) Counting(
          state.count) else Counting(0)
      is Command.Pause -> if (state is Counting) Paused(state.count) else state
      is Command.Count -> if (state is Counting) Counting(state.count + 1) else
        throw IllegalArgumentException("Here should only be counting")
      is Command.Navigation -> Navigation(command.key)
    }

fun State.Companion.initialize(commands: Observable<Command>,
                               initState: State): Observable<State> {
  val countFeedBack: (Observable<State>) -> Observable<Command> = {
    it.map { it is Counting }
        .distinctUntilChanged()
        .switchMap {
          Log.i("Juraj", it.toString())
          if (it)
            Observable.interval(1, TimeUnit.SECONDS)
                .map { Command.Count() }
                .share()
          else
            Observable.empty()
        }
  }
  
  val autoStartFeedback: (Observable<State>) -> Observable<Command> = {
    it.map { it is Idle }
        .distinctUntilChanged()
        .switchMap {
          if (it)
            appObjectGraph.timerService().autoStart().map { Command.Start() }.share()
          else
            Observable.empty()
        }
  }
  
  return system(initState,
                { state, command -> State.reduce(state, command) },
                AndroidSchedulers.mainThread(),
                { commands },
                countFeedBack,
                autoStartFeedback)
}

fun timerController() = TimerController(null)
