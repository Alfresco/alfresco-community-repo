package org.alfresco.rest.framework;

import static java.lang.annotation.ElementType.METHOD;

import org.alfresco.rest.framework.core.ResourceOperation;
import org.springframework.extensions.webscripts.Status;

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
    int successStatus() default ResourceOperation.UNSET_STATUS;
}

