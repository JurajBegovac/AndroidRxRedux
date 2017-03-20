package beg.hr.rxredux.java;

import com.google.auto.value.AutoValue;

import android.os.Parcelable;

import com.jakewharton.rxbinding.view.RxView;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import beg.hr.rxredux.java.util.view.ViewPresenter;
import java8.util.function.Function;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

import static beg.hr.rxredux.java.TimerScreen.StateType.COUNTING;
import static beg.hr.rxredux.java.TimerScreen.StateType.IDLE;
import static beg.hr.rxredux.java.TimerScreen.StateType.PAUSED;

/** Created by juraj on 20/03/2017. */
public class TimerScreen {

  // model - states
  enum StateType {
    IDLE,
    COUNTING,
    PAUSED
  }

  // intents = commands
  interface Command {
    Type type();

    enum Type {
      START,
      STOP,
      RESUME,
      PAUSE,
      COUNT
    }
  }

  public static class Presenter extends ViewPresenter<TimerView> {

    // todo handle this with android rx lifecycle instead of this subscription
    private Subscription subscription;

    @Override
    protected void onLoad() {
      super.onLoad();
      TimerView view = getView();
      subscription =
          initialize(userCommands())
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(view::render);
    }

    @Override
    protected void onDestroy() {
      super.onDestroy();
      if (subscription != null) subscription.unsubscribe();
    }

    private State reduce(State state, Command command) {
      switch (command.type()) {
        case START:
          if (state.isCounting()) return state;
          else return state.toBuilder().type(COUNTING).count(0).build();
        case STOP:
          return state.toBuilder().type(IDLE).count(0).build();
        case RESUME:
          if (state.isCounting()) return state;
          else return state.toBuilder().type(COUNTING).build();
        case PAUSE:
          return state.toBuilder().type(PAUSED).build();
        case COUNT:
          return state.toBuilder().type(COUNTING).count(state.count() + 1).build();
        default:
          throw new IllegalArgumentException("Wrong command");
      }
    }

    private Observable<State> initialize(Observable<Command> commands) {
      Function<Observable<State>, Observable<Command>> countFeedBack = countFeedBack();

      return ObservableUtils.reduxWithFeedback(
          commands,
          State.defaultState(),
          this::reduce,
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

    private Observable<Command> userCommands() {
      TimerView view = getView();
      Observable<Command> start$ =
          RxView.clicks(view.start).share().map(aVoid -> StartCommand.create());
      Observable<Command> stop$ =
          RxView.clicks(view.stop).share().map(aVoid -> StopCommand.create());
      Observable<Command> pause$ =
          RxView.clicks(view.pause).share().map(aVoid -> PauseCommand.create());
      Observable<Command> resume$ =
          RxView.clicks(view.resume).share().map(aVoid -> ResumeCommand.create());
      return Observable.merge(start$, stop$, pause$, resume$);
    }
  }

  @AutoValue
  abstract static class State implements Parcelable {

    public static State defaultState() {
      return create(0, IDLE);
    }

    public static Builder builder() {
      return new AutoValue_TimerScreen_State.Builder();
    }

    public static State create(int count, StateType type) {
      return builder().count(count).type(type).build();
    }

    abstract int count();

    abstract StateType type();

    public abstract Builder toBuilder();

    public boolean isCounting() {
      return type().equals(COUNTING);
    }

    @AutoValue.Builder
    public abstract static class Builder {
      public abstract Builder count(int count);

      public abstract Builder type(StateType type);

      public abstract State build();
    }
  }

  @AutoValue
  abstract static class StartCommand implements Command {

    public static StartCommand create() {
      return new AutoValue_TimerScreen_StartCommand();
    }

    @Override
    public Type type() {
      return Type.START;
    }
  }

  @AutoValue
  abstract static class StopCommand implements Command {

    public static StopCommand create() {
      return new AutoValue_TimerScreen_StopCommand();
    }

    @Override
    public Type type() {
      return Type.STOP;
    }
  }

  @AutoValue
  abstract static class ResumeCommand implements Command {

    public static ResumeCommand create() {
      return new AutoValue_TimerScreen_ResumeCommand();
    }

    @Override
    public Type type() {
      return Type.RESUME;
    }
  }

  @AutoValue
  abstract static class PauseCommand implements Command {

    public static PauseCommand create() {
      return new AutoValue_TimerScreen_PauseCommand();
    }

    @Override
    public Type type() {
      return Type.PAUSE;
    }
  }

  @AutoValue
  abstract static class CountCommand implements Command {

    public static CountCommand create() {
      return new AutoValue_TimerScreen_CountCommand();
    }

    @Override
    public Type type() {
      return Type.COUNT;
    }
  }
}
