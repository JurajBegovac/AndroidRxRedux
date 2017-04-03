package beg.hr.rxredux.kotlin.timer

import rx.Observable
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by juraj on 31/03/2017.
 */
@Singleton
class TimerService @Inject constructor() {
  
  fun autoStart(): Observable<String> = Observable.just("Start").delay(3, TimeUnit.SECONDS)
}
