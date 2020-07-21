/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
package org.alfresco.opencmis;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.util.TempFileProvider;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;

/**
 * Interceptor to deal with ContentStreams, determining mime type if appropriate.
 * 
 * @author steveglover
 * @author Alan Davis
 * @since 4.0.2.26 / 4.1.4
 */
public class AlfrescoCmisStreamInterceptor implements MethodInterceptor
{
    private MimetypeService mimetypeService;

    /**
     * @param mimetypeService service for helping with mimetypes
     */
    public void setMimetypeService(MimetypeService mimetypeService)
    {
        this.mimetypeService = mimetypeService;
    }

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
                        // reusable streams are required for buffering in case of tx retry
                        ReusableContentStream reusableContentStream = new ReusableContentStream(contentStream);

                        // ALF-18006
                        if (contentStream.getMimeType() == null)
                        {
                            String mimeType = mimetypeService.guessMimetype(reusableContentStream.getFileName(), new FileContentReader(reusableContentStream.file));
                            reusableContentStream.setMimeType(mimeType);
                        }

                        reusableContentStreams.add(reusableContentStream);
                        arguments[i] = reusableContentStream;
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
