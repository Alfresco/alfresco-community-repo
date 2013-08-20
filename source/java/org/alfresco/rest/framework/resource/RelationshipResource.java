package org.alfresco.rest.framework.resource;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.stereotype.Component;

/**
 * Represents a relationship to an entity resource
 *
 * @author Gethin James
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface RelationshipResource
{
    String name();
    String title();
    Class<?> entityResource();
}
