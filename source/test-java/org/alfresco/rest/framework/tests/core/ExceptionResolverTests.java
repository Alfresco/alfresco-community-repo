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
package org.alfresco.rest.framework.tests.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.alfresco.repo.forms.FormNotFoundException;
import org.alfresco.repo.node.integrity.IntegrityException;
import org.alfresco.rest.framework.core.exceptions.ApiException;
import org.alfresco.rest.framework.core.exceptions.ConstraintViolatedException;
import org.alfresco.rest.framework.core.exceptions.DeletedResourceException;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.core.exceptions.ErrorResponse;
import org.alfresco.rest.framework.core.exceptions.InsufficientStorageException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.core.exceptions.NotFoundException;
import org.alfresco.rest.framework.core.exceptions.PermissionDeniedException;
import org.alfresco.rest.framework.core.exceptions.RelationshipResourceNotFoundException;
import org.alfresco.rest.framework.core.exceptions.SimpleMappingExceptionResolver;
import org.alfresco.rest.framework.core.exceptions.StaleEntityException;
import org.alfresco.rest.framework.core.exceptions.UnsupportedResourceOperationException;
import org.alfresco.rest.framework.resource.parameters.where.InvalidQueryException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:test-rest-context.xml" })
public class ExceptionResolverTests
{
    @Autowired
    SimpleMappingExceptionResolver simpleMappingExceptionResolver;
    
    
    @Test
    public void testMatchException()
    {
        ErrorResponse response = simpleMappingExceptionResolver.resolveException(new ApiException(null));
        assertNotNull(response);
        assertEquals(500, response.getStatusCode());  //default to INTERNAL_SERVER_ERROR
       
        response = simpleMappingExceptionResolver.resolveException(new InvalidArgumentException(null));
        assertEquals(400, response.getStatusCode());  //default to STATUS_BAD_REQUEST

        response = simpleMappingExceptionResolver.resolveException(new InvalidQueryException(null));
        assertEquals(400, response.getStatusCode());  //default to STATUS_BAD_REQUEST
        
        response = simpleMappingExceptionResolver.resolveException(new NotFoundException(null));
        assertEquals(404, response.getStatusCode());  //default to STATUS_NOT_FOUND
        
        response = simpleMappingExceptionResolver.resolveException(new EntityNotFoundException(null));
        assertEquals(404, response.getStatusCode());  //default to STATUS_NOT_FOUND

        response = simpleMappingExceptionResolver.resolveException(new RelationshipResourceNotFoundException(null, null));
        assertEquals(404, response.getStatusCode());  //default to STATUS_NOT_FOUND

        response = simpleMappingExceptionResolver.resolveException(new PermissionDeniedException(null));
        assertEquals(403, response.getStatusCode());  //default to STATUS_FORBIDDEN

        response = simpleMappingExceptionResolver.resolveException(new UnsupportedResourceOperationException(null));
        assertEquals(405, response.getStatusCode());  //default to STATUS_METHOD_NOT_ALLOWED

        response = simpleMappingExceptionResolver.resolveException(new DeletedResourceException(null));
        assertEquals(405, response.getStatusCode());  //default to STATUS_METHOD_NOT_ALLOWED
        
        response = simpleMappingExceptionResolver.resolveException(new ConstraintViolatedException(null));
        assertEquals(409, response.getStatusCode());  //default to STATUS_CONFLICT    
        
        response = simpleMappingExceptionResolver.resolveException(new StaleEntityException(null));
        assertEquals(409, response.getStatusCode());  //default to STATUS_CONFLICT    
        
        //Try a random exception
        response = simpleMappingExceptionResolver.resolveException(new WebScriptException(null));
        assertNull(response);
        
        //Try a random exception
        response = simpleMappingExceptionResolver.resolveException(new FormNotFoundException(null));
        assertNull(response);

        response = simpleMappingExceptionResolver.resolveException(new InsufficientStorageException(null));
        assertEquals(507, response.getStatusCode());

        response = simpleMappingExceptionResolver.resolveException(new IntegrityException(null));
        assertEquals(422, response.getStatusCode());
        
    }
}
