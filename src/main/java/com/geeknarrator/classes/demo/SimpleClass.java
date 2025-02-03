package com.geeknarrator.classes.demo;

public class SimpleClass {
  private int field1;
  private String field2;

  public SimpleClass(int f1, String f2) {
    this.field1 = f1;
    this.field2 = f2;
  }

  public void method1() {
    System.out.println("Method 1: " + field1 + ", " + field2);
  }

  public String method2(int param) {
    return field2 + param;
  }
}
