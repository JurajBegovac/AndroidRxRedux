package beg.hr.rxredux.java.timer;

import com.google.auto.value.AutoValue;

import android.os.Parcelable;

import beg.hr.rxredux.java.redux.Command;

import static beg.hr.rxredux.java.timer.TimerRedux.StateType.COUNTING;
import static beg.hr.rxredux.java.timer.TimerRedux.StateType.IDLE;
import static beg.hr.rxredux.java.timer.TimerRedux.StateType.PAUSED;

/** Created by juraj on 17/03/2017. */
public class TimerRedux {

  static State reduce(State state, Command<TimerCommand> command) {
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

  enum StateType {
    IDLE,
    COUNTING,
    PAUSED
  }

  enum TimerCommand {
    START,
    STOP,
    RESUME,
    PAUSE,
    COUNT
  }

  @AutoValue
  abstract static class State implements Parcelable {

    public static State defaultState() {
      return create(0, IDLE);
    }

    public static Builder builder() {
      return new AutoValue_TimerRedux_State.Builder();
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
  abstract static class StartCommand implements Command<TimerCommand> {

    public static StartCommand create() {
      return new AutoValue_TimerRedux_StartCommand();
    }

    @Override
    public TimerCommand type() {
      return TimerCommand.START;
    }
  }

  @AutoValue
  abstract static class StopCommand implements Command<TimerCommand> {

    public static StopCommand create() {
      return new AutoValue_TimerRedux_StopCommand();
    }

    @Override
    public TimerCommand type() {
      return TimerCommand.STOP;
    }
  }

  @AutoValue
  abstract static class ResumeCommand implements Command<TimerCommand> {

    public static ResumeCommand create() {
      return new AutoValue_TimerRedux_ResumeCommand();
    }

    @Override
    public TimerCommand type() {
      return TimerCommand.RESUME;
    }
  }

  @AutoValue
  abstract static class PauseCommand implements Command<TimerCommand> {

    public static PauseCommand create() {
      return new AutoValue_TimerRedux_PauseCommand();
    }

    @Override
    public TimerCommand type() {
      return TimerCommand.PAUSE;
    }
  }

  @AutoValue
  abstract static class CountCommand implements Command<TimerCommand> {

    public static CountCommand create() {
      return new AutoValue_TimerRedux_CountCommand();
    }

    @Override
    public TimerCommand type() {
      return TimerCommand.COUNT;
    }
  }
}
