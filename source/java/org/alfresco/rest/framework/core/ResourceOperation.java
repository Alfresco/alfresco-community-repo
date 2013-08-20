package org.alfresco.rest.framework.core;

import java.util.List;

import org.springframework.http.HttpMethod;

/**
 * Operations that can typically take place on a Restful resource
 *
 * @author Gethin James
 */
public class ResourceOperation
{
    private final HttpMethod httpMethod;
    private final String title;
    private final String description;
    private final List<ResourceParameter> parameters;
    
    /**
     * @param httpMethod
     * @param title
     * @param description
     * @param parameters
     */
    public ResourceOperation(HttpMethod httpMethod, String title, String description, List<ResourceParameter> parameters)
    {
        super();
        this.httpMethod = httpMethod;
        this.title = title;
        this.description = description;
        this.parameters = parameters;
    }

    public HttpMethod getHttpMethod()
    {
        return this.httpMethod;
    }

    public String getTitle()
    {
        return this.title;
    }

    public String getDescription()
    {
        return this.description;
    }

    public List<ResourceParameter> getParameters()
    {
        return this.parameters;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("ResourceOperation [httpMethod=");
        builder.append(this.httpMethod);
        builder.append(", title=");
        builder.append(this.title);
        builder.append(", description=");
        builder.append(this.description);
        builder.append(", parameters=");
        builder.append(this.parameters);
        builder.append("]");
        return builder.toString();
    }
}
