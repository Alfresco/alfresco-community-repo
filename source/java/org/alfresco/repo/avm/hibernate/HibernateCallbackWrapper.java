/**
 * 
 */
package org.alfresco.repo.avm.hibernate;

import java.sql.SQLException;

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
    private HibernateTxnCallback fCallback;
    
    /**
     * Make one up.
     * @param callback
     */
    public HibernateCallbackWrapper(HibernateTxnCallback callback)
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
        fCallback.perform(session);
        return null;
    }
}
