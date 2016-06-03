package org.alfresco.opencmis;

import java.io.InputStream;

import org.alfresco.service.cmr.repository.MimetypeService;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;

/**
 * Interceptor to deal with ContentStreams, determining mime type if appropriate.
 * 
 * Note: this used to cache content stream to a local file so that retrying transaction behaviour
 * worked properly; this is now done in the Chemsitry OpenCMIS layer so no need to do it again
 * here. 
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
        Class<?>[] parameterTypes = mi.getMethod().getParameterTypes();
        Object[] arguments = mi.getArguments();
        for (int i = 0; i < parameterTypes.length; i++)
        {
            if (arguments[i] instanceof ContentStreamImpl)
            {
            	ContentStreamImpl contentStream = (ContentStreamImpl) arguments[i];
                if (contentStream != null)
                {
                    // ALF-18006
                    if (contentStream.getMimeType() == null)
                    {
                    	InputStream stream = contentStream.getStream();
                        String mimeType = mimetypeService.guessMimetype(contentStream.getFileName(), stream);
                        contentStream.setMimeType(mimeType);
                    }
                }
            }
        }
        return mi.proceed();
    }
}
