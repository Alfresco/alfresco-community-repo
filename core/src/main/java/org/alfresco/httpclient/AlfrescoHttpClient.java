package org.alfresco.httpclient;

import java.io.IOException;

/**
 * 
 * @since 4.0
 *
 */
public interface AlfrescoHttpClient
{
    /**
     * Send Request to the repository
     */
    public Response sendRequest(Request req) throws AuthenticationException, IOException;
    
    
    /**
     * Set the base url to alfresco
     * - normally /alfresco
     * @param baseUrl
     */
    public void setBaseUrl(String baseUrl);


    /**
     * 
     */
    public void close();
}
