/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2019 Alfresco Software Limited
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
package org.alfresco.repo.rendition2;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Model used for Transform Request Events.
 *
 * @author aepure
 */
public class TransformRequest implements Serializable
{
    private String requestId;
    private String transformName;
    private String nodeRef;
    private String targetMediaType;
    private Map<String, String> transformOptions = new HashMap<>();
    private String clientData;
    private String replyQueue;

    public String getRequestId()
    {
        return requestId;
    }

    public void setRequestId(String requestId)
    {
        this.requestId = requestId;
    }

    public String getTransformName()
    {
        return transformName;
    }

    public void setTransformName(String transformName)
    {
        this.transformName = transformName;
    }

    public String getNodeRef()
    {
        return nodeRef;
    }

    public void setNodeRef(String nodeRef)
    {
        this.nodeRef = nodeRef;
    }

    public String getTargetMediaType()
    {
        return targetMediaType;
    }

    public void setTargetMediaType(String targetMediaType)
    {
        this.targetMediaType = targetMediaType;
    }

    public Map<String, String> getTransformOptions()
    {
        return transformOptions;
    }

    public String getClientData()
    {
        return clientData;
    }

    public void setClientData(String clientData)
    {
        this.clientData = clientData;
    }

    public void setTransformOptions(Map<String, String> transformOptions)
    {
        this.transformOptions = transformOptions;
    }

    public String getReplyQueue()
    {
        return replyQueue;
    }

    public void setReplyQueue(String replyQueue)
    {
        this.replyQueue = replyQueue;
    }

    @Override public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        TransformRequest that = (TransformRequest) o;
        return requestId.equals(that.requestId) && nodeRef.equals(that.nodeRef);
    }

    @Override public int hashCode()
    {
        return Objects.hash(requestId, nodeRef);
    }

    @Override public String toString()
    {
        return '{' +
            "\"requestId\":\"" + requestId + '\"' +
            ", \"transformName\":\"" + transformName + '\"' +
            ", \"nodeRef\":\"" + nodeRef + '\"' +
            ", \"targetMediaType\":\"" + targetMediaType + '\"' +
            ", \"transformOptions\":" + transformOptions +
            ", \"clientData\":\"" + clientData + '\"' +
            ", \"replyQueue\":\"" + replyQueue + '\"' +
            '}';
    }

    public static class Builder {
        private final TransformRequest request;

        private Builder()
        {
            this.request = new TransformRequest();
        }

        public TransformRequest.Builder withRequestId(final String requestId)
        {
            this.request.requestId = requestId;
            return this;
        }

        public TransformRequest.Builder withTransformName(final String transformName)
        {
            this.request.transformName = transformName;
            return this;
        }

        public TransformRequest.Builder withNodeRef(final String nodeRef)
        {
            this.request.nodeRef = nodeRef;
            return this;
        }

        public TransformRequest.Builder withTargetMediaType(final String targetMediaType)
        {
            this.request.targetMediaType = targetMediaType;
            return this;
        }

        public TransformRequest.Builder withTransformOptions(final Map<String, String> transformOptions)
        {
            this.request.transformOptions = transformOptions;
            return this;
        }

        public TransformRequest.Builder withUserData(final String userData)
        {
            this.request.clientData = userData;
            return this;
        }

        public TransformRequest.Builder withReplyQueue(final String replyQueue)
        {
            this.request.replyQueue = replyQueue;
            return this;
        }

        public TransformRequest build()
        {
            return this.request;
        }
    }
}
