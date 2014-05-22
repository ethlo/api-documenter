package com.ethlo.api.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * @author mha
 *
 */
@Target({ ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
@Inherited
@Documented
public @interface Api
{    
    String[] group() default "";
}
