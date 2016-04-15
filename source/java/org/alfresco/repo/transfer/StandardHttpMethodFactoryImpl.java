package org.alfresco.repo.transfer;

import org.apache.commons.httpclient.methods.PostMethod;

public class StandardHttpMethodFactoryImpl implements HttpMethodFactory
{

    @Override
    public PostMethod createPostMethod()
    {
        return new PostMethod();
    }

}
