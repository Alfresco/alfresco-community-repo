package org.alfresco.rest.framework.resource;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates a method and indicates it is the unique id of a embedded object.
 * 
 * The method must have zero arguments.  It will be executed and the enclosing class will have
 * the result of a call to the entity resource embedded in it.
 *
 * @author Gethin James
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EmbeddedEntityResource
{
    String propertyName();
    Class<?> entityResource();
}
