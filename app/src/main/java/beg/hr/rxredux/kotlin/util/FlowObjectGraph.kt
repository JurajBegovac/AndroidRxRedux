package beg.hr.rxredux.kotlin.util

import android.app.Activity
import beg.hr.kotlindesarrolladorandroid.common.dagger2.ActivityModule
import beg.hr.kotlindesarrolladorandroid.common.dagger2.ActivityObjectGraph
import beg.hr.kotlindesarrolladorandroid.common.dagger2.PerActivity
import beg.hr.rxredux.kotlin.dummy.DummyController
import beg.hr.rxredux.kotlin.dummy.DummyObjectGraph
import beg.hr.rxredux.kotlin.timer.TimerController
import beg.hr.rxredux.kotlin.timer.TimerObjectGraph
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

/**
 * Created by juraj on 30/03/2017.
 */
@PerActivity
@Subcomponent(modules = arrayOf(FlowModule::class))
interface FlowObjectGraph : ActivityObjectGraph {
  
  // timer
  fun inject(target: TimerController)
  
  fun timerBuilder(): TimerObjectGraph.Builder
  
  // dummy
  fun inject(target: DummyController)
  
  fun dummyBuilder(): DummyObjectGraph.Builder
  
  @Subcomponent.Builder
  interface Builder {
    fun module(module: FlowModule): Builder
    fun build(): FlowObjectGraph
  }
}

@Module(subcomponents = arrayOf(TimerObjectGraph::class, DummyObjectGraph::class))
class FlowModule(activity: Activity, val flow: Flow) : ActivityModule(activity) {
  
  @Provides
  @PerActivity
  fun flow(): Flow = flow
  
}
