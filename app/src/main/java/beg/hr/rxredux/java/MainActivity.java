package beg.hr.rxredux.java;

import android.os.Bundle;
import android.view.View;

import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import beg.hr.rxredux.R;
import beg.hr.rxredux.java.redux.presentation.ViewDriverComponent;
import beg.hr.rxredux.java.timer.TimerComponent;
import beg.hr.rxredux.java.timer.TimerView;
import beg.hr.rxredux.java.timer.TimerViewDriver;

public class MainActivity extends RxAppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    TimerViewDriver driver =
        new TimerViewDriver((TimerView) View.inflate(this, R.layout.activity_main, null), this);
    ViewDriverComponent component = new TimerComponent(driver);
    setContentView(component.view());
  }
}
