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
import org.alfresco.rest.framework.core.exceptions.StaleEntityException;
import org.alfresco.rest.framework.core.exceptions.UnsupportedResourceOperationException;
import org.alfresco.rest.framework.resource.parameters.where.InvalidQueryException;
import org.alfresco.rest.framework.tools.ApiAssistant;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.servlet.http.HttpServletResponse;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:test-rest-context.xml" })
public class ExceptionResolverTests
{
    @Autowired
    ApiAssistant assistant;

    @Test
    public void testWebscriptException()
    {
        ErrorResponse response = assistant.resolveException(new WebScriptException(null));
        assertNotNull(response);
        assertEquals(500, response.getStatusCode());  //default to INTERNAL_SERVER_ERROR

        response = assistant.resolveException(new WebScriptException(HttpServletResponse.SC_UNAUTHORIZED, "Authentication failed for Web Script "));
        assertNotNull(response);
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatusCode());  //default to INTERNAL_SERVER_ERROR
    }

    //04180006 Authentication failed for Web Script org/alfresco/api/ResourceWebScript.get
    @Test
    public void testMatchException()
    {
        ErrorResponse response = assistant.resolveException(new ApiException(null));
        assertNotNull(response);
        assertEquals(500, response.getStatusCode());  //default to INTERNAL_SERVER_ERROR
       
        response = assistant.resolveException(new InvalidArgumentException(null));
        assertEquals(400, response.getStatusCode());  //default to STATUS_BAD_REQUEST

        response = assistant.resolveException(new InvalidQueryException(null));
        assertEquals(400, response.getStatusCode());  //default to STATUS_BAD_REQUEST
        
        response = assistant.resolveException(new NotFoundException(null));
        assertEquals(404, response.getStatusCode());  //default to STATUS_NOT_FOUND
        
        response = assistant.resolveException(new EntityNotFoundException(null));
        assertEquals(404, response.getStatusCode());  //default to STATUS_NOT_FOUND

        response = assistant.resolveException(new RelationshipResourceNotFoundException(null, null));
        assertEquals(404, response.getStatusCode());  //default to STATUS_NOT_FOUND

        response = assistant.resolveException(new PermissionDeniedException(null));
        assertEquals(403, response.getStatusCode());  //default to STATUS_FORBIDDEN

        response = assistant.resolveException(new UnsupportedResourceOperationException(null));
        assertEquals(405, response.getStatusCode());  //default to STATUS_METHOD_NOT_ALLOWED

        response = assistant.resolveException(new DeletedResourceException(null));
        assertEquals(405, response.getStatusCode());  //default to STATUS_METHOD_NOT_ALLOWED
        
        response = assistant.resolveException(new ConstraintViolatedException(null));
        assertEquals(409, response.getStatusCode());  //default to STATUS_CONFLICT    
        
        response = assistant.resolveException(new StaleEntityException(null));
        assertEquals(409, response.getStatusCode());  //default to STATUS_CONFLICT    

        //Try a random exception
        response = assistant.resolveException(new FormNotFoundException(null));
        assertEquals(500, response.getStatusCode());  //default to INTERNAL_SERVER_ERROR

        response = assistant.resolveException(new InsufficientStorageException(null));
        assertEquals(507, response.getStatusCode());

        response = assistant.resolveException(new IntegrityException(null));
        assertEquals(422, response.getStatusCode());
        
    }
}
