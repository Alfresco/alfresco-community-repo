/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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

/**
 * This filter protects the solr callback urls by verifying MACs on requests and encrypting responses
 * and generating MACs on responses, if the secureComms property is set to "md5". If it is set to "https"
 * or "none", the filter does nothing to the request and response.
 * 
 * @since 4.0
 *
 */
public class SOLRAuthenticationFilter implements DependencyInjectedFilter
{
	public static enum SecureCommsType
	{
		HTTPS, NONE;
		
		public static SecureCommsType getType(String type)
		{
			if(type.equalsIgnoreCase("https"))
			{
				return HTTPS;
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

	public void doFilter(ServletContext context, ServletRequest request,
			ServletResponse response, FilterChain chain) throws IOException,
			ServletException
	{
		HttpServletRequest httpRequest = (HttpServletRequest)request;
		HttpServletResponse httpResponse = (HttpServletResponse)response;

/*		if(secureComms == SecureCommsType.ALFRESCO)
		{
			// Need to get as a byte array because we need to read the request twice, once for authentication
			// and again by the web service.
			SOLRHttpServletRequestWrapper requestWrapper = new SOLRHttpServletRequestWrapper(httpRequest, encryptionUtils);
	
			if(logger.isDebugEnabled())
			{
				logger.debug("Authenticating " + httpRequest.getRequestURI());
			}
	
			if(encryptionUtils.authenticate(httpRequest, requestWrapper.getDecryptedBody()))
			{
				try
				{
					OutputStream out = response.getOutputStream();
	
					GenericResponseWrapper responseWrapper = new GenericResponseWrapper(httpResponse);
	
					// TODO - do I need to chain to other authenticating filters - probably not?
					// Could also remove sending of credentials with http request
					chain.doFilter(requestWrapper, responseWrapper);
	
					Pair<byte[], AlgorithmParameters> pair = encryptor.encrypt(KeyProvider.ALIAS_SOLR, null, responseWrapper.getData());
	
					encryptionUtils.setResponseAuthentication(httpRequest, httpResponse, responseWrapper.getData(), pair.getSecond());

					httpResponse.setHeader("Content-Length", Long.toString(pair.getFirst().length));
					out.write(pair.getFirst());
					out.close();
				}
				catch(Exception e)
				{
					throw new AlfrescoRuntimeException("", e);
				}
			}
			else
			{
				httpResponse.setStatus(401);
			}
		}
		else */if(secureComms == SecureCommsType.HTTPS)
		{
			if(httpRequest.isSecure())
			{
				// https authentication
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

    protected boolean validateTimestamp(String timestampStr)
    {
    	if(timestampStr == null || timestampStr.equals(""))
    	{
    		throw new AlfrescoRuntimeException("Missing timestamp on request");
    	}
    	long timestamp = -1;
    	try
    	{
    		timestamp = Long.valueOf(timestampStr);
    	}
    	catch(NumberFormatException e)
    	{
    		throw new AlfrescoRuntimeException("Invalid timestamp on request");
    	}
    	if(timestamp == -1)
    	{
    		throw new AlfrescoRuntimeException("Invalid timestamp on request");
    	}
    	long currentTime = System.currentTimeMillis();
    	return((currentTime - timestamp) < 30 * 1000); // 5s
    }
    
/*    private static class SOLRHttpServletRequestWrapper extends HttpServletRequestWrapper
    {
    	private byte[] body;

    	SOLRHttpServletRequestWrapper(HttpServletRequest req, EncryptionUtils encryptionUtils) throws IOException
    	{
    		super(req);
    		this.body = encryptionUtils.decryptBody(req);
    	}

    	byte[] getDecryptedBody()
    	{
    		return body;
    	}

    	public ServletInputStream getInputStream()
    	{
    		final InputStream in = (body != null ? new ByteArrayInputStream(body) : null);
    		return new ServletInputStream()
    		{
				public int read() throws IOException
				{
					if(in == null)
					{
						return -1;
					}
					else
					{
						int i = in.read();
						if(i == -1)
						{
							in.close();
						}
						return i;
					}
				}
    		};
    	}
    }*/
    
    private static class ByteArrayServletOutputStream extends ServletOutputStream
    {
    	private ByteArrayOutputStream out = new ByteArrayOutputStream();

    	ByteArrayServletOutputStream()
    	{
    	}

    	public byte[] getData()
    	{
    		return out.toByteArray();
    	}
    	
		@Override
		public void write(int b) throws IOException
		{
			out.write(b);
		}
    }
    
    public static class GenericResponseWrapper extends HttpServletResponseWrapper { 
    	private ByteArrayServletOutputStream output;
    	private int contentLength;
    	private String contentType;

    	public GenericResponseWrapper(HttpServletResponse response) { 
    		super(response);
    		output = new ByteArrayServletOutputStream();
    	} 

    	public byte[] getData() { 
    		return output.getData(); 
    	} 

    	public ServletOutputStream getOutputStream() { 
    		return output; 
    	} 

    	public PrintWriter getWriter() { 
    		return new PrintWriter(getOutputStream(),true); 
    	} 

    	public void setContentLength(int length) { 
    		this.contentLength = length;
    		super.setContentLength(length); 
    	} 

    	public int getContentLength() { 
    		return contentLength; 
    	} 

    	public void setContentType(String type) { 
    		this.contentType = type;
    		super.setContentType(type); 
    	} 


    	public String getContentType() { 
    		return contentType; 
    	} 
    } 
}
