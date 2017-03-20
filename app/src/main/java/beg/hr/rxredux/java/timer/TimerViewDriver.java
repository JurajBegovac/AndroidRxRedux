package beg.hr.rxredux.java.timer;

import com.jakewharton.rxbinding.view.RxView;
import com.trello.rxlifecycle.LifecycleProvider;
import com.trello.rxlifecycle.android.ActivityEvent;

import beg.hr.rxredux.java.redux.Command;
import beg.hr.rxredux.java.redux.presentation.ViewDriver;
import beg.hr.rxredux.java.timer.TimerRedux.PauseCommand;
import beg.hr.rxredux.java.timer.TimerRedux.ResumeCommand;
import beg.hr.rxredux.java.timer.TimerRedux.StartCommand;
import beg.hr.rxredux.java.timer.TimerRedux.State;
import beg.hr.rxredux.java.timer.TimerRedux.StopCommand;
import rx.Observable;

/** Created by juraj on 17/03/2017. */
public class TimerViewDriver extends ViewDriver<TimerView, TimerRedux.State> {

  private TimerRedux.State currentState;

  public TimerViewDriver(TimerView view, LifecycleProvider<ActivityEvent> lifecycleProvider) {
    super(view, lifecycleProvider);
  }

  @Override
  public void input(State state) {
    view().setTimer(String.valueOf(state.count()));
    currentState = state;
  }

  @Override
  public Observable<Command> output() {
    Observable<Command> start$ =
        RxView.clicks(view().getStart()).share().map(aVoid -> StartCommand.create());
    Observable<Command> stop$ =
        RxView.clicks(view().getStop()).share().map(aVoid -> StopCommand.create());
    Observable<Command> pause$ =
        RxView.clicks(view().getPause()).share().map(aVoid -> PauseCommand.create());
    Observable<Command> resume$ =
        RxView.clicks(view().getResume()).share().map(aVoid -> ResumeCommand.create());
    return Observable.merge(start$, stop$, pause$, resume$);
  }
}
