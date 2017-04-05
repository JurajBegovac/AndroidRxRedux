package beg.hr.kotlindesarrolladorandroid.common.dagger2

import android.app.Activity
import android.content.Context
import beg.hr.rxredux.kotlin.dummy.DummyObjectGraph
import beg.hr.rxredux.kotlin.muliti_controllers.ParentController
import beg.hr.rxredux.kotlin.timer.TimerObjectGraph
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

/**
 * Created by juraj on 24/02/2017.
 */

@PerActivity
@Subcomponent(modules = arrayOf(ActivityModule::class))
interface ActivityObjectGraph {
  
  // timer
  fun timerBuilder(): TimerObjectGraph.Builder
  
  // dummy
  fun dummyBuilder(): DummyObjectGraph.Builder
  
  fun inject(target: ParentController)
  
  @Subcomponent.Builder
  interface Builder {
    fun module(module: ActivityModule): Builder
    fun build(): ActivityObjectGraph
  }
}

@Module(subcomponents = arrayOf(TimerObjectGraph::class, DummyObjectGraph::class))
open class ActivityModule(val activity: Activity) {
  
  @Provides
  @PerActivity
  fun activity() = activity
  
  @Provides
  @PerActivity
  @ActivityContext
  fun context(): Context = activity
  
}
