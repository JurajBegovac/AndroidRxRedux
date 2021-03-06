package beg.hr.kotlindesarrolladorandroid.common.dagger2

import android.app.Application
import android.content.Context
import beg.hr.rxredux.kotlin.timer.TimerService
import beg.hr.rxredux.kotlin.util.FlowObjectGraph
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * Created by juraj on 24/02/2017.
 */
@Singleton
@Component(modules = arrayOf(AppModule::class))
interface AppObjectGraph {
  fun application(): Application
  fun activityObjectGraphBuilder(): ActivityObjectGraph.Builder
  fun flowObjectGraphBuilder(): FlowObjectGraph.Builder
  fun timerService(): TimerService
}

@Module(subcomponents = arrayOf(ActivityObjectGraph::class, FlowObjectGraph::class))
class AppModule(val application: Application) {
  
  @Provides
  @Singleton
  fun application() = application
  
  @Provides
  @Singleton
  @ApplicationContext
  fun context(): Context = application.applicationContext
  
}
