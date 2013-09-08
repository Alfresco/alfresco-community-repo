/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.repo.webdav;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * Unit tests for the {@link GetMethod} class.
 *  
 * @author Matt Ward
 */
@RunWith(MockitoJUnitRunner.class)
public class GetMethodTest
{
    private GetMethod getMethod;
    private MockHttpServletRequest req;
    private @Mock HttpServletResponse resp;
    private @Mock WebDAVHelper davHelper;
    private NodeRef rootNode;
    private @Mock ContentReader reader;
    private @Mock FileInfo fileInfo;
    private @Mock Log logger;
    private @Mock ServiceRegistry serviceRegistry;
    private @Mock ContentService contentService;
    
    @Before
    public void setUp() throws Exception
    {
        getMethod = new GetMethod();
        req = new MockHttpServletRequest();
        rootNode = new NodeRef("workspace://SpacesStore/node-id");
        getMethod.setDetails(req, resp, davHelper, rootNode);
        getMethod.logger = logger;
        
        when(reader.getMimetype()).thenReturn("text/plain");
        when(logger.isErrorEnabled()).thenReturn(true);
        when(logger.isDebugEnabled()).thenReturn(true);
    }

    
    @Test
    public void readByteRangeContentDoesNotLogSocketExceptions() throws IOException, WebDAVServerException
    {
        // getContentService() during range request
        when(davHelper.getServiceRegistry()).thenReturn(serviceRegistry);
        when(serviceRegistry.getContentService()).thenReturn(contentService);
        
        req.addHeader("Range", "bytes=500-1500");
        getMethod.parseRequestHeaders();
        SocketException sockEx = new SocketException("Client aborted connection");
        IOException ioEx = new IOException("Wrapping the socket exception.", sockEx);
        
        // Somewhere along the line a client disconnect will happen (IOException)
        when(resp.getOutputStream()).thenThrow(ioEx);
        
        try
        {
            getMethod.readContent(fileInfo, reader);
            fail("Exception should have been thrown.");
        }
        catch(WebDAVServerException e)
        {
            verify(logger, never()).error(anyString(), same(ioEx));
            verify(logger).debug(anyString(), same(ioEx));
            assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getHttpStatusCode());
            assertNull(e.getCause()); // Avoids logging stacking trace
        }
    }
    
    @Test
    public void readByteRangeContentLogsLegitimateExceptions() throws IOException, WebDAVServerException
    {
        // getContentService() during range request
        when(davHelper.getServiceRegistry()).thenReturn(serviceRegistry);
        when(serviceRegistry.getContentService()).thenReturn(contentService);
        
        req.addHeader("Range", "bytes=500-1500");
        getMethod.parseRequestHeaders();
        RuntimeException rEx = new RuntimeException("Some sort of proper error");
        IOException ioEx = new IOException("Wrapping the exception.", rEx);
        
        // Somewhere along the line a client disconnect will happen (IOException)
        when(resp.getOutputStream()).thenThrow(ioEx);
        
        try
        {
            getMethod.readContent(fileInfo, reader);
            fail("Exception should have been thrown.");
        }
        catch(WebDAVServerException e)
        {
            verify(logger).error(anyString(), same(ioEx));
            verify(logger, never()).debug(anyString(), same(ioEx));
            assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getHttpStatusCode());
            assertNull(e.getCause()); // Avoids logging stacking trace elsewhere
        }
    }
    
    @Test
    public void readContentDoesNotLogSocketExceptions() throws IOException, WebDAVServerException
    {
        SocketException sockEx = new SocketException("Client aborted connection");
        ContentIOException contentEx = new ContentIOException("Wrapping the socket exception.", sockEx);
        
        // Reader.getContent() will throw a ContentIOException when a client aborts.
        doThrow(contentEx).when(reader).getContent(any(OutputStream.class));
        
        try
        {
            getMethod.readContent(fileInfo, reader);
            fail("Exception should have been thrown.");
        }
        catch(WebDAVServerException e)
        {
            verify(logger, never()).error(anyString(), same(contentEx));
            // Error will only be seen when debugging.
            verify(logger).debug(anyString(), same(contentEx));
            assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getHttpStatusCode());
            assertNull(e.getCause()); // Avoids logging stacking trace
        }
    }
    
    @Test
    public void readContentLogsLegitimateExceptions() throws IOException, WebDAVServerException
    {
        RuntimeException rEx = new RuntimeException("Some sort of proper error");
        ContentIOException contentEx = new ContentIOException("Wrapping the exception.", rEx);
        
        doThrow(contentEx).when(reader).getContent(any(OutputStream.class));
        
        try
        {
            getMethod.readContent(fileInfo, reader);
            fail("Exception should have been thrown.");
        }
        catch(WebDAVServerException e)
        {
            verify(logger).error(anyString(), same(contentEx));
            verify(logger, never()).debug(anyString(), same(contentEx));
            assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getHttpStatusCode());
            assertNull(e.getCause()); // Avoids logging stacking trace
        }
    }
}
