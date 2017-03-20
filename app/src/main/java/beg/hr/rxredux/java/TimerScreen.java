package beg.hr.rxredux.java;

import com.google.auto.value.AutoValue;

import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.jakewharton.rxbinding.view.RxView;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import beg.hr.rxredux.java.util.view.ViewPresenter;
import java8.util.function.Function;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

import static beg.hr.rxredux.java.TimerScreen.StateType.COUNTING;
import static beg.hr.rxredux.java.TimerScreen.StateType.IDLE;
import static beg.hr.rxredux.java.TimerScreen.StateType.LOADING_USER;
import static beg.hr.rxredux.java.TimerScreen.StateType.PAUSED;
import static beg.hr.rxredux.java.TimerScreen.StateType.USER_LOADED;

/** Created by juraj on 20/03/2017. */
public class TimerScreen {

  // model - states
  enum StateType {
    IDLE,
    COUNTING,
    PAUSED,
    LOADING_USER,
    USER_LOADED
  }

  // commands
  interface Command {
    Type type();

    enum Type {
      START,
      STOP,
      RESUME,
      PAUSE,
      COUNT,
      LOAD_USER,
      USER_LOADED
    }
  }

  public static class Presenter extends ViewPresenter<TimerView> {

    private final UserService userService;

    // todo handle this with android rx lifecycle instead of this subscription
    private Subscription subscription;

    public Presenter(UserService userService) {
      this.userService = userService;
    }

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
        case LOAD_USER:
          return state.toBuilder().type(LOADING_USER).build();
        case USER_LOADED:
          return state.toBuilder().type(USER_LOADED).user(((UserLoaded) command).user()).build();
        default:
          throw new IllegalArgumentException("Wrong command");
      }
    }

    private Observable<State> initialize(Observable<Command> commands) {
      Function<Observable<State>, Observable<Command>> countFeedBack = countFeedBack();
      Function<Observable<State>, Observable<Command>> loadUserFeedback = loadUserFeedback();

      return ObservableUtils.reduxWithFeedback(
          commands,
          State.builder().count(0).type(LOADING_USER).build(),
          this::reduce,
          AndroidSchedulers.mainThread(),
          Arrays.asList(countFeedBack, loadUserFeedback));
    }

    private Function<Observable<State>, Observable<Command>> loadUserFeedback() {
      return state$ ->
          state$
              .map(
                  state -> {
                    if (state.type() == LOADING_USER) return 1;
                    else return -1;
                  })
              .distinctUntilChanged()
              .switchMap(
                  flag -> {
                    if (flag == -1) return Observable.empty();
                    else return userService.getUser().map(UserLoaded::create).share();
                  });
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
      Observable<Command> loadUser$ =
          RxView.clicks(view.loadUser).share().map(aVoid -> LoadUser.create());
      return Observable.merge(start$, stop$, pause$, resume$, loadUser$);
    }
  }

  @AutoValue
  abstract static class State implements Parcelable {

    public static State defaultState() {
      return builder().count(0).type(IDLE).user(null).build();
    }

    public static Builder builder() {
      return new AutoValue_TimerScreen_State.Builder();
    }

    abstract int count();

    abstract StateType type();

    @Nullable
    abstract String user();

    public abstract Builder toBuilder();

    public boolean isCounting() {
      return type().equals(COUNTING);
    }

    @AutoValue.Builder
    public abstract static class Builder {
      public abstract Builder count(int count);

      public abstract Builder type(StateType type);

      public abstract Builder user(String user);

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

  @AutoValue
  abstract static class LoadUser implements Command {

    public static LoadUser create() {
      return new AutoValue_TimerScreen_LoadUser();
    }

    @Override
    public Type type() {
      return Type.LOAD_USER;
    }
  }

  @AutoValue
  abstract static class UserLoaded implements Command {

    public static UserLoaded create(String user) {
      return new AutoValue_TimerScreen_UserLoaded(user);
    }

    abstract String user();

    @Override
    public Type type() {
      return Type.USER_LOADED;
    }
  }
}
