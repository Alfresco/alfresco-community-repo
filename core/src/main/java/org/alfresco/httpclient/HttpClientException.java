package org.alfresco.httpclient;

import org.alfresco.error.AlfrescoRuntimeException;

public class HttpClientException extends AlfrescoRuntimeException
{
    public HttpClientException(String msgId)
    {
        super(msgId);
    }
}
