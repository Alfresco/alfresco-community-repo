/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail. Otherwise, the software is
 * provided under the following open source license terms:
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.web.app.servlet;

import static junit.framework.TestCase.assertEquals;

import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;

import java.util.Properties;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.httpclient.HttpClientFactory.SecureCommsType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *  Unit tests for {@link AlfrescoX509ServletFilter}.
 */
public class AlfrescoX509ServletFilterTest
{

    private static final String BEAN_GLOBAL_PROPERTIES = "global-properties";
    private static final String PROP_SECURE_COMMS = "solr.secureComms";
    private static final String PROP_SHARED_SECRET = "solr.sharedSecret";
    private static final String PROP_SHARED_SECRET_HEADER = "solr.sharedSecret.header";
    private static final String SHARED_SECRET_HEADER = "X-Alfresco-Search-Secret";
    private static final String SECRET = "secret";
    private static final String ALLOW_UNAUTHORIZED_SOLR_ENDPOINT = "allow-unauthenticated-solr-endpoint";

    private static final String MISSING_SHARED_SECRET_EXCEPTION_MSG = "Missing value for solr.sharedSecret configuration property";
    private static final String MISSING_SHARED_SECRET_HEADER_EXCEPTION_MSG = "Missing value for sharedSecretHeader";
    private static final String SECURE_COMMS_NONE_IS_NOT_SUPPORTED_EXCEPTION_MSG = "solr.secureComms=none is no longer supported. Please use https or secret";

    private FilterConfig filterConfig;
    private Properties globalProperties;
    private AlfrescoX509ServletFilter filter;

    @Before
    public void before()
    {
        FilterConfig filterConfig = Mockito.mock(FilterConfig.class);
        WebApplicationContext webApplicationContext = Mockito.mock(WebApplicationContext.class);
        ServletContext servletContext = Mockito.mock(ServletContext.class);
        Properties globalProperties = Mockito.mock(Properties.class);

        Mockito.when(servletContext.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE)).thenReturn(webApplicationContext);
        Mockito.when(filterConfig.getServletContext()).thenReturn(servletContext);
        Mockito.when(WebApplicationContextUtils.getRequiredWebApplicationContext(filterConfig.getServletContext())).thenReturn(webApplicationContext);
        Mockito.when(webApplicationContext.getBean(BEAN_GLOBAL_PROPERTIES)).thenReturn(globalProperties);

        this.filterConfig = filterConfig;
        this.globalProperties = globalProperties;
        this.filter = new AlfrescoX509ServletFilter();
    }

    @Test (expected = AlfrescoRuntimeException.class)
    public void testSharedSecretIsEmpty() throws ServletException
    {
        Mockito.when(globalProperties.getProperty(PROP_SECURE_COMMS)).thenReturn(SecureCommsType.SECRET.name());
        Mockito.when(globalProperties.getProperty(PROP_SHARED_SECRET)).thenReturn("");
        Mockito.when(globalProperties.getProperty(PROP_SHARED_SECRET_HEADER)).thenReturn(SHARED_SECRET_HEADER);

        try
        {
            filter.init(filterConfig);
        }
        catch (AlfrescoRuntimeException ex)
        {
            assertEquals(MISSING_SHARED_SECRET_EXCEPTION_MSG, ex.getMsgId());
            throw ex;
        }
    }

    @Test (expected = AlfrescoRuntimeException.class)
    public void testSharedSecretIsNull() throws ServletException
    {
        Mockito.when(globalProperties.getProperty(PROP_SECURE_COMMS)).thenReturn(SecureCommsType.SECRET.name());
        Mockito.when(globalProperties.getProperty(PROP_SHARED_SECRET)).thenReturn(null);
        Mockito.when(globalProperties.getProperty(PROP_SHARED_SECRET_HEADER)).thenReturn(SHARED_SECRET_HEADER);

        try
        {
            filter.init(filterConfig);
        }
        catch (AlfrescoRuntimeException ex)
        {
            assertEquals(MISSING_SHARED_SECRET_EXCEPTION_MSG, ex.getMsgId());
            throw ex;
        }
    }

    @Test (expected = AlfrescoRuntimeException.class)
    public void testSharedSecretHeaderIsEmpty() throws ServletException
    {
        Mockito.when(globalProperties.getProperty(PROP_SECURE_COMMS)).thenReturn(SecureCommsType.SECRET.name());
        Mockito.when(globalProperties.getProperty(PROP_SHARED_SECRET)).thenReturn(SECRET);
        Mockito.when(globalProperties.getProperty(PROP_SHARED_SECRET_HEADER)).thenReturn("");

        try
        {
            filter.init(filterConfig);
        }
        catch (AlfrescoRuntimeException ex)
        {
            assertEquals(MISSING_SHARED_SECRET_HEADER_EXCEPTION_MSG, ex.getMsgId());
            throw ex;
        }
    }

    @Test (expected = AlfrescoRuntimeException.class)
    public void testSharedSecretHeaderIsNull() throws ServletException
    {
        Mockito.when(globalProperties.getProperty(PROP_SECURE_COMMS)).thenReturn(SecureCommsType.SECRET.name());
        Mockito.when(globalProperties.getProperty(PROP_SHARED_SECRET)).thenReturn(SECRET);
        Mockito.when(globalProperties.getProperty(PROP_SHARED_SECRET_HEADER)).thenReturn("");

        try
        {
            filter.init(filterConfig);
        }
        catch (AlfrescoRuntimeException ex)
        {
            assertEquals(MISSING_SHARED_SECRET_HEADER_EXCEPTION_MSG, ex.getMsgId());
            throw ex;
        }
    }

    @Test
    public void testSharedSecretProperlyConfigured() throws ServletException
    {
        Mockito.when(globalProperties.getProperty(PROP_SECURE_COMMS)).thenReturn(SecureCommsType.SECRET.name());
        Mockito.when(globalProperties.getProperty(PROP_SHARED_SECRET)).thenReturn(SECRET);
        Mockito.when(globalProperties.getProperty(PROP_SHARED_SECRET_HEADER)).thenReturn(SHARED_SECRET_HEADER);

        filter.init(filterConfig);
    }

    @Test (expected = AlfrescoRuntimeException.class)
    public void testSecureCommsNoneAndNotAllowUnauthenticatedSolrEndpoint() throws ServletException
    {
        Mockito.when(globalProperties.getProperty(PROP_SECURE_COMMS)).thenReturn(SecureCommsType.NONE.name());
        Mockito.when(filterConfig.getInitParameter(ALLOW_UNAUTHORIZED_SOLR_ENDPOINT)).thenReturn("false");

        try
        {
            filter.init(filterConfig);
        }
        catch (AlfrescoRuntimeException ex)
        {
            assertEquals(SECURE_COMMS_NONE_IS_NOT_SUPPORTED_EXCEPTION_MSG, ex.getMsgId());
            throw ex;
        }
    }

    @Test
    public void testSecureCommsNoneAndAllowUnauthenticatedSolrEndpoint() throws ServletException
    {
        Mockito.when(globalProperties.getProperty(PROP_SECURE_COMMS)).thenReturn(SecureCommsType.NONE.name());
        Mockito.when(filterConfig.getInitParameter(ALLOW_UNAUTHORIZED_SOLR_ENDPOINT)).thenReturn("true");

        filter.init(filterConfig);
    }

}
