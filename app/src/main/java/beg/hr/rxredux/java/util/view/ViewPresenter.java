package beg.hr.rxredux.java.util.view;

import beg.hr.rxredux.java.util.view.Mvp.Presenter;

public abstract class ViewPresenter<V> implements Presenter<V> {

  private V view = null;

  /** Load has been called for the current {@link #view}. */
  private boolean loaded;

  /**
   * Called to give this presenter control of a view, typically from {@link
   * android.view.View#onAttachedToWindow()}. Sets the view that will be returned from {@link
   * #getView()}.
   *
   * <p>This presenter will be immediately {@link BundleService#register registered} (or
   * re-registered) with the given view's scope, leading to an immediate call to {@link #onLoad}.
   *
   * <p>It is expected that {@link #dropView(Object)} will be called with the same argument when the
   * view is no longer active, e.g. from {@link android.view.View#onDetachedFromWindow()}.
   *
   * @see BundleService#register
   */
  @Override
  public final void takeView(V view) {
    if (view == null) throw new NullPointerException("new view must not be null");

    if (this.view != view) {
      if (this.view != null) dropView(this.view);

      this.view = view;
      onLoad();
    }
  }

  /**
   * Called to surrender control of this view, e.g. when the view is detached. If and only if the
   * given view matches the last passed to {@link #takeView}, the reference to the view is cleared.
   *
   * <p>Mismatched views are a no-op, not an error. This is to provide protection in the not
   * uncommon case that dropView and takeView are called out of order. For example, an activity's
   * views are typically inflated in {@link android.app.Activity#onCreate}, but are only detached
   * some time after {@link android.app.Activity#onDestroy() onExitScope}. It's possible for a view
   * from one activity to be detached well after the window for the next activity has its views
   * inflated&mdash;that is, after the next activity's onResume call.
   */
  @Override
  public void dropView(V view) {
    if (view == null) throw new NullPointerException("dropped view must not be null");
    if (view == this.view) {
      loaded = false;
      onDestroy();
      this.view = null;
    }
  }

  /**
   * Returns the view managed by this presenter, or null if {@link #takeView} has never been called,
   * or after {@link #dropView}.
   */
  protected final V getView() {
    return view;
  }

  /**
   * @return true if this presenter is currently managing a view, or false if {@link #takeView} has
   *     never been called, or after {@link #dropView}.
   */
  protected final boolean hasView() {
    return view != null;
  }

  /**
   * Like {@link Bundler#onLoad}, but called only when {@link #getView()} is not null, and
   * debounced. That is, this method will be called exactly once for a given view instance, at least
   * until that view is {@link #dropView(Object) dropped}.
   *
   * <p>See {@link #takeView} for details.
   */
  protected void onLoad() {}

  /** Like {@link Bundler#onDestroy}. */
  protected void onDestroy() {}
}
