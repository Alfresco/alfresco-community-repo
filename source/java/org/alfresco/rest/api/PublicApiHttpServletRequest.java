
package org.alfresco.rest.api;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class PublicApiHttpServletRequest extends HttpServletRequestWrapper
{
    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String MULTIPART_FORM_DATA = "multipart/form-data";

    public PublicApiHttpServletRequest(HttpServletRequest request) throws IOException
    {
        super(getWrappedHttpServletRequest(request));
    }

    public void resetInputStream() throws IOException
    {
        ServletInputStream stream = getInputStream();
        if (stream.markSupported())
        {
            stream.reset();
        }
    }

    private static HttpServletRequest getWrappedHttpServletRequest(HttpServletRequest request) throws IOException
    {
        //TODO is it really necessary to wrap the request into a BufferedInputStream?
        // If not, then we could remove the check for multipart upload. 
        // The check is needed as we get an IOException (Resetting to invalid mark) for files more than 8193 bytes. 
        boolean resetSupported = true;
        String contentType = request.getHeader(HEADER_CONTENT_TYPE);
        if (contentType != null && contentType.startsWith(MULTIPART_FORM_DATA))
        {
           resetSupported = false;
        }
        final PublicApiServletInputStream sis = new PublicApiServletInputStream(request.getInputStream(), resetSupported);
        HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper(request)
        {
            public ServletInputStream getInputStream() throws java.io.IOException
            {
                return sis;
            }
        };
        return wrapper;
    }

    private static class PublicApiServletInputStream extends ServletInputStream
    {
        private final InputStream in;
        private final boolean resetSupported;

        PublicApiServletInputStream(InputStream in, boolean resetSupported)
        {
            this.resetSupported = resetSupported;
            if (resetSupported)
            {
                this.in = new BufferedInputStream(in);
                this.in.mark(8096);
            }
            else
            {
                this.in = in;
            }
        }

        @Override
        public int read() throws IOException
        {
            return in.read();
        }

        @Override
        public void reset() throws IOException
        {
            in.reset();
        }

        @Override
        public boolean markSupported()
        {
            return resetSupported;
        }
    }
}
