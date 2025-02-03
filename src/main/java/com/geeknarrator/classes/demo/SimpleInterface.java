package com.geeknarrator.classes.demo;

public interface SimpleInterface {
  void method1();
  String method2(int param);

  // Default method demonstration
  default void defaultMethod() {
    System.out.println("Default implementation");
    helperMethod();
  }

  // Private interface method (Java 9+)
  private void helperMethod() {
    System.out.println("Helper method");
  }
}
