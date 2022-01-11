/*
 * #%L
 * Alfresco Remote API
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

package org.alfresco.web.app.servlet;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.httpclient.HttpClientFactory;
import org.alfresco.httpclient.HttpClientFactory.SecureCommsType;
import org.alfresco.web.scripts.servlet.X509ServletFilterBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * The AlfrescoX509ServletFilter implements the checkEnforce method of the X509ServletFilterBase.
 * This allows the configuration of X509 authentication to be toggled on/off through a
 * configuration outside of the web.xml.
 **/

public class AlfrescoX509ServletFilter extends X509ServletFilterBase
{

    private SecureCommsType secureComms = SecureCommsType.HTTPS;

    private String sharedSecret;

    private String sharedSecretHeader = HttpClientFactory.DEFAULT_SHAREDSECRET_HEADER;

    private static final String BEAN_GLOBAL_PROPERTIES = "global-properties";
    private static final String PROP_SECURE_COMMS = "solr.secureComms";
    private static final String PROP_SHARED_SECRET = "solr.sharedSecret";
    private static final String PROP_SHARED_SECRET_HEADER = "solr.sharedSecret.header";
    private static Log logger = LogFactory.getLog(AlfrescoX509ServletFilter.class);

    public void init(FilterConfig config) throws ServletException
    {
        WebApplicationContext wc = WebApplicationContextUtils.getRequiredWebApplicationContext(config.getServletContext());
        Properties globalProperties = (Properties) wc.getBean(BEAN_GLOBAL_PROPERTIES);
        String secureCommsProp = globalProperties.getProperty(PROP_SECURE_COMMS);
        if(secureCommsProp != null && !secureCommsProp.isEmpty()) {
            secureComms = SecureCommsType.getType(secureCommsProp);
        }
        sharedSecret = globalProperties.getProperty(PROP_SHARED_SECRET);
        sharedSecretHeader = globalProperties.getProperty(PROP_SHARED_SECRET_HEADER);
        if(secureComms == SecureCommsType.SECRET)
        {
            if(sharedSecret == null || sharedSecret.length()==0)
            {
                logger.fatal("Missing value for solr.sharedSecret configuration property. If solr.secureComms is set to \"secret\", a value for solr.sharedSecret is required. See https://docs.alfresco.com/search-services/latest/install/options/");
                throw new AlfrescoRuntimeException("Missing value for solr.sharedSecret configuration property");
            }
            if(sharedSecretHeader == null || sharedSecretHeader.length()==0)
            {
                throw new AlfrescoRuntimeException("Missing value for sharedSecretHeader");
            }
        }
        /*
        // TODO: Activate this part after OPSEXP-1163 got implemented
        if(secureComms == SecureCommsType.NONE)
        {
            if(!"true".equalsIgnoreCase(config.getInitParameter("allow-unauthenticated-solr-endpoint")))
            {
                throw new AlfrescoRuntimeException("solr.secureComms=none is no longer supported. Please use https or secret");
            }
        }
        */
        super.init(config);
    }

    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain) throws IOException, ServletException
    {
        HttpServletRequest httpRequest = (HttpServletRequest)request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        switch(secureComms) {
            case HTTPS:
                super.doFilter(request,response,chain);
                return;
            case SECRET:
                if(sharedSecret.equals(httpRequest.getHeader(sharedSecretHeader)))
                {
                    chain.doFilter(request, response);
                }
                else
                {
                    httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Authentication failure");
                }
                return;
            case NONE:
                chain.doFilter(request,response);
                return;
            default:
                httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Authentication failure");
        }
    }

    @Override
    protected boolean checkEnforce(ServletContext servletContext) throws IOException
    {
        return secureComms == SecureCommsType.HTTPS;
    }

}