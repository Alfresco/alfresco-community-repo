/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2024 Alfresco Software Limited
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

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.httpclient.HttpClientFactory;
import org.alfresco.repo.web.filter.beans.DependencyInjectedFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * This filter protects the solr callback urls by verifying a shared secret on the request header if
 * the secureComms property is set to "secret". If it is set to "https", this will will just verify
 * that the request came in through a "secure" tomcat connector. (but it will not validate the certificate
 * on the request; this done in a different filter).
 *
 * @since 4.0
 *
 */
public class SOLRAuthenticationFilter implements DependencyInjectedFilter, InitializingBean
{
	public static enum SecureCommsType
	{
		HTTPS, SECRET, NONE;
		
		public static SecureCommsType getType(String type)
		{
			if(type.equalsIgnoreCase("https"))
			{
				return HTTPS;
			}
			else if(type.equalsIgnoreCase("secret"))
			{
				return SECRET;
			}
			else if(type.equalsIgnoreCase("none"))
			{
				return NONE;
			}
			else
			{
				throw new IllegalArgumentException("Invalid communications type");
			}
		}
	};

    // Logger
    private static Log logger = LogFactory.getLog(SOLRAuthenticationFilter.class);

    private SecureCommsType secureComms = SecureCommsType.HTTPS;

	private String sharedSecret;

	private String sharedSecretHeader = HttpClientFactory.DEFAULT_SHAREDSECRET_HEADER;

	public void setSecureComms(String type)
	{
		try
		{
			this.secureComms = SecureCommsType.getType(type);
		}
		catch(IllegalArgumentException e)
		{
			throw new AlfrescoRuntimeException("", e);
		}
	}

	public void setSharedSecret(String sharedSecret)
	{
		this.sharedSecret = sharedSecret;
	}

	public void setSharedSecretHeader(String sharedSecretHeader)
	{
		this.sharedSecretHeader = sharedSecretHeader;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
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
	}

	public void doFilter(ServletContext context, ServletRequest request,
			ServletResponse response, FilterChain chain) throws IOException,
			ServletException
	{
		HttpServletRequest httpRequest = (HttpServletRequest)request;
		HttpServletResponse httpResponse = (HttpServletResponse)response;

		if(secureComms == SecureCommsType.SECRET)
		{
			if(sharedSecret.equals(httpRequest.getHeader(sharedSecretHeader)))
			{
				chain.doFilter(request, response);
			}
			else
			{
				httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Authentication failure");
			}
		}
		else if(secureComms == SecureCommsType.HTTPS)
		{
			if(httpRequest.isSecure())
			{
				// https authentication; cert got verified in X509 filter
				chain.doFilter(request, response);
			}
			else
			{
				throw new AlfrescoRuntimeException("Expected a https request");
			}
		}
		else
		{
			chain.doFilter(request, response);
		}
	}

}
