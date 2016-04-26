package org.alfresco.repo.policy.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Roy Wetherall
 * @since 5.0
 */
@Target(value = ElementType.TYPE)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface BehaviourBean
{
    /** qualified name of type/aspect */
    String defaultType() default "";
}
