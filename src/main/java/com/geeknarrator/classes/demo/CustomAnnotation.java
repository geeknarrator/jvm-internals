package com.geeknarrator.classes.demo;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@interface CustomAnnotation {
  String value() default "";
  int priority() default 0;
  String[] tags() default {};
}