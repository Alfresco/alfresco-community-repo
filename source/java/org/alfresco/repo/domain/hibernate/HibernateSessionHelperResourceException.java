package org.alfresco.repo.domain.hibernate;

import org.alfresco.error.AlfrescoRuntimeException;

public class HibernateSessionHelperResourceException extends AlfrescoRuntimeException
{

    /**
     * 
     */
    private static final long serialVersionUID = 2935681199033295625L;

    public HibernateSessionHelperResourceException(String msgId, Object[] msgParams, Throwable cause)
    {
        super(msgId, msgParams, cause);
        // TODO Auto-generated constructor stub
    }

    public HibernateSessionHelperResourceException(String msgId, Object[] msgParams)
    {
        super(msgId, msgParams);
        // TODO Auto-generated constructor stub
    }

    public HibernateSessionHelperResourceException(String msgId, Throwable cause)
    {
        super(msgId, cause);
        // TODO Auto-generated constructor stub
    }

    public HibernateSessionHelperResourceException(String msgId)
    {
        super(msgId);
        // TODO Auto-generated constructor stub
    }

}
