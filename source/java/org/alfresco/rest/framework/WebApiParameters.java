package org.alfresco.rest.framework;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An array of parameters used on a resource method operation in the Rest API
 *
 * @author Gethin James
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface WebApiParameters {
    WebApiParam[] value()  default {};
}
