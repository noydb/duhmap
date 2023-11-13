package com.noydb.damap.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
// marks  the annotation as only available at source processing. So not available at runtime
@Target(ElementType.TYPE) // can only be used on a class, interface, enum, or record
public @interface DuhMap {

    String[] ignoredFields() default "";

}
