package beg.hr.rxredux.kotlin

import rx.Observable
import rx.subjects.BehaviorSubject
import javax.inject.Singleton

/**
 * Created by juraj on 04/04/2017.
 */

/**
 * State
 */
val initialState = State(listOf(Navigation("Timer")))

data class State(val navigation: List<Navigation>)

data class Navigation(val navigationKey: String)

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
  
  fun dispatch(action: Action) {
    stateObservable.onNext(reduce(getState(), action))
  }
  
  fun getState(): State = stateObservable.value
  
  fun observe(): Observable<State> = stateObservable.asObservable()
  
  fun observeNavigation(): Observable<Navigation> =
      observe().map {
        if (it.navigation.isEmpty()) return@map Navigation("Last")
        return@map it.navigation.last()
      }
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
      else
        return state.minusElement(state.last())
    }
    else -> return state
  }
}
