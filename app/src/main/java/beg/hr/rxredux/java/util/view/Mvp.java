package beg.hr.rxredux.java.util.view;

import com.google.auto.value.AutoValue;

import android.support.v4.view.ViewCompat;
import android.util.Log;
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

import rx.Observable;

/** TODO: Add a class header comment! */
public class Mvp {

  public interface Presenter<V> {
    void takeView(V view);

    void dropView(V view);
  }

  @AutoValue
  public abstract static class Link<P extends Presenter<V>, V extends View>
      implements LifecycleProvider<ViewAttachEvent> {

    public static <P extends Presenter<V>, V extends View> Link<P, V> create(
        P presenter, V view, LifecycleProvider<ActivityEvent> lifecycleProvider) {

      ViewAttachEvent attachEvent = ViewAttachEvent.create(view, Kind.ATTACH);
      ViewAttachEvent detachEvent = ViewAttachEvent.create(view, Kind.DETACH);

      Observable<ViewAttachEvent> events = RxView.attachEvents(view);
      if (ViewCompat.isAttachedToWindow(view)) {
        events = events.startWith(attachEvent);
      }

      Observable<Boolean> attachEvents =
          events.filter(event -> event.equals(attachEvent)).map(viewAttachEvent -> true);
      Observable<Boolean> detachEvents =
          events.filter(event -> event.equals(detachEvent)).map(viewAttachEvent -> false);

      Observable<Boolean> onStartEvent =
          lifecycleProvider
              .lifecycle()
              .filter(activityEvent -> activityEvent.equals(ActivityEvent.START))
              .map(activityEvent -> true);

      Observable<Boolean> onStopEvent =
          lifecycleProvider
              .lifecycle()
              .filter(activityEvent -> activityEvent.equals(ActivityEvent.STOP))
              .map(activityEvent -> false);

      Observable.merge(attachEvents, detachEvents, onStartEvent, onStopEvent)
          .distinctUntilChanged()
          .compose(RxLifecycle.bindUntilEvent(events, ViewAttachEvent.create(view, Kind.DETACH)))
          .subscribe(
              attach -> {
                if (attach) presenter.takeView(view);
                else presenter.dropView(view);
              },
              throwable -> Log.e("TAG", "Error: " + throwable.toString()),
              () -> presenter.dropView(view));

      return new AutoValue_Mvp_Link<>(presenter, view);
    }

    @Nonnull
    @CheckReturnValue
    public Observable<ViewAttachEvent> lifecycle() {
      V view = view();
      Observable<ViewAttachEvent> events = RxView.attachEvents(view);
      if (ViewCompat.isAttachedToWindow(view))
        events = events.startWith(ViewAttachEvent.create(view, Kind.ATTACH));
      return events;
    }

    @Nonnull
    @CheckReturnValue
    public <T> LifecycleTransformer<T> bindUntilEvent(@Nonnull ViewAttachEvent event) {
      return RxLifecycle.bindUntilEvent(lifecycle(), event);
    }

    @Nonnull
    @CheckReturnValue
    public <T> LifecycleTransformer<T> bindToLifecycle() {
      return RxLifecycleAndroid.bindView(view());
    }

    @Nonnull
    @CheckReturnValue
    public <T> LifecycleTransformer<T> bindUntilDestroy() {
      // TODO: 20/03/2017 handle onStop
      return bindUntilEvent(ViewAttachEvent.create(view(), Kind.DETACH));
    }

    public abstract P presenter();

    public abstract V view();
  }
}
