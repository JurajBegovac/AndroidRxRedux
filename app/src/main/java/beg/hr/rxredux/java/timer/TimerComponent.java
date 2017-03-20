package beg.hr.rxredux.java.timer;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import beg.hr.rxredux.java.redux.Command;
import beg.hr.rxredux.java.redux.ObservableUtils;
import beg.hr.rxredux.java.redux.presentation.ViewDriverComponent;
import beg.hr.rxredux.java.timer.TimerRedux.CountCommand;
import beg.hr.rxredux.java.timer.TimerRedux.State;
import java8.util.function.Function;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

/** Created by juraj on 17/03/2017. */
public class TimerComponent extends ViewDriverComponent<TimerViewDriver> {

  public TimerComponent(TimerViewDriver driver) {
    super(driver);
    driver().load().subscribe(this::onLoad);
  }

  private void onLoad(boolean b) {
    Observable<Command> commands = driver().output();
    initialize(commands)
        .compose(driver().bindUntilDestroy())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(driver()::input);
  }

  private Observable<State> initialize(Observable<Command> commands) {
    Function<Observable<State>, Observable<Command>> countFeedBack = countFeedBack();

    return ObservableUtils.reduxWithFeedback(
        commands,
        State.defaultState(),
        TimerRedux::reduce,
        AndroidSchedulers.mainThread(),
        Collections.singletonList(countFeedBack));
  }

  private Function<Observable<State>, Observable<Command>> countFeedBack() {
    return state$ ->
        state$
            .map(
                state -> {
                  if (state.isCounting()) return 1;
                  else return -1;
                })
            .distinctUntilChanged()
            .switchMap(
                flag -> {
                  if (flag == -1) return Observable.empty();
                  else
                    return Observable.interval(1, TimeUnit.SECONDS)
                        .map(aLong -> CountCommand.create())
                        .share();
                });
  }
}
