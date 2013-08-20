package org.alfresco.rest.framework;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates inclusion in the web api
 *
 * @author Gethin James
 */
@Target(ElementType.PACKAGE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface WebApi
{
    String name();
    Api.SCOPE scope();
    int version();

}
