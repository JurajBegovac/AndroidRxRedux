package beg.hr.rxredux.kotlin

import android.os.Bundle
import android.view.View
import beg.hr.rxredux.R
import beg.hr.rxredux.kotlin.timer.TimerComponent
import beg.hr.rxredux.kotlin.timer.TimerDriver
import beg.hr.rxredux.kotlin.timer.TimerView
import com.trello.rxlifecycle.components.support.RxAppCompatActivity

class MainActivityKotlin : RxAppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val timerDriver: TimerDriver = TimerDriver(View.inflate(this, R.layout.activity_main_kotlin, null) as TimerView, this)
        val timerComponent: TimerComponent = TimerComponent(timerDriver)
        setContentView(timerComponent.view())
    }
}
