package beg.hr.rxredux.java;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import beg.hr.rxredux.R;
import beg.hr.rxredux.java.TimerScreen.State;
import butterknife.BindView;
import butterknife.ButterKnife;

/** Created by juraj on 20/03/2017. */
public class TimerView extends LinearLayout {

  @BindView(R.id.timer)
  TextView timer;

  @BindView(R.id.start)
  Button start;

  @BindView(R.id.stop)
  Button stop;

  @BindView(R.id.pause)
  Button pause;

  @BindView(R.id.resume)
  Button resume;

  public TimerView(Context context) {
    this(context, null);
  }

  public TimerView(Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public TimerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    ButterKnife.bind(this);
  }

  public void render(State state) {
    timer.setText(String.valueOf(state.count()));
  }
}
