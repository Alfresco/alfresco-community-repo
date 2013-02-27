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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.util.TempFileProvider;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;

/**
 * Interceptor to replace ContentStream parameter values with ReusableContentStream parameters
 * which unlike the original may be closed and then opened again. This is important as retrying
 * transaction advice is also added. A reopen of the original results is zero bytes being read.
 * 
 * @author Alan Davis
 * @since 4.0.2.26 / 4.1.4
 */
public class AlfrescoCmisStreamInterceptor implements MethodInterceptor
{
    public Object invoke(MethodInvocation mi) throws Throwable
    {
        List<ReusableContentStream> reusableContentStreams = null;
        try
        {
            Class<?>[] parameterTypes = mi.getMethod().getParameterTypes();
            Object[] arguments = mi.getArguments();
            for (int i=0; i<parameterTypes.length; i++)
            {
                if (ContentStream.class.isAssignableFrom(parameterTypes[i]))
                {
                    ContentStream contentStream = (ContentStream) arguments[i];
                    if (contentStream != null)
                    {
                        if (reusableContentStreams == null)
                        {
                            reusableContentStreams = new ArrayList<ReusableContentStream>();
                        }
                        ReusableContentStream reuableContentStream = new ReusableContentStream(contentStream);
                        reusableContentStreams.add(reuableContentStream);
                        
                        // It is possible to just change the arguments. No need to call a setter.
                        // Wow, did not expect that.
                        arguments[i] = reuableContentStream;
                    }
                }
            }
            return mi.proceed();
        }
        finally
        {
            if (reusableContentStreams != null)
            {
                for (ReusableContentStream contentStream: reusableContentStreams)
                {
                    contentStream.close();
                }
            }
        }
    }
    
    private static class ReusableContentStream extends ContentStreamImpl
    {
        private static final long serialVersionUID = 8992465629472248502L;

        private File file;
        
        public ReusableContentStream(ContentStream contentStream) throws Exception
        {
            setLength(contentStream.getBigLength());
            setMimeType(contentStream.getMimeType());
            setFileName(contentStream.getFileName());
            file = TempFileProvider.createTempFile(contentStream.getStream(), "cmis", "contentStream");
        }
        
        @Override
        public InputStream getStream() {
            InputStream stream = super.getStream();
            if (stream == null && file != null)
            {
                try
                {
                    stream = new FileInputStream(file)
                    {
                        @Override
                        public void close() throws IOException
                        {
                            setStream(null);
                            super.close();
                        }
                    };
                }
                catch (Exception e)
                {
                    throw new AlfrescoRuntimeException("Expected to be able to reopen temporary file", e);
                }
                setStream(stream);
            }
            return stream;
        }

        public void close()
        {
            try
            {
                file.delete();
            }
            finally
            {
                file = null;
            }
        }
    }
}
