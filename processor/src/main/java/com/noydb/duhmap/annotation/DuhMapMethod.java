package com.noydb.duhmap.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// marks  the annotation as only available
// at source processing. So not available
// at runtime
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD) // can only be
// used on a class, interface, enum, or
// record
public @interface DuhMapMethod {

    String[] ignoredFields() default {};

    boolean ignore() default false;

}
