package com.geeknarrator.classes.demo;
abstract class AbstractBase {
  protected int field1;
  private String field2;

  public AbstractBase(int f1) {
    this.field1 = f1;
  }

  abstract void abstractMethod();

  public final String concreteMethod(int param) {
    return "Concrete: " + (param + field1);
  }
}
