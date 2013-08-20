package org.alfresco.rest.framework;

import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to describe a resource method operation in the Rest API
 *
 * @author Gethin James
 */
@Target({ METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface WebApiDescription {
    String title();
    String description() default "";
}

