package beg.hr.rxredux.kotlin.util

import rx.Observable
import rx.Scheduler
import rx.subjects.ReplaySubject

/**
 * Created by juraj on 16/03/2017.
 */
object Observables {
  fun <State, Command> system(initState: State,
                              accumulator: (State, Command) -> State,
                              scheduler: Scheduler,
                              vararg feedback: (Observable<State>) -> Observable<Command>): Observable<State> {
    return Observable.defer {
      val replaySubject: ReplaySubject<State> = ReplaySubject.create(1)
      
      val inputs: Observable<Command> = Observable.merge(feedback.map { it(replaySubject) })
          .observeOn(scheduler)
      
      inputs.scan(initState, accumulator)
          .doOnNext { replaySubject.onNext(it) }
    }
  }
}
