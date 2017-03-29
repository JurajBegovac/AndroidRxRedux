package beg.hr.rxredux.kotlin.util.ui

import android.view.View

/**
 * Created by juraj on 26/03/2017.
 */
interface ViewComponent<out V : View> {
    fun view(): V
}
