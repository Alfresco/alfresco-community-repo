/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
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
package org.alfresco.rest.api.tests.client;


import org.alfresco.rest.framework.core.exceptions.ConstraintViolatedException;
import org.alfresco.rest.framework.core.exceptions.DefaultExceptionResolver;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.core.exceptions.NotFoundException;
import org.alfresco.rest.framework.core.exceptions.PermissionDeniedException;
import org.alfresco.rest.framework.core.exceptions.RelationshipResourceNotFoundException;
import org.alfresco.rest.framework.core.exceptions.RequestEntityTooLargeException;
import org.alfresco.rest.framework.core.exceptions.StaleEntityException;
import org.alfresco.rest.framework.core.exceptions.UnsupportedMediaTypeException;
import org.alfresco.rest.framework.core.exceptions.UnsupportedResourceOperationException;
import org.alfresco.rest.framework.resource.parameters.InvalidSelectException;
import org.alfresco.rest.framework.resource.parameters.where.InvalidQueryException;
import org.springframework.extensions.webscripts.Status;

public enum RestErrorResponseDefault {
	    
    DEFAULT_API_EXCEPTION("org.alfresco.rest.framework.core.exceptions.ApiException",  Status.STATUS_EXPECTATION_FAILED, DefaultExceptionResolver.DEFAULT_MESSAGE_ID),
    INVALID_QUERY_EXCEPTION("org.alfresco.rest.framework.core.exceptions.InvalidArgumentException", Status.STATUS_BAD_REQUEST, InvalidQueryException.DEFAULT_MESSAGE_ID),
    INVALID_SELECT_EXCEPTION("org.alfresco.rest.framework.core.exceptions.InvalidArgumentException", Status.STATUS_BAD_REQUEST, InvalidSelectException.DEFAULT_MESSAGE_ID),
    INVALID_ARGUMENT_EXCEPTION("org.alfresco.rest.framework.core.exceptions.InvalidArgumentException", Status.STATUS_BAD_REQUEST, InvalidArgumentException.DEFAULT_MESSAGE_ID),
    NOT_FOUND_EXCEPTION("org.alfresco.rest.framework.core.exceptions.NotFoundException", Status.STATUS_NOT_FOUND, NotFoundException.DEFAULT_MESSAGE_ID),
    ENTITY_NOT_FOUND_EXCEPTION("org.alfresco.rest.framework.core.exceptions.EntityNotFoundException", Status.STATUS_NOT_FOUND, EntityNotFoundException.DEFAULT_MESSAGE_ID),
    RELATIONSHIP_RESOURCE_NOT_FOUND_EXCEPTION("org.alfresco.rest.framework.core.exceptions.RelationshipResourceNotFoundException", Status.STATUS_NOT_FOUND, RelationshipResourceNotFoundException.DEFAULT_MESSAGE_ID),
    PERMISSION_DENIED_EXCEPTION("org.alfresco.rest.framework.core.exceptions.PermissionDeniedException",Status.STATUS_FORBIDDEN,PermissionDeniedException.DEFAULT_MESSAGE_ID),
    UNSUPPORTED_RESOURCE_OPERATION_EXCEPTION("org.alfresco.rest.framework.core.exceptions.UnsupportedResourceOperationException",Status.STATUS_METHOD_NOT_ALLOWED,UnsupportedResourceOperationException.DEFAULT_MESSAGE_ID),
    CONSTRAINT_VIOLATED_EXCEPTION("org.alfresco.rest.framework.core.exceptions.ConstraintViolatedException", Status.STATUS_CONFLICT,ConstraintViolatedException.DEFAULT_MESSAGE_ID),
    STALE_ENTITY_EXCEPTION("org.alfresco.rest.framework.core.exceptions.StaleEntityException",Status.STATUS_CONFLICT,StaleEntityException.DEFAULT_MESSAGE_ID ),
    REQUEST_ENTITY_TOO_LARGE_EXCEPTION("org.alfresco.rest.framework.core.exceptions.RequestEntityTooLargeException", Status.STATUS_REQUEST_ENTITY_TOO_LARGE, RequestEntityTooLargeException.DEFAULT_MESSAGE_ID),
    UNSUPPORTED_MEDIA_TYPE_EXCEPTION("org.alfresco.rest.framework.core.exceptions.UnsupportedMediaTypeException", Status.STATUS_UNSUPPORTED_MEDIA_TYPE,UnsupportedMediaTypeException.DEFAULT_MESSAGE_ID);

    private String exceptionClass;
    private int statusCode;
    private String defaultMessage;

    public String getDefaultMessage()
    {
        return defaultMessage;
    }

    private RestErrorResponseDefault(String exceptionClass, int statusCode, String defaultMessage)
    {

        this.exceptionClass = exceptionClass;
        this.statusCode = statusCode;
        this.defaultMessage = defaultMessage;
    }

    public int getStatusCode()
    {
        return this.statusCode;
    }

    public String getExceptionClass()
    {
        return exceptionClass;
    }

}
