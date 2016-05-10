package org.alfresco.rest.framework.webscripts;

import org.alfresco.rest.framework.resource.content.ContentInfo;
import org.springframework.extensions.webscripts.Cache;

import java.util.HashMap;
import java.util.Map;

/**
 * Values to be set on the response at the appropriate time.
 *
 * It should be ok to set these variables multiple times but only the latest values are used.
 *
 * @author Gethin James
 */
public class ResponseCallBack
{
    private ContentInfo contentInfo;
    private int status;
    private Map<String, String> headers;
    private Cache cache;

    public ResponseCallBack(int status, ContentInfo contentInfo, Cache cache)
    {
        this.contentInfo = contentInfo;
        this.status = status;
        this.headers = new HashMap<>();
        this.cache = cache;
    }

    /**
     * Sets the information about the content: mimetype, encoding, locale, length
     *
     * @param contentInfo
     */
    public void setContentInfo(ContentInfo contentInfo)
    {
        this.contentInfo = contentInfo;
    }

    /**
     * Sets the Response Status
     *
     * @param status int
     */
    public void setStatus(int status)
    {
        this.status = status;
    }

    /**
     * Set a response header with the given name and value.  If the header has
     * already been set, the new value overwrites the previous one.
     *
     * @param name  header name
     * @param value  header value
     */
    public void setHeader(String name, String value)
    {
        headers.put(name, value);
    }

    /**
     * Sets the Cache control
     *
     * @param  cache  cache control
     */
    public void setCache(Cache cache)
    {
        this.cache = cache;
    }

    public ContentInfo getContentInfo()
    {
        return contentInfo;
    }

    public int getStatus()
    {
        return status;
    }

    public Cache getCache()
    {
        return cache;
    }

    public Map<String, String> getHeaders()
    {
        return headers;
    }


}
