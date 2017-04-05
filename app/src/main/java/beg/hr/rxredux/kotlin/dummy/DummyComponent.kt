package beg.hr.rxredux.kotlin.dummy

import beg.hr.kotlindesarrolladorandroid.common.dagger2.ActivityObjectGraph
import beg.hr.kotlindesarrolladorandroid.common.dagger2.PerComponent
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

/**
 * Created by juraj on 31/03/2017.
 */
fun dummyController(activityObjectGraph: ActivityObjectGraph): DummyController {
  val graph = activityObjectGraph.dummyBuilder().build()
  val controller = graph.controller()
  graph.inject(controller)
  return controller
}

// Object graph
@PerComponent
@Subcomponent(modules = arrayOf(DummyModule::class))
interface DummyObjectGraph {
  
  fun controller(): DummyController
  fun inject(controller: DummyController)
  
  @Subcomponent.Builder
  interface Builder {
    fun module(module: DummyModule): Builder
    fun build(): DummyObjectGraph
  }
}

@Module
class DummyModule {
  
  @PerComponent
  @Provides
  fun controller(): DummyController = DummyController()
  
}
