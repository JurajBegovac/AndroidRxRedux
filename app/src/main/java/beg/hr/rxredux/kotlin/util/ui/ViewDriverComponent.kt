package beg.hr.rxredux.kotlin.util.ui

import android.view.View

/**
 * Created by juraj on 26/03/2017.
 */
abstract class ViewDriverComponent<in S, out V : View, C, out VD : ViewDriver<V, S, C>>(val driver: VD) : ViewComponent<V> {
    override fun view(): V = driver.view
}
