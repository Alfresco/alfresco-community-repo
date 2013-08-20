package org.alfresco.rest.framework.core;

import org.springframework.http.HttpMethod;

/**
 * Simple marker interface to indicate which HTTP method the implementation supports
 *
 * @author Gethin James
 */
public interface HttpMethodSupport
{
    public HttpMethod getHttpMethod();
}
