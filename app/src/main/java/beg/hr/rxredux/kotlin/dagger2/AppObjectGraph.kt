package beg.hr.kotlindesarrolladorandroid.common.dagger2

import android.app.Application
import android.content.Context
import beg.hr.rxredux.kotlin.State
import beg.hr.rxredux.kotlin.Store
import beg.hr.rxredux.kotlin.timer.TimerService
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
  fun timerService(): TimerService
  fun store(): Store
}

@Module(subcomponents = arrayOf(ActivityObjectGraph::class))
class AppModule(val application: Application, val initialState: State) {
  
  @Provides
  @Singleton
  fun application() = application
  
  @Provides
  @Singleton
  @ApplicationContext
  fun context(): Context = application.applicationContext
  
  @Provides
  @Singleton
  fun store(): Store = Store(initialState)
  
}
