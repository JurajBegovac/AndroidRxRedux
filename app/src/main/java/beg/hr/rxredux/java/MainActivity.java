package beg.hr.rxredux.java;

import com.google.auto.value.AutoValue;

import android.os.Bundle;
import android.os.Parcelable;
import android.widget.Button;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;
import com.trello.rxlifecycle.android.ActivityEvent;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import beg.hr.rxredux.R;
import butterknife.BindView;
import butterknife.ButterKnife;
import java8.util.function.Function;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

import static beg.hr.rxredux.java.MainActivity.StateType.COUNTING;
import static beg.hr.rxredux.java.MainActivity.StateType.IDLE;
import static beg.hr.rxredux.java.MainActivity.StateType.PAUSED;

public class MainActivity extends RxAppCompatActivity {

  private static final String TAG_STATE = "tag_state";

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

  private State state;

  static State reduce(State state, Command command) {
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

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (savedInstanceState != null) {
      if (savedInstanceState.containsKey(TAG_STATE))
        state = savedInstanceState.getParcelable(TAG_STATE);
    }
    if (state == null) state = State.defaultState();

    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);
  }

  @Override
  protected void onStart() {
    super.onStart();

    Observable<Command> userCommands = commands();

    initialize(userCommands)
        .compose(bindUntilEvent(ActivityEvent.STOP))
        .observeOn(AndroidSchedulers.mainThread())
        .doOnNext(this::setState)
        .subscribe(this::render);
  }

  private void setState(State state) {
    this.state = state;
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putParcelable(TAG_STATE, state);
  }

  private Observable<Command> commands() {
    Observable<Command> start$ = RxView.clicks(start).share().map(aVoid -> StartCommand.create());
    Observable<Command> stop$ = RxView.clicks(stop).share().map(aVoid -> StopCommand.create());
    Observable<Command> pause$ = RxView.clicks(pause).share().map(aVoid -> PauseCommand.create());
    Observable<Command> resume$ =
        RxView.clicks(resume).share().map(aVoid -> ResumeCommand.create());
    return Observable.merge(start$, stop$, pause$, resume$);
  }

  private void render(State state) {
    timer.setText(String.valueOf(state.count()));
  }

  private Observable<State> initialize(Observable<Command> commands) {
    Function<Observable<State>, Observable<Command>> countFeedBack = countFeedBack();

    return ObservableUtils.reduxWithFeedback(
        commands,
        state,
        MainActivity::reduce,
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

  enum StateType {
    IDLE,
    COUNTING,
    PAUSED
  }

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

  @AutoValue
  abstract static class State implements Parcelable {

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
    public Type type() {
      return Type.START;
    }
  }

  @AutoValue
  abstract static class StopCommand implements Command {

    public static StopCommand create() {
      return new AutoValue_MainActivity_StopCommand();
    }

    @Override
    public Type type() {
      return Type.STOP;
    }
  }

  @AutoValue
  abstract static class ResumeCommand implements Command {

    public static ResumeCommand create() {
      return new AutoValue_MainActivity_ResumeCommand();
    }

    @Override
    public Type type() {
      return Type.RESUME;
    }
  }

  @AutoValue
  abstract static class PauseCommand implements Command {

    public static PauseCommand create() {
      return new AutoValue_MainActivity_PauseCommand();
    }

    @Override
    public Type type() {
      return Type.PAUSE;
    }
  }

  @AutoValue
  abstract static class CountCommand implements Command {

    public static CountCommand create() {
      return new AutoValue_MainActivity_CountCommand();
    }

    @Override
    public Type type() {
      return Type.COUNT;
    }
  }
}
