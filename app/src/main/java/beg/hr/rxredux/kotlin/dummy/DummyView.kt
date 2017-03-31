package beg.hr.rxredux.kotlin.dummy

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.view_dummy.view.*

/**
 * Created by juraj on 30/03/2017.
 */
class DummyView : LinearLayout {
  constructor(context: Context?) : this(context, null)
  constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
  constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context,
                                                                                  attrs,
                                                                                  defStyleAttr)
  
  fun back() = back
}