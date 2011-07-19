package org.alfresco.repo.web.scripts.solr;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.security.AlgorithmParameters;

import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.alfresco.encryption.EncryptionUtils;
import org.alfresco.encryption.Encryptor;
import org.alfresco.encryption.KeyProvider;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.web.filter.beans.DependencyInjectedFilter;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SOLRAuthenticationFilter implements DependencyInjectedFilter
{
    // Logger
    private static Log logger = LogFactory.getLog(Encryptor.class);

    private boolean enabled = true;
    private Encryptor encryptor;
    private EncryptionUtils encryptionUtils;

    public void setEncryptor(Encryptor encryptor)
	{
		this.encryptor = encryptor;
	}

	public void setEncryptionUtils(EncryptionUtils encryptionUtils)
	{
		this.encryptionUtils = encryptionUtils;
	}
	
	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	public void doFilter(ServletContext context, ServletRequest request,
			ServletResponse response, FilterChain chain) throws IOException,
			ServletException
	{
		if(enabled)
		{
			HttpServletRequest httpRequest = (HttpServletRequest)request;
			HttpServletResponse httpResponse = (HttpServletResponse)response;
	
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
    
    private static class SOLRHttpServletRequestWrapper extends HttpServletRequestWrapper
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
    }
    
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
