package org.alfresco.repo.transfer;

import org.apache.commons.httpclient.methods.PostMethod;

public interface HttpMethodFactory
{
    PostMethod createPostMethod();
}
