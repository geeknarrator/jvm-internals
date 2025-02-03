package com.geeknarrator.classes.demo;

@CustomAnnotation(value = "test", priority = 1, tags = {"demo", "class"})
public class AnnotatedClass {

  @CustomAnnotation("method")
  public void annotatedMethod() {
    // Method implementation
  }
}