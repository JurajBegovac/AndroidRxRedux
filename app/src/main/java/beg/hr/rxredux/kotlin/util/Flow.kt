package beg.hr.rxredux.kotlin.util

/**
 * Created by juraj on 30/03/2017.
 */
interface Flow {
  fun go(key: String)
  fun goBack()
  fun replace(keys: List<String>)
}
