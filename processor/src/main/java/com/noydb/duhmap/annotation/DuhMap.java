package com.noydb.duhmap.annotation;

import com.noydb.duhmap.kit.DuhMapClassType;
import com.noydb.duhmap.kit.DuhMapStrictRule;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * To be used at the interface level to
 * tell DuhMap to generate a concrete
 * implementation, mapping all applicable
 * methods.
 *
 * @author bpower
 */
// marks  the annotation as only available
// at source processing. So not available
// at runtime
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE) // can only be
// used on a class, interface, enum, or
// record
public @interface DuhMap {

    /**
     * If enabled, the source and target
     * classes must possess identical
     * (determined by type & name) fields.
     * Ignored fields also have to be defined
     * in the source and target classes.
     */
    boolean strictChecks() default false;

    String[] ignoredMethods() default "";

    DuhMapClassType beanType() default DuhMapClassType.DEFAULT;

    DuhMapStrictRule[] ignoredStrictChecks() default {};
}