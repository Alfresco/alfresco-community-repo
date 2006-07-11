/**
 * 
 */
package org.alfresco.repo.avm.hibernate;

import java.sql.SQLException;

import org.alfresco.repo.avm.RetryingTransactionCallback;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

/**
 * This is a wrapper around HibernateTxnCallback implementation.
 * @author britt
 */
public class HibernateCallbackWrapper implements HibernateCallback
{
    /**
     * The HibernateTxnCallback to execute.
     */
    private RetryingTransactionCallback fCallback;
    
    /**
     * Make one up.
     * @param callback
     */
    public HibernateCallbackWrapper(RetryingTransactionCallback callback)
    {
        fCallback = callback;
    }
    
    /**
     * Call the HibernateTxnCallback internally.
     * @param session The Hibernate Session.
     */
    public Object doInHibernate(Session session) throws HibernateException,
            SQLException
    {
        fCallback.perform();
        return null;
    }
}
