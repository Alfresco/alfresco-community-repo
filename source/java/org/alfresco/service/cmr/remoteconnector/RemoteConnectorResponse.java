package org.alfresco.service.cmr.remoteconnector;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.httpclient.Header;
import org.springframework.extensions.webscripts.Status;

/**
 * Helper wrapper around a Remote Response, for a request that
 *  was executed by {@link RemoteConnectorService}.
 * 
 * @author Nick Burch
 * @since 4.0.2
 */
public interface RemoteConnectorResponse
{
    /**
     * @return The request that generated this response
     */
    RemoteConnectorRequest getRequest();
    
    /**
     * @return The HTTP {@link Status} Code for the response
     */
    int getStatus();
    
    /**
     * @return The raw response content type, if available
     */
    String getRawContentType();
    /**
     * @return The mimetype of the response content, if available
     */
    String getContentType();
    /**
     * @return The charset of the response content, if available
     */
    String getCharset();
    
    /**
     * @return All of the response headers
     */
    Header[] getResponseHeaders();
    
    /**
     * @return The response data, as a stream
     */
    InputStream getResponseBodyAsStream() throws IOException;
    /**
     * @return The response data, as a byte array
     */
    byte[] getResponseBodyAsBytes() throws IOException;
    /**
     * @return The response as a string, based on the response content type charset
     */
    String getResponseBodyAsString() throws IOException;
}
