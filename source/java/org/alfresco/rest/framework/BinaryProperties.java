package org.alfresco.rest.framework;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An array of binary property names used on a resource method operation in the Rest API
 *
 * @author Gethin James
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BinaryProperties {
    String[] value();
}

