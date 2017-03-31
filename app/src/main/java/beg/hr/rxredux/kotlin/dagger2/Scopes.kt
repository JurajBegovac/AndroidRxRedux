package beg.hr.kotlindesarrolladorandroid.common.dagger2

import javax.inject.Scope
import kotlin.annotation.AnnotationRetention.RUNTIME

/**
 * Created by juraj on 24/02/2017.
 */

@Scope
@Retention(RUNTIME)
annotation class PerActivity

@Scope
@Retention(RUNTIME)
annotation class PerComponent
