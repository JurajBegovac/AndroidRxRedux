package beg.hr.rxredux.kotlin

import android.app.Application
import beg.hr.kotlindesarrolladorandroid.common.dagger2.AppModule
import beg.hr.kotlindesarrolladorandroid.common.dagger2.AppObjectGraph
import beg.hr.kotlindesarrolladorandroid.common.dagger2.DaggerAppObjectGraph

/**
 * Created by juraj on 30/03/2017.
 */
class MyApp : Application() {
  
  companion object {
    lateinit var appObjectGraph: AppObjectGraph
  }
  
  override fun onCreate() {
    super.onCreate()
    appObjectGraph = DaggerAppObjectGraph.builder()
        .appModule(AppModule(this, initialState))
        .build()
  }
}
