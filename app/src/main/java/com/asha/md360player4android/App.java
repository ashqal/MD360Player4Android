package com.asha.md360player4android;

import android.app.Application;

/**
 * @author yangkai
 */
public class App extends Application {

  private static App application;

  public static App getInstance() {
    return application;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    application = this;
  }
}
