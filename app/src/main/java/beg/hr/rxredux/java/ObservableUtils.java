package beg.hr.rxredux.java;

import java.util.List;

import java8.util.function.Function;
import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;
import rx.Observable;
import rx.Scheduler;
import rx.functions.Func2;
import rx.subjects.ReplaySubject;

/** Created by juraj on 16/03/2017. */
public class ObservableUtils {

  public static <R, T> Observable<R> reduxWithFeedback(
      Observable<T> commands,
      R initState,
      Func2<R, T, R> reducer,
      Scheduler scheduler,
      List<Function<Observable<R>, Observable<T>>> feedback) {

    return Observable.defer(
        () -> {
          ReplaySubject<R> replaySubject = ReplaySubject.create();

          Observable<R> output = replaySubject.asObservable().share();

          List<Observable<T>> feedbacks =
              StreamSupport.stream(feedback)
                  .map(function -> function.apply(output))
                  .collect(Collectors.toList());

          // add commands in feedback loop
          feedbacks.add(0, commands);

          Observable<T> inputs = Observable.merge(feedbacks).observeOn(scheduler);

          return inputs.scan(initState, reducer).doOnNext(replaySubject::onNext);
        });
  }
}
