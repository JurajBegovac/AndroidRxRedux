package beg.hr.kotlindesarrolladorandroid.common.dagger2

import javax.inject.Qualifier

/**
 * Created by juraj on 24/02/2017.
 */
@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class ApplicationContext


@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class ActivityContext
