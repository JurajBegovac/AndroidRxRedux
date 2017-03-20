package beg.hr.rxredux.java;

import android.os.Bundle;
import android.view.View;

import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import beg.hr.rxredux.R;
import beg.hr.rxredux.java.util.view.Mvp;

public class MainActivity extends RxAppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Mvp.Link mvp =
        Mvp.Link.create(
            new TimerScreen.Presenter(),
            (TimerView) View.inflate(this, R.layout.activity_main, null),
            this);
    setContentView(mvp.view());
  }
}
