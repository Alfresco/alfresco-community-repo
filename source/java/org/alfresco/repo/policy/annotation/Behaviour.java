package org.alfresco.repo.policy.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.alfresco.repo.policy.Behaviour.NotificationFrequency;

/**
 * Behaviour method annotation.
 *
 * @author Roy Wetherall
 * @since 5.0
 */
@Target(value = ElementType.METHOD)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Behaviour
{
    /** lookup name of the behaviour, if none specified behaviour is not recorded in the registry */
    String name() default "";

    /** kind of behaviour */
    BehaviourKind kind();

    /** qualified name of policy */
    String policy() default "";

    /** indicates whether this is a service behaviour or not */
    boolean isService() default false;

    /** qualified name of type/aspect */
    String type() default "";

    /** qualified name of association */
    String assocType() default "cm:contains";

    /** notification frequency */
    NotificationFrequency notificationFrequency() default NotificationFrequency.EVERY_EVENT;
}
