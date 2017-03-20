package beg.hr.rxredux.java.redux.presentation;

import android.support.v4.view.ViewCompat;
import android.view.View;

import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.view.ViewAttachEvent;
import com.jakewharton.rxbinding.view.ViewAttachEvent.Kind;
import com.trello.rxlifecycle.LifecycleProvider;
import com.trello.rxlifecycle.LifecycleTransformer;
import com.trello.rxlifecycle.RxLifecycle;
import com.trello.rxlifecycle.android.ActivityEvent;
import com.trello.rxlifecycle.android.RxLifecycleAndroid;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

import beg.hr.rxredux.java.redux.Command;
import rx.Observable;

import static rx.Observable.merge;

/** Created by juraj on 17/03/2017. */
public abstract class ViewDriver<V extends View, S> implements LifecycleProvider<ViewAttachEvent> {
  private final V view;
  private final ViewAttachEvent attachEvent;
  private final ViewAttachEvent detachEvent;
  private final LifecycleProvider<ActivityEvent> lifecycleProvider;

  protected ViewDriver(V view, LifecycleProvider<ActivityEvent> lifecycleProvider) {
    this.view = view;
    this.lifecycleProvider = lifecycleProvider;
    this.attachEvent = ViewAttachEvent.create(view, Kind.ATTACH);
    this.detachEvent = ViewAttachEvent.create(view, Kind.DETACH);
  }

  public abstract void input(S state);

  public abstract Observable<Command> output();

  public V view() {
    return view;
  }

  private boolean isAttach(ViewAttachEvent e) {
    return e.equals(attachEvent);
  }

  private boolean isDetach(ViewAttachEvent e) {
    return e.equals(detachEvent);
  }

  @Nonnull
  @Override
  public Observable<ViewAttachEvent> lifecycle() {
    Observable<ViewAttachEvent> events = RxView.attachEvents(this.view);
    if (ViewCompat.isAttachedToWindow(view)) events = events.startWith(attachEvent);
    return events;
  }

  @Nonnull
  @Override
  public <T> LifecycleTransformer<T> bindUntilEvent(ViewAttachEvent event) {
    return RxLifecycle.bindUntilEvent(lifecycle(), event);
  }

  @Nonnull
  @Override
  public <T> LifecycleTransformer<T> bindToLifecycle() {
    return RxLifecycleAndroid.bindView(this.view);
  }

  @Nonnull
  @CheckReturnValue
  public <T> LifecycleTransformer<T> bindUntilDetach() {
    return bindUntilEvent(ViewAttachEvent.create(view, Kind.DETACH));
  }

  public Observable<Boolean> load() {
    Observable<Boolean> attachEvent =
        lifecycle().filter(this::isAttach).map(viewAttachEvent -> true);
    Observable<Boolean> onStartEvent =
        lifecycleProvider
            .lifecycle()
            .filter(activityEvent -> activityEvent.equals(ActivityEvent.START))
            .map(activityEvent -> true);

    Observable<Boolean> detachEvent =
        lifecycle().filter(this::isDetach).map(viewAttachEvent -> false);
    Observable<Boolean> onStopEvent =
        lifecycleProvider
            .lifecycle()
            .filter(activityEvent -> activityEvent.equals(ActivityEvent.STOP))
            .map(activityEvent -> false);

    return merge(attachEvent, onStartEvent, detachEvent, onStopEvent)
        .distinctUntilChanged()
        .compose(bindUntilDetach())
        .filter(Boolean::booleanValue);
  }

  public <T> LifecycleTransformer<T> bindUntilDestroy() {
    // TODO: 17/03/2017 add onStop
    return bindUntilDetach();
  }
}
