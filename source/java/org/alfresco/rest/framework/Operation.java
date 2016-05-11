package org.alfresco.rest.framework;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An Operation as on an entity in the Rest API
 *
 * Your method signature should have 4 parameters (uniqueId, typePassedin, Parameters, WithResponse),
 " use Void if you are not interested in the second argument.

 * @author Gethin James
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Operation
{
    String value();
}

