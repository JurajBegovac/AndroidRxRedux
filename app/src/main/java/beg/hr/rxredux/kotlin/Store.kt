package beg.hr.rxredux.kotlin

import beg.hr.kotlindesarrolladorandroid.common.dagger2.PerActivity
import beg.hr.rxredux.kotlin.util.reduxWithFeedback
import rx.Observable
import rx.schedulers.Schedulers
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

/**
 * Created by juraj on 04/04/2017.
 */

/**
 * State
 */
data class State(val navigation: Navigation) {
  companion object
}

data class Navigation(val navigationKeys: List<String>, val direction: Direction)

enum class Direction {
  FORWARD, BACK, REPLACE
}

/**
 * Default State
 */
val defaultNavigation = Navigation(listOf("Parent"), Direction.REPLACE)
val initialState = State(defaultNavigation)

/**
 * Redux with feedback
 */
fun State.Companion.initialize(initState: State, commands: Observable<Action>): Observable<State> {
  val finishFeedback: (Observable<State>) -> Observable<Action> = {
    it.map(State::navigation)
        .map { it.navigationKeys.isEmpty() }
        .distinctUntilChanged()
        .filter { it == true }
        .map { replace(defaultNavigation.navigationKeys) }
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
 * Action payloads
 */
data class NavigationPayload(val key: List<String>, val direction: Direction)

/**
 * Navigation action creators
 */
fun go(navigationKey: String): Action =
    Action(ActionTypes.NAVIGATION, NavigationPayload(listOf(navigationKey), Direction.FORWARD))

fun goBack(): Action =
    Action(ActionTypes.NAVIGATION, NavigationPayload(emptyList(), Direction.BACK))

fun replace(navigationKeys: List<String>): Action =
    Action(ActionTypes.NAVIGATION, NavigationPayload(navigationKeys, Direction.REPLACE))

/**
 * Store
 */
@PerActivity
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
  
  fun observe(): Observable<State> = stateObservable.asObservable().share()
  
  fun observeNavigation(): Observable<Navigation> =
      observe()
          .map { it.navigation }
          .distinctUntilChanged()
}

/**
 * Reducers
 */
fun reduce(state: State, action: Action): State = State(navigation(state.navigation, action))

fun navigation(state: Navigation, action: Action): Navigation {
  when (action.type) {
    ActionTypes.NAVIGATION -> {
      val (key, direction) = action.payload as NavigationPayload
      when (direction) {
        Direction.FORWARD -> return state.copy(state.navigationKeys.plus(key), direction)
        Direction.BACK -> return state.copy(
            state.navigationKeys.minusElement(state.navigationKeys.last()), direction)
        Direction.REPLACE -> return state.copy(key, direction)
      }
    }
    else -> return state
  }
}
