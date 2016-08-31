package org.alfresco.httpclient;

import org.apache.commons.httpclient.HttpMethod;

public class AuthenticationException extends Exception
{
    private static final long serialVersionUID = -407003742855571557L;

    private HttpMethod method;

    public AuthenticationException(HttpMethod method)
    {
        this.method = method;
    }

    public HttpMethod getMethod()
    {
        return method;
    }

}
