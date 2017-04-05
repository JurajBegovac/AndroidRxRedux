package beg.hr.rxredux.kotlin

import beg.hr.rxredux.kotlin.util.reduxWithFeedback
import rx.Observable
import rx.schedulers.Schedulers
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import javax.inject.Singleton

/**
 * Created by juraj on 04/04/2017.
 */

/**
 * State
 */
val initialState = State(listOf(Navigation("Parent")))

data class State(val navigation: List<Navigation>) {
  companion object
}

data class Navigation(val navigationKey: String)

fun State.Companion.initialize(initState: State, commands: Observable<Action>): Observable<State> {
  val finishFeedback: (Observable<State>) -> Observable<Action> = {
    it.map(State::navigation)
        .map { it.last().navigationKey }
        .distinctUntilChanged()
        .filter { it == "Last" }
        .map {
          Action(ActionTypes.NAVIGATION, Any())
        }
  }
  return commands.reduxWithFeedback(initState,
                                    ::reduce,
                                    Schedulers.computation(),
                                    finishFeedback)
}

/**
 * Action
 */
data class Action(val type: String, val payload: Any)

/**
 * Action types
 */
class ActionTypes {
  companion object {
    val NAVIGATION: String = "NAVIGATION"
  }
}

/**
 * Navigation actions creators
 */
fun go(navigationKey: String): Action = Action(ActionTypes.NAVIGATION, Navigation(navigationKey))

fun goBack(): Action = Action(ActionTypes.NAVIGATION, Any())

/**
 * Store
 */
@Singleton
class Store(initState: State) {
  
  private val stateObservable: BehaviorSubject<State> = BehaviorSubject.create(initState)
  private val actions: PublishSubject<Action> = PublishSubject.create()
  
  init {
    State.initialize(initState, actions).subscribe { stateObservable.onNext(it) }
  }
  
  fun dispatch(action: Action) {
    actions.onNext(action)
  }
  
  fun getState(): State = stateObservable.value
  
  fun observe(): Observable<State> = stateObservable.asObservable()
  
  fun observeNavigation(): Observable<Navigation> =
      observe().map { it.navigation.last() }
          .distinctUntilChanged()
          .skip(1)
}

/**
 * Reducers
 */
fun reduce(state: State, action: Action): State = State(navigation(state.navigation, action))

fun navigation(state: List<Navigation>, action: Action): List<Navigation> {
  when (action.type) {
    ActionTypes.NAVIGATION -> {
      if (action.payload is Navigation)
        return state.plusElement(action.payload)
      else {
        if (state.size == 1) return state.plusElement(Navigation("Last"))
        return state.minusElement(state.last())
      }
    }
    else -> return state
  }
}
