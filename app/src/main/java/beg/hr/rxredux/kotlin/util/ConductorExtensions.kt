package beg.hr.rxredux.kotlin.util

import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler
import com.bluelinelabs.conductor.changehandler.VerticalChangeHandler

/**
 * Created by juraj on 31/03/2017.
 */

fun RouterTransaction.leftRight() = this.pushChangeHandler(HorizontalChangeHandler())
    .popChangeHandler(HorizontalChangeHandler())

fun RouterTransaction.bottomTop() = this.pushChangeHandler(VerticalChangeHandler())
    .popChangeHandler(VerticalChangeHandler())

fun RouterTransaction.fade() = this.pushChangeHandler(FadeChangeHandler())
    .popChangeHandler(FadeChangeHandler())
