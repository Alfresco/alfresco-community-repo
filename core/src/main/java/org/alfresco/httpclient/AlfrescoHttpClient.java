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

    /**
     * Allows to override (or not) the default headers that will be included in HTTP requests
     *
     * @param override
     *            if true, it will prevent the default headers to be duplicated, otherwise the request may contain
     *            multiple instances of the same header
     */
    public void setOverrideDefaultHeaders(boolean override);
}
