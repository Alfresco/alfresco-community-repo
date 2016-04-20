package org.alfresco.util;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;

/**
 * Base test class providing Hibernate sessions.
 * <p>
 * By default this is auto-wired by type. If a this is going to 
 * result in a conlict the use auto-wire by name.  This can be done by
 * setting populateProtectedVariables to true in the constructor and 
 * then adding protected members with the same name as the bean you require.
 * 
 * @author Derek Hulley
 */
public abstract class BaseSpringTest extends AbstractTransactionalDataSourceSpringContextTests
{
    /** protected so that it gets populated if autowiring is done by variable name **/
    protected SessionFactory sessionFactory;

	/**
	 * Constructor
	 */
    public BaseSpringTest()
    {
        // TODO temporarily switch default to auto-wire by name
        this.setAutowireMode(AUTOWIRE_BY_NAME);
        //this.setDependencyCheck(false);
        
    }
    
    /**
     * Setter present for in case autowiring is done by type
     * 
     * @param sessionFactory
     */
    public void setSessionFactory(SessionFactory sessionFactory)
    {
        this.sessionFactory = sessionFactory;
    }
    
    /**
     * @return Returns the existing session attached to the thread.
     *      A new session will <b>not</b> be created.
     */
    protected Session getSession()
    {
        return SessionFactoryUtils.getSession(sessionFactory, true);
    }
    
    /**
     * Forces the session to flush to the database (without commiting) and clear the
     * cache.  This ensures that all reads against the session are fresh instances,
     * which gives the assurance that the DB read/write operations occur correctly.
     */
    protected void flushAndClear()
    {
        getSession().flush();
        getSession().clear();
    }

	/**
	 * Get the config locations
	 * 
	 * @return  an array containing the config locations
	 */
    @Override
    protected String[] getConfigLocations()
    {
        
        if (logger.isDebugEnabled())
        {
            logger.debug("Getting config locations");
        }
        return ApplicationContextHelper.CONFIG_LOCATIONS;
    }

}
