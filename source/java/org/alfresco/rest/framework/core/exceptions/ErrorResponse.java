package org.alfresco.rest.framework.core.exceptions;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

/**
 * A response object that can be represented to the client, based on the Alfresco API guidelines.
 *
 * @author Gethin James
 */
public class ErrorResponse
{
    final private String errorKey;
    final private int statusCode;
    final private String briefSummary;
    final private String stackTrace;
    final private Map<String,Object> additionalState;
    final private String descriptionURL;
    
    public ErrorResponse(String errorKey, int statusCode, String briefSummary,
                StackTraceElement[] stackTrace, Map<String,Object> additionalState)
    {
        super();
        this.errorKey = errorKey;
        this.statusCode = statusCode;
        this.briefSummary = briefSummary;
        this.stackTrace = Arrays.toString(stackTrace);
        this.additionalState = additionalState==null?null:Collections.unmodifiableMap(additionalState);
        this.descriptionURL = null;
    }

    public ErrorResponse(String errorKey, int statusCode, String briefSummary,
                         String descriptionURL, Map<String,Object> additionalState)
    {
        super();
        this.errorKey = errorKey;
        this.statusCode = statusCode;
        this.briefSummary = briefSummary;
        this.stackTrace = " ";
        this.additionalState = additionalState==null?null:Collections.unmodifiableMap(additionalState);
        this.descriptionURL = descriptionURL;
    }

    public String getErrorKey()
    {
        return this.errorKey;
    }

    public int getStatusCode()
    {
        return this.statusCode;
    }

    public String getBriefSummary()
    {
        return this.briefSummary;
    }

    public String getStackTrace()
    {
        return this.stackTrace;
    }

    public String getDescriptionURL()
    {
        return this.descriptionURL;
    }

    public Map<String, Object> getAdditionalState()
    {
        return this.additionalState;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("ErrorResponse [errorKey=").append(this.errorKey).append(", statusCode=")
               .append(this.statusCode).append(", briefSummary=").append(this.briefSummary)
               .append(", descriptionURL=").append(this.descriptionURL)
               .append(", stackTrace=").append(this.stackTrace).append(", additionalState=")
               .append(this.additionalState).append("]");
        return builder.toString();
    }
    
    
}
