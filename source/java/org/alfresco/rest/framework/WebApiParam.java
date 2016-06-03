/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
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

