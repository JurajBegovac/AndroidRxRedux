package beg.hr.rxredux.kotlin.timer

import android.content.Context
import android.util.AttributeSet
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main_kotlin.view.*

/**
 * Created by juraj on 24/03/2017.
 */
class TimerView : LinearLayout {
    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    fun timer(): TextView = timer
    fun start(): Button = start
    fun stop(): Button = stop
    fun resume(): Button = resume
    fun pause(): Button = pause
}
