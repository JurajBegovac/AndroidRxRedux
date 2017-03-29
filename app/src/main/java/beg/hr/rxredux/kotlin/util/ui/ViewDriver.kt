package beg.hr.rxredux.kotlin.util.ui

import android.support.v4.view.ViewCompat
import android.view.View
import com.jakewharton.rxbinding.view.RxView
import com.jakewharton.rxbinding.view.ViewAttachEvent
import com.trello.rxlifecycle.LifecycleProvider
import com.trello.rxlifecycle.LifecycleTransformer
import com.trello.rxlifecycle.RxLifecycle
import com.trello.rxlifecycle.android.ActivityEvent
import com.trello.rxlifecycle.android.RxLifecycleAndroid
import rx.Observable
import rx.Observable.merge
import rx.subjects.BehaviorSubject

/**
 * Created by juraj on 24/03/2017.
 */
abstract class ViewDriver<out V : View, in S, C>(val view: V, val lifecycleProvider: LifecycleProvider<ActivityEvent>) : LifecycleProvider<ViewAttachEvent> {

    val attachEvent: ViewAttachEvent = ViewAttachEvent.create(view, ViewAttachEvent.Kind.ATTACH)
    val detachEvent: ViewAttachEvent = ViewAttachEvent.create(view, ViewAttachEvent.Kind.DETACH)

    fun loadEvent(): Observable<Boolean> {
        val activityEvents = BehaviorSubject.create<ActivityEvent>()

        lifecycleProvider.lifecycle().subscribe({ activityEvents.onNext(it) })
        val lifecycle = activityEvents.asObservable().share()

        val attachEvent = lifecycle()
                .filter({ this.isAttach(it) })
                .map { true }
                .filter {
                    // fixme sometimes attach event is called after onStop ?!
                    // Don't let it through if last activity event is stop
                    ActivityEvent.STOP != activityEvents.value
                }

        val onStartEvent = lifecycle
                .filter { activityEvent -> activityEvent == ActivityEvent.START }
                .map { true }

        val detachEvent = lifecycle().filter({ this.isDetach(it) }).map { false }

        val onStopEvent = lifecycle
                .filter { activityEvent -> activityEvent == ActivityEvent.STOP }
                .map { false }

        return merge(attachEvent, onStartEvent, detachEvent, onStopEvent)
                .distinctUntilChanged()
                .compose(bindUntilDetach())
                .filter { it }
    }

    fun isAttach(e: ViewAttachEvent): Boolean {
        return e == attachEvent
    }

    fun isDetach(e: ViewAttachEvent): Boolean {
        return e == detachEvent
    }

    override fun lifecycle(): Observable<ViewAttachEvent> {
        var events: Observable<ViewAttachEvent> = RxView.attachEvents(view)
        if (ViewCompat.isAttachedToWindow(view)) events = events.startWith(attachEvent)
        return events
    }

    override fun <T : Any?> bindUntilEvent(event: ViewAttachEvent): LifecycleTransformer<T> = RxLifecycle.bindUntilEvent(lifecycle(), event)

    override fun <T : Any?> bindToLifecycle(): LifecycleTransformer<T> = RxLifecycleAndroid.bindView(view)

    fun <T> bindUntilDetach(): LifecycleTransformer<T> = bindUntilEvent(ViewAttachEvent.create(view, ViewAttachEvent.Kind.DETACH))

    abstract fun render(state: S)

    abstract fun commands(): Observable<C>
}
