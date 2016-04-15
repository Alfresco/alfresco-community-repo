package org.alfresco.repo.policy;

import org.alfresco.api.AlfrescoPublicApi;

/**
 * Base Policy Exception.
 * 
 * @author David Caruana
 */
@AlfrescoPublicApi
public class PolicyException extends RuntimeException
{
    private static final long serialVersionUID = 3761122726173290550L;

    
    public PolicyException(String msg)
    {
       super(msg);
    }
    
    public PolicyException(String msg, Throwable cause)
    {
       super(msg, cause);
    }
}
