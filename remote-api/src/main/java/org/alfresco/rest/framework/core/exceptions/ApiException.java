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

import java.util.Map;

import org.springframework.extensions.surf.exception.PlatformRuntimeException;

/**
 * Base exception for errors in the API framework.
 * 
 * In general, we don't want developers having to think about http status codes or rendering responses to the user.
 * This exception hierarchy is designed to abstract the http logic away so the developer can concentrate on implementing
 * services logic.  The framework will attempt to render errors in a consistent way.
 * 
 * Default status is STATUS_INTERNAL_SERVER_ERROR = 500.
 *
 * @author Gethin James
 */
public class ApiException extends PlatformRuntimeException
{
    private static final long serialVersionUID = 156335194944891591L;
    
    private Map<String,Object> additionalState;
    private String msgId;
    
    public ApiException(String msgId)
    {
        super(msgId);
        this.msgId = msgId;
    }

    public ApiException(String msgId, Object[] msgParams)
    {
        super(msgId, msgParams);
        this.msgId = msgId;
    }
    
    public ApiException(String msgId, Throwable cause)
    {
        super(msgId, cause);
        this.msgId = msgId;
    }

    public ApiException(String msgId, String message)
    {
        super(message);
        this.msgId = msgId;
    }

    public ApiException(String msgId, String message, Throwable cause)
    {
        super(message, cause);
        this.msgId = msgId;
    }
    
    public ApiException(String msgId, Throwable cause, Map<String,Object> additionalState)
    {
        super(msgId, cause);
        this.msgId = msgId;
        this.additionalState = additionalState;
    }
    
    public ApiException(String msgId, Map<String,Object> additionalState)
    {
        super(msgId);
        this.msgId = msgId;
        this.additionalState = additionalState;
    }

    /**
     * A free-form object that contains any additional state that the developer thinks might be relevant for troubleshooting.
     * This object will be rendered as JSON.
     */
    public Map<String,Object> getAdditionalState()
    {
        return this.additionalState;
    }

    /**
     * Returns the message id key.
     * @return String messageId
     */
    public String getMsgId()
    {
        return this.msgId;
    }

}
