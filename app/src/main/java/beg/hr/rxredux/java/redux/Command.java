package beg.hr.rxredux.java.redux;

/** Created by juraj on 17/03/2017. */
public interface Command<T> {
  T type();
}
