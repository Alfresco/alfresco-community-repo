package org.alfresco.rest.framework;

import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.alfresco.rest.framework.core.ResourceParameter;

/**
 * A parameter used on a resource method operation in the Rest API
 * 
 * The parameter kind is one of :
 * 
 * QUERY_STRING - A query string parameter as part of the URL
 * HTTP_BODY_OBJECT - A JSON object specified in a HTTP_BODY used by either a POST or PUT
 * URL_PATH - Included as part of the actual url, e.g. entity id. (Does not support multiple values)
 * HTTP_HEADER - Included in the request's HTTP Header
 *
 * @author Gethin James
 */
@Target({ METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface WebApiParam  {
    String name();
    String title();
    String description() default "";
    boolean required() default false;
    ResourceParameter.KIND kind() default ResourceParameter.KIND.URL_PATH;
    boolean allowMultiple() default true;  //For URL_PATH the only permitted value will be FALSE
}

