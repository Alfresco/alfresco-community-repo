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
package org.alfresco.opencmis;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.util.FileFilterMode;
import org.alfresco.util.FileFilterMode.Client;
import org.alfresco.util.TempFileProvider;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.chemistry.opencmis.commons.data.CmisExtensionElement;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.exceptions.CmisStorageException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Interceptor to manage threads and perform other menial jobs that are common to all
 * calls made to the service.  It also provides detailed logging of values passing
 * in and out of the service.
 * <p/>
 * <b>DEBUG</b> shows authentication and inbound arguments.  <b>TRACE</b> shows full
 * return results as well.
 * 
 * @author Derek Hulley
 * @since 4.0
 */
public class AlfrescoCmisServiceInterceptor implements MethodInterceptor
{
    private static Log logger = LogFactory.getLog(AlfrescoCmisServiceInterceptor.class);

    public AlfrescoCmisServiceInterceptor()
    {
    }

    private PersistedContentStream getPersistedContentStream(ContentStream stream)
    {
    	PersistedContentStream newStream = (stream != null ? new PersistedContentStream(stream) : null);
    	return newStream;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable
    {
    	String methodName = invocation.getMethod().getName();
    	Object[] args = invocation.getArguments();

        // Keep note of whether debug is required
        boolean debug = logger.isDebugEnabled();
        boolean trace = logger.isTraceEnabled();
        StringBuilder sb = null;
        if (debug || trace)
        {
            sb = new StringBuilder("\n" +
                        "CMIS invocation:         \n" +
                        "   Method:                 " + methodName + "\n" +
                        "   Arguments:            \n");
            for (Object arg : args)
            {
                sb.append("      ").append(arg).append("\n");
            }
        }

        Object ret = null;
        AlfrescoCmisService service = (AlfrescoCmisService) invocation.getThis();

        List<PersistedContentStream> persistedContentStreams = new LinkedList<PersistedContentStream>();

        try
        {
            // Wrap with pre- and post-method calls
            try
            {
                if(debug || trace)
                {
                    sb.append(
                            "   Pre-call authentication: \n" +
                            "      Full auth:           " + AuthenticationUtil.getFullyAuthenticatedUser() + "\n" +
                            "      Effective auth:      " + AuthenticationUtil.getRunAsUser() + "\n");
                }

                service.beforeCall();

                if(debug || trace)
                {
                    sb.append(
                            "   In-call authentication: \n" +
                            "      Full auth:           " + AuthenticationUtil.getFullyAuthenticatedUser() + "\n" +
                            "      Effective auth:      " + AuthenticationUtil.getRunAsUser() + "\n");
                }

                // wrap CMIS content streams to make them useable with retrying transactions by persisting their contents
                for(int i = 0; i < args.length; i++)
                {
                	Object arg = args[i];
                	if(arg instanceof ContentStream)
                	{
                        PersistedContentStream persistedContentStream = getPersistedContentStream((ContentStream)arg);
                		args[i] = persistedContentStream;
                		persistedContentStreams.add(persistedContentStream);
                	}
                }
                
                FileFilterMode.setClient(Client.cmis);

                ret = invocation.proceed();
            }
            finally
            {
            	FileFilterMode.clearClient();

                service.afterCall();

                // cleanup persisted content streams
                for(PersistedContentStream stream : persistedContentStreams)
                {
                	stream.cleanup();
                }

                if(debug || trace)
                {
                    sb.append(
                            "   Post-call authentication: \n" +
                            "      Full auth:           " + AuthenticationUtil.getFullyAuthenticatedUser() + "\n" +
                            "      Effective auth:      " + AuthenticationUtil.getRunAsUser() + "\n");
                }
            }
            if (trace)
            {
                sb.append(
                        "   Returning:              ").append(ret).append("\n");
                logger.debug(sb);
            }
            // Done
            return ret;
        }
        catch (Throwable e)
        {
            if (debug)
            {
                sb.append("   Throwing:             " + e.getMessage());
                logger.debug(sb, e);
            }
            // Rethrow
            throw e;
        }
    }
    
    /**
     * Persisted content stream, for use in retrying transactions.
     * 
     * @author steveglover
     *
     */
    private static class PersistedContentStream implements ContentStream
    {
        private File tempFile = null;
    	private ContentStream stream;

    	public PersistedContentStream(ContentStream stream)
    	{
    		this.stream = stream;
    		copyToTempFile();
    	}

    	@Override
    	public List<CmisExtensionElement> getExtensions()
    	{
    		return stream.getExtensions();
    	}

    	@Override
    	public void setExtensions(List<CmisExtensionElement> extensions)
    	{
    		stream.setExtensions(extensions);
    	}

    	@Override
    	public long getLength()
    	{
    		return stream.getLength();
    	}

    	@Override
    	public BigInteger getBigLength()
    	{
    		return stream.getBigLength();
    	}

    	@Override
    	public String getMimeType()
    	{
    		return stream.getMimeType();
    	}

    	@Override
    	public String getFileName()
    	{
    		return stream.getFileName();
    	}
    	
    	@Override
    	public InputStream getStream()
    	{
    		try
    		{
    	        if(tempFile != null)
    	        {
    	        	InputStream stream = new BufferedInputStream(new FileInputStream(tempFile));
    				return stream;
    	        }
    	        else
    	        {
    	        	throw new CmisStorageException("Stream is null");
    	        }
    		}
    		catch (FileNotFoundException e)
    		{
                throw new ContentIOException("Failed to copy content from input stream: \n" +
                        "   writer: " + this,
                        e);
    		}
    	}
    	
        private void copyToTempFile()
        {
            int bufferSize = 40 * 1014;
            long count = 0;

            try
            {
                tempFile = TempFileProvider.createTempFile("cmis", "content");
                if (stream.getStream() != null)
                {
                    OutputStream out = new BufferedOutputStream(new FileOutputStream(tempFile), bufferSize);
                    //InputStream in = new BufferedInputStream(stream.getStream(), bufferSize);
                    // Temporary work around for bug in InternalTempFileInputStream which auto closes during read
                    // BufferedInputStream subsequent use of available() throws an exception.
                    InputStream in = stream.getStream();

                    byte[] buffer = new byte[bufferSize];
                    int i;
                    while ((i = in.read(buffer)) > -1)
                    {
                        out.write(buffer, 0, i);
                        count += i;
                    }

                    in.close();
                    out.close();
                }
            }
            catch (Exception e)
            {
            	cleanup();
                throw new CmisStorageException("Unable to store content: " + e.getMessage(), e);
            }

            if (stream.getLength() > -1 && stream.getLength() != count)
            {
            	cleanup();
                throw new CmisStorageException(
                        "Expected " + stream.getLength() + " bytes but retrieved " + count + "bytes!");
            }
        }
        
        public void cleanup()
        {
            if (tempFile == null)
            {
                return;
            }

            try
            {
                tempFile.delete();
            }
            catch (Exception e)
            {
                // ignore - file will be removed by TempFileProvider
            }
        }
    }
}
