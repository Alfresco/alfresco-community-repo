package org.alfresco.rest.framework.resource;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates a method (to be invoked) that returns the unique id of the object.
 * 
 * The method must have zero arguments.
 * 
 * The "name" argument allows you to specify an alternative name for this "id" property.  The preferred/default value is "id"
 *
 * @author Gethin James
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UniqueId
{
    String UNIQUE_NAME = "id";
    String name() default UNIQUE_NAME;
}
