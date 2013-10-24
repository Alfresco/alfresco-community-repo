package org.alfresco.rest.workflow.api.tests;

import org.alfresco.rest.api.tests.client.AuthenticatedHttp;
import org.alfresco.rest.api.tests.client.PublicApiHttpClient;

public class WorkflowApiHttpClient extends PublicApiHttpClient
{
    public WorkflowApiHttpClient(String host, int port, String contextPath, String servletName, AuthenticatedHttp authenticatedHttp)
    {
        this("http", host, port, contextPath, servletName, authenticatedHttp);
    }

    public WorkflowApiHttpClient(String scheme, String host, int port, String contextPath, String servletName, AuthenticatedHttp authenticatedHttp)
    {
        super(scheme, host, port, contextPath, servletName, authenticatedHttp);
        apiName = "workflow";
    }

}
