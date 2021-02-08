/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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
package org.alfresco.repo.web.scripts.solr;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.web.filter.beans.DependencyInjectedFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * This filter protects the solr callback urls by verifying MACs on requests and encrypting responses
 * and generating MACs on responses, if the secureComms property is set to "md5". If it is set to "https"
 * or "none", the filter does nothing to the request and response.
 * 
 * @since 4.0
 *
 */
public class SOLRAuthenticationFilter implements DependencyInjectedFilter, InitializingBean
{
	public static enum SecureCommsType
	{
		HTTPS, APIKEY;
		
		public static SecureCommsType getType(String type)
		{
			if(type.equalsIgnoreCase("https"))
			{
				return HTTPS;
			}
			else if(type.equalsIgnoreCase("apikey"))
			{
				return APIKEY;
			}
			else
			{
				throw new IllegalArgumentException("Invalid communications type. Expecting \"https\" or \"apikey\".");
			}
		}
	};

    // Logger
    private static Log logger = LogFactory.getLog(SOLRAuthenticationFilter.class);

    private SecureCommsType secureComms = SecureCommsType.HTTPS;

    private String apiKey;

	private String apiKeyHeader = DEFAULT_APIKEY_HEADER;

	private static final String DEFAULT_APIKEY_HEADER = "X-Alfresco-Search-ApiKey";

	public void setSecureComms(String type)
	{
		try
		{
			this.secureComms = SecureCommsType.getType(type);
		}
		catch(IllegalArgumentException e)
		{
			logger.fatal("Invalid configuration for solr.secureComms property. The only allowed values are \"https\" or \"apikey\". See https://docs.alfresco.com/search-enterprise/tasks/solr-install-withoutSSL.html");
			throw new AlfrescoRuntimeException("Invalid value for solr.secureComms configuration property. The only supported values are \"https\" or \"apikey\".", e);
		}
	}

	public void setApiKey(String apiKey)
	{
		this.apiKey = apiKey;
	}

	public void setApiKeyHeader(String apiKeyHeader)
	{
		this.apiKeyHeader = apiKeyHeader;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		if(secureComms == SecureCommsType.APIKEY)
		{
			if(apiKey == null || apiKey.length()==0)
			{
				logger.fatal("Missing value for solr.apiKey configuration property. If solr.secureComms is set to \"https\", a value for solr.apiKey is required. See https://docs.alfresco.com/search-enterprise/tasks/solr-install-withoutSSL.html");
				throw new AlfrescoRuntimeException("Missing value for solr.apiKey configuration property");
			}
			if(apiKeyHeader == null || apiKeyHeader.length()==0)
			{
				throw new AlfrescoRuntimeException("Missing value for apiKeyHeader");
			}
		}
	}

	public void doFilter(ServletContext context, ServletRequest request,
						 ServletResponse response, FilterChain chain) throws IOException,
			ServletException
	{
		HttpServletRequest httpRequest = (HttpServletRequest)request;
		HttpServletResponse httpResponse = (HttpServletResponse)response;

		if(secureComms == SecureCommsType.HTTPS)
		{
			if(httpRequest.isSecure())
			{
				// https authentication
				chain.doFilter(request, response);
			}
			else
			{
				httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Authentication failure");
			}
		}
		else if(secureComms == SecureCommsType.APIKEY)
		{
			if(apiKey.equals(httpRequest.getHeader(apiKeyHeader)))
			{
				chain.doFilter(request, response);
			}
			else
			{
				httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Authentication failure");
			}
		}
		else
		{
			httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Authentication failure");
		}
	}

}
