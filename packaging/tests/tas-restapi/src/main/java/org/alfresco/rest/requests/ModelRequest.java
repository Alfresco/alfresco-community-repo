package org.alfresco.rest.requests;

import org.alfresco.rest.core.RestWrapper;

public abstract class ModelRequest<Request>
{
    protected RestWrapper restWrapper;

    public ModelRequest(RestWrapper restWrapper)
    {
        this.restWrapper = restWrapper;
    }

    @SuppressWarnings("unchecked")
    public Request usingParams(String... parameters)
    {
        restWrapper.withParams(parameters);
        return (Request) this;
    }

    @SuppressWarnings("unchecked")
    public Request includePath()
    {
        restWrapper.withParams("include=path");
        return (Request) this;
    }
}
