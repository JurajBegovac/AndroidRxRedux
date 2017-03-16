package beg.hr.rxredux.java;

import com.google.auto.value.AutoValue;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;
import com.trello.rxlifecycle.android.ActivityEvent;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import java.util.List;
import java.util.concurrent.TimeUnit;

import beg.hr.rxredux.R;
import butterknife.BindView;
import butterknife.ButterKnife;
import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.ReplaySubject;

import static beg.hr.rxredux.java.MainActivity.StateType.COUNTING;
import static beg.hr.rxredux.java.MainActivity.StateType.IDLE;

public class MainActivity extends RxAppCompatActivity {

  @BindView(R.id.timer)
  TextView timer;

  @BindView(R.id.start)
  Button start;

  @BindView(R.id.stop)
  Button stop;

  @BindView(R.id.pause)
  Button pause;

  @BindView(R.id.resume)
  Button resume;

  static State execute(State state, Command command) {
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
        return state.toBuilder().type(IDLE).build();
      case COUNT:
        return state.toBuilder().type(COUNTING).count(state.count() + 1).build();
      default:
        throw new IllegalArgumentException("Wrong command");
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);
  }

  @Override
  protected void onStart() {
    super.onStart();

    Observable<Command> start$ = RxView.clicks(start).map(aVoid -> StartCommand.create());
    Observable<Command> stop$ = RxView.clicks(stop).map(aVoid -> StopCommand.create());
    Observable<Command> pause$ = RxView.clicks(pause).map(aVoid -> PauseCommand.create());
    Observable<Command> resume$ = RxView.clicks(resume).map(aVoid -> ResumeCommand.create());

    Observable<Command> commands = Observable.merge(start$, stop$, pause$, resume$);

    initialize(commands)
        .compose(bindUntilEvent(ActivityEvent.STOP))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(this::render);
  }

  private void render(State state) {
    timer.setText(String.valueOf(state.count()));
  }

  private Observable<State> initialize(Observable<Command> commands) {
    Observable<Command> countFeedback =
        Observable.interval(1, TimeUnit.SECONDS).map(aLong -> CountCommand.create());

    return commands
        .mergeWith(countFeedback)
        .scan(State.defaultState(), MainActivity::execute)
        .startWith(State.defaultState());
  }

  private Observable<Command> countFeedback(Observable<State> state$) {
    return state$
        .map(
            state -> {
              if (state.isCounting()) return state;
              else return null;
            })
        .distinctUntilChanged()
        .switchMap(
            state -> {
              if (state == null) return Observable.empty();
              else
                return Observable.interval(1, TimeUnit.SECONDS).map(aLong -> CountCommand.create());
            });
  }

  private Observable<State> reduxWithFeedBack(
      State initialState, List<Observable<Command>> feedback) {
    return Observable.defer(
        () -> {
          ReplaySubject<State> replaySubject = ReplaySubject.create(1);

          List<Observable<State>> feedbacks =
              StreamSupport.stream(feedback)
                  .map(commandObservable -> replaySubject.asObservable())
                  .collect(Collectors.toList());

          Observable<State> input = Observable.merge(feedbacks);

          //    return input.scan(initialState, MainActivity::execute).startWith(initialState)
          //        .doOnNext(state -> replaySubject.onNext(state));
          return null;
        });
  }

  enum StateType {
    IDLE,
    COUNTING
  }

  private enum CommandType {
    START,
    STOP,
    RESUME,
    PAUSE,
    COUNT
  }

  interface Command {
    CommandType type();
  }

  @AutoValue
  abstract static class State {

    public static State defaultState() {
      return create(0, IDLE);
    }

    public static Builder builder() {
      return new AutoValue_MainActivity_State.Builder();
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
      return new AutoValue_MainActivity_StartCommand();
    }

    @Override
    public CommandType type() {
      return CommandType.START;
    }
  }

  @AutoValue
  abstract static class StopCommand implements Command {

    public static StopCommand create() {
      return new AutoValue_MainActivity_StopCommand();
    }

    @Override
    public CommandType type() {
      return CommandType.STOP;
    }
  }

  @AutoValue
  abstract static class ResumeCommand implements Command {

    public static ResumeCommand create() {
      return new AutoValue_MainActivity_ResumeCommand();
    }

    @Override
    public CommandType type() {
      return CommandType.RESUME;
    }
  }

  @AutoValue
  abstract static class PauseCommand implements Command {

    public static PauseCommand create() {
      return new AutoValue_MainActivity_PauseCommand();
    }

    @Override
    public CommandType type() {
      return CommandType.PAUSE;
    }
  }

  @AutoValue
  abstract static class CountCommand implements Command {

    public static CountCommand create() {
      return new AutoValue_MainActivity_CountCommand();
    }

    @Override
    public CommandType type() {
      return CommandType.COUNT;
    }
  }
}
