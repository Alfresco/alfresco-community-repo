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
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.core.exceptions.NotFoundException;
import org.alfresco.rest.framework.core.exceptions.PermissionDeniedException;
import org.alfresco.rest.framework.core.exceptions.RelationshipResourceNotFoundException;
import org.alfresco.rest.framework.core.exceptions.RequestEntityTooLargeException;
import org.alfresco.rest.framework.core.exceptions.StaleEntityException;
import org.alfresco.rest.framework.core.exceptions.UnsupportedMediaTypeException;
import org.alfresco.rest.framework.core.exceptions.UnsupportedResourceOperationException;
import org.springframework.extensions.webscripts.Status;

public enum RestHTTPErrorStatus {
	
     API_EXCEPTION("org.alfresco.rest.framework.core.exceptions.ApiException",  Status.STATUS_INTERNAL_SERVER_ERROR, null),
     THROWABLE("java.lang.Throwable", Status.STATUS_INTERNAL_SERVER_ERROR, null),
     ILLEGAL_ARGUMENT_EXCEPTION("java.lang.IllegalArgumentException", Status.STATUS_BAD_REQUEST, null),
     CYCLIC_CHILD_RELATIONSHIP_EXCEPTION("org.alfresco.service.cmr.repository.CyclicChildRelationshipException",Status.STATUS_BAD_REQUEST,null),
     INVALID_ARGUMENT_EXCEPTION("org.alfresco.rest.framework.core.exceptions.InvalidArgumentException", Status.STATUS_BAD_REQUEST, InvalidArgumentException.DEFAULT_MESSAGE_ID),
     VERSION_SERVICE_EXCEPTION("org.alfresco.service.cmr.version.VersionServiceException", Status.STATUS_BAD_REQUEST,null),
     TYPE_CONVERSION_EXCEPTION("org.alfresco.service.cmr.repository.datatype.TypeConversionException", Status.STATUS_BAD_REQUEST,null),
     NOT_FOUND_EXCEPTION("org.alfresco.rest.framework.core.exceptions.NotFoundException", Status.STATUS_NOT_FOUND, NotFoundException.DEFAULT_MESSAGE_ID),
     ENTITY_NOT_FOUND_EXCEPTION("org.alfresco.rest.framework.core.exceptions.EntityNotFoundException", Status.STATUS_NOT_FOUND, EntityNotFoundException.DEFAULT_MESSAGE_ID),
     INVALID_NODEREF_EXCEPTION("org.alfresco.service.cmr.repository.InvalidNodeRefException", Status.STATUS_NOT_FOUND, null),
     RELATIONSHIP_RESOURCE_NOT_FOUND_EXCEPTION("org.alfresco.rest.framework.core.exceptions.RelationshipResourceNotFoundException", Status.STATUS_NOT_FOUND, RelationshipResourceNotFoundException.DEFAULT_MESSAGE_ID),
     VERSION_DOES_NOT_EXIST_EXCEPTION("org.alfresco.service.cmr.version.VersionDoesNotExistException",Status.STATUS_NOT_FOUND, null),
     CLIENT_APP_NOT_FOUND_EXCEPTION("org.alfresco.repo.client.config.ClientAppNotFoundException",Status.STATUS_NOT_FOUND,null),
     PERMISSION_DENIED_EXCEPTION("org.alfresco.rest.framework.core.exceptions.PermissionDeniedException",Status.STATUS_FORBIDDEN,PermissionDeniedException.DEFAULT_MESSAGE_ID),
	 UNKNOWN_AUTHORITY_EXCEPTION("org.alfresco.repo.security.authority.UnknownAuthorityException", Status.STATUS_NOT_FOUND,null),
	 ACCESS_DENIED_EXCEPTION("org.alfresco.repo.security.permissions.AccessDeniedException",Status.STATUS_FORBIDDEN,null),
	 UNSUPPORTED_RESOURCE_OPERATION_EXCEPTION("org.alfresco.rest.framework.core.exceptions.UnsupportedResourceOperationException",Status.STATUS_METHOD_NOT_ALLOWED,UnsupportedResourceOperationException.DEFAULT_MESSAGE_ID),
	 CONSTRAINT_VIOLATED_EXCEPTION("org.alfresco.rest.framework.core.exceptions.ConstraintViolatedException", Status.STATUS_CONFLICT,ConstraintViolatedException.DEFAULT_MESSAGE_ID),
	 NODE_LOCKED_EXCEPTION("org.alfresco.service.cmr.lock.NodeLockedException",Status.STATUS_CONFLICT,null),
	 UNABLE_TO_AQUIRE_LOCK_EXCEPTION("org.alfresco.service.cmr.lock.UnableToAquireLockException", 422,null),
	 UNABLE_TO_RELEASE_LOCK_EXCEPTION("org.alfresco.service.cmr.lock.UnableToReleaseLockException",422,null),
	 DUPLICATE_CHILD_NODE_NAME_EXCEPTION("org.alfresco.service.cmr.repository.DuplicateChildNodeNameException",Status.STATUS_CONFLICT,null),
	 STALE_ENTITY_EXCEPTION("org.alfresco.rest.framework.core.exceptions.StaleEntityException",Status.STATUS_CONFLICT,StaleEntityException.DEFAULT_MESSAGE_ID ),
	 STATUS_REQUEST_ENTITY_TOO_LARGE("org.alfresco.repo.content.ContentLimitViolationException",Status.STATUS_REQUEST_ENTITY_TOO_LARGE,null),
	 REQUEST_ENTITY_TOO_LARGE_EXCEPTION("org.alfresco.rest.framework.core.exceptions.RequestEntityTooLargeException", Status.STATUS_REQUEST_ENTITY_TOO_LARGE, RequestEntityTooLargeException.DEFAULT_MESSAGE_ID),
	 DISABLED_SERVICE_EXCEPTION("org.alfresco.rest.framework.core.exceptions.DisabledServiceException",Status.STATUS_NOT_IMPLEMENTED,null),
	 CONTENT_QUOTA_EXCEPTION("org.alfresco.service.cmr.usage.ContentQuotaException", 507, null),
	 INSUFFICIENT_STORAGE_EXCEPTION("org.alfresco.rest.framework.core.exceptions.InsufficientStorageException",507,null),
	 INTEGRITY_EXCEPTION("org.alfresco.repo.node.integrity.IntegrityException",422,null),
	 SITE_SERVICE_EXCEPTION("org.alfresco.repo.site.SiteServiceException",422,null),
	 UNSUPPORTED_MEDIA_TYPE_EXCEPTION("org.alfresco.rest.framework.core.exceptions.UnsupportedMediaTypeException", Status.STATUS_UNSUPPORTED_MEDIA_TYPE,UnsupportedMediaTypeException.DEFAULT_MESSAGE_ID),
	 INVALID_MEDIA_TYPE_EXCEPTION("org.springframework.http.InvalidMediaTypeException",Status.STATUS_UNSUPPORTED_MEDIA_TYPE,null);

     
	String exceptionClass;
	int statusCode;
	String defaultMessage;

	public String getDefaultMessage() {
		return defaultMessage;
	}

	private RestHTTPErrorStatus(String exceptionClass, int statusCode, String defaultMessage) {

		this.exceptionClass = exceptionClass;
		this.statusCode = statusCode;
		this.defaultMessage = defaultMessage;
	}

	public int getStatusCode() {
		return this.statusCode;
	}

	public String getExceptionClass() {
		return exceptionClass;
	}
	
}
