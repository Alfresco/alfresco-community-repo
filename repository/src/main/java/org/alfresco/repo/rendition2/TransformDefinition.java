/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
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

import java.util.Map;

/**
 * A TransformDefinition is a transient {@link RenditionDefinition2} that provides a target mimetype and transform options just like a RenditionDefinition2. However one should be created for each transform request, so that additional client supplied data can be included and returned in the response to the client. It also identifies the reply queue to be used.
 * <p>
 *
 * When it is known that the same target mimetype and transform options are being supplied many times a transform name should be supplied so that the transform registry is able to use the name as a key into a cache used to work out if the transform is possible. The transform name is prefixed {@link #TRANSFORM_NAMESPACE} to avoid clashes with rendition names.
 *
 * @author adavis
 */
public class TransformDefinition extends RenditionDefinition2Impl
{
    public static final String TRANSFORM_NAMESPACE = "transform:";

    private final String clientData;
    private final String replyQueue;
    private final String requestId;
    private String errorMessage;

    /**
     * Constructor where the same targetMimetype and transformOptions are used in multiple calls. In such a case, how or if the transform will take place, will be cached against the transformName.
     */
    public TransformDefinition(String transformName, String targetMimetype, Map<String, String> transformOptions,
            String clientData, String replyQueue, String requestId,
            RenditionDefinitionRegistry2Impl registry)
    {
        super(convertToRenditionName(transformName), targetMimetype, transformOptions, registry);
        this.clientData = clientData;
        this.replyQueue = replyQueue;
        this.requestId = requestId;
        this.errorMessage = null;
    }

    /**
     * Constructor where the targetMimetype and transformOptions are unlikely to be repeated.
     */
    public TransformDefinition(String targetMimetype, Map<String, String> transformOptions,
            String clientData, String replyQueue, String requestId)
    {
        this(null, targetMimetype, transformOptions, clientData, replyQueue, requestId, null);
    }

    public static String convertToRenditionName(String transformName)
    {
        return transformName == null ? null : TRANSFORM_NAMESPACE + transformName;
    }

    public String getTransformName()
    {
        String renditionName = getRenditionName();
        return getTransformName(renditionName);
    }

    public static String getTransformName(String renditionName)
    {
        return renditionName == null || !renditionName.startsWith(TRANSFORM_NAMESPACE)
                ? null
                : renditionName.substring(TRANSFORM_NAMESPACE.length());
    }

    public String getClientData()
    {
        return clientData;
    }

    public String getReplyQueue()
    {
        return replyQueue;
    }

    public String getRequestId()
    {
        return requestId;
    }

    public String getErrorMessage()
    {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage)
    {
        this.errorMessage = errorMessage;
    }
}
