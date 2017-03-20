package beg.hr.rxredux.java.redux.presentation;

import android.view.View;

/** Created by juraj on 17/03/2017. */
public abstract class ViewDriverComponent<VD extends ViewDriver> {

  private final VD driver;

  public ViewDriverComponent(VD driver) {
    this.driver = driver;
  }

  protected VD driver() {
    return driver;
  }

  public View view() {
    return driver.view();
  }
}
