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
    public static final int UNSET_STATUS = -1;
    private final HttpMethod httpMethod;
    private final String title;
    private final String description;
    private final List<ResourceParameter> parameters;
    private final int successStatus;
    
    /**
     * @param httpMethod HttpMethod
     * @param title String
     * @param description String
     * @param parameters List<ResourceParameter>
     * @param successStatus HTTP status
     */
    public ResourceOperation(HttpMethod httpMethod, String title, String description, List<ResourceParameter> parameters, int successStatus)
    {
        super();
        this.httpMethod = httpMethod;
        this.title = title;
        this.description = description;
        this.parameters = parameters;
        this.successStatus = successStatus;
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

    public int getSuccessStatus()
    {
        return successStatus;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("ResourceOperation [httpMethod=");
        builder.append(this.httpMethod);
        builder.append(", title=");
        builder.append(this.title);
        builder.append(", status=");
        builder.append(this.successStatus);
        builder.append(", description=");
        builder.append(this.description);
        builder.append(", parameters=");
        builder.append(this.parameters);
        builder.append("]");
        return builder.toString();
    }
}
