/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.opencmis;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.alfresco.repo.node.integrity.IntegrityException;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.coci.CheckOutCheckInServiceException;
import org.alfresco.service.cmr.lock.NodeLockedException;
import org.alfresco.service.cmr.model.FileExistsException;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisContentAlreadyExistsException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUpdateConflictException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisVersioningException;
import org.junit.Assert;
import org.junit.Test;

/**
 * Ensure that CMIS handles specific types of Alfresco exceptions
 * 
 * @author Derek Hulley
 * @since 4.3
 */
public class AlfrescoCmisExceptionInterceptorTest
{
    private AlfrescoCmisExceptionInterceptor interceptor = new AlfrescoCmisExceptionInterceptor();
    
    /**
     * Does the mock call ensuring that the exception is thrown
     * @throws throws the exception provided
     */
    private void doMockCall(Exception toThrow, Class<?> toCatch) throws Throwable
    {
        MethodInvocation mi = mock(MethodInvocation.class);
        when(mi.proceed()).thenThrow(toThrow);
        try
        {
            interceptor.invoke(mi);
            fail("Expected an exception to be thrown here.");
        }
        catch (Exception e)
        {
            Assert.assertEquals("Incorrect exception thrown: ", toCatch, e.getClass());
        }
    }
    
    @Test
    public void testNoException() throws Throwable
    {
        MethodInvocation mi = mock(MethodInvocation.class);
        when(mi.proceed()).thenReturn("BOB");
        interceptor.invoke(mi);
    }
    
    @Test
    public void testAuthenticationException() throws Throwable
    {
        Exception e = new AuthenticationException("x");
        Class<?> toCatch = CmisPermissionDeniedException.class;
        
        doMockCall(e, toCatch);
        doMockCall(new RuntimeException(new RuntimeException(e)), toCatch);
    }
    
    @Test
    public void testCheckOutCheckInServiceException() throws Throwable
    {
        Exception e = new CheckOutCheckInServiceException("x");
        Class<?> toCatch = CmisVersioningException.class;
        
        doMockCall(e, toCatch);
        doMockCall(new RuntimeException(new RuntimeException(e)), toCatch);
    }
    
    @Test
    public void testFileExistsException() throws Throwable
    {
        Exception e = new FileExistsException(null, null);
        Class<?> toCatch = CmisContentAlreadyExistsException.class;
        
        doMockCall(e, toCatch);
        doMockCall(new RuntimeException(new RuntimeException(e)), toCatch);
    }
    
    @Test
    public void testIntegrityException() throws Throwable
    {
        Exception e = new IntegrityException(null);
        Class<?> toCatch = CmisConstraintException.class;
        
        doMockCall(e, toCatch);
        doMockCall(new RuntimeException(new RuntimeException(e)), toCatch);
    }
    
    @Test
    public void testAccessDeniedException() throws Throwable
    {
        Exception e = new AccessDeniedException("x");
        Class<?> toCatch = CmisPermissionDeniedException.class;
        
        doMockCall(e, toCatch);
        doMockCall(new RuntimeException(new RuntimeException(e)), toCatch);
    }
    
    @Test
    public void testNodeLockedException() throws Throwable
    {
        Exception e = new NodeLockedException();
        Class<?> toCatch = CmisUpdateConflictException.class;
        
        doMockCall(e, toCatch);
        doMockCall(new RuntimeException(new RuntimeException(e)), toCatch);
    }
}
