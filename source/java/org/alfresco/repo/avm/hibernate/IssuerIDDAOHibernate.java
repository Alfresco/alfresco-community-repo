/**
 *
 */
package org.alfresco.repo.avm.hibernate;

import org.alfresco.repo.avm.IssuerID;
import org.alfresco.repo.avm.IssuerIDDAO;
import org.alfresco.repo.avm.IssuerIDImpl;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * Hibernate implementation of IssuerIDDAO
 * @author britt
 */
public class IssuerIDDAOHibernate extends HibernateDaoSupport implements IssuerIDDAO
{
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.IssuerIDDAO#get(java.lang.String)
     */
    public IssuerID get(String name)
    {
        return (IssuerID)getSession().get(IssuerIDImpl.class, name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.IssuerIDDAO#save(org.alfresco.repo.avm.IssuerID)
     */
    public void save(IssuerID issuerID)
    {
        getSession().save(issuerID);
    }
}
