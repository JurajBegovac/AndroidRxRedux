package beg.hr.rxredux.java;

import java.util.concurrent.TimeUnit;

import rx.Observable;

/** Created by juraj on 20/03/2017. */
public class UserService {

  public Observable<String> getUser() {
    return Observable.just("Juraj Begovac").delay(3, TimeUnit.SECONDS);
  }
}
