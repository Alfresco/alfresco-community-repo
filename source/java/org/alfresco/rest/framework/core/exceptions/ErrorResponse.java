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
