package com.noydb.duhmap.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// marks  the annotation as only available
// at source processing. So not available
// at runtime
@Retention(RetentionPolicy.SOURCE)
// can only be used on a class, interface,
// enum, or record
@Target(ElementType.METHOD)
public @interface DuhMapMethod {

    String[] ignoredFields() default {};

    boolean ignore() default false;

    boolean nullSafe() default true;

    boolean mapAll() default false;

}
