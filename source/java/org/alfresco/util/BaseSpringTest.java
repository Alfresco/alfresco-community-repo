/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.util;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
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
    private boolean defaultContext;
    
	/**
	 * Constructor
	 */
    public BaseSpringTest()
    {
        defaultContext = false;
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
        // The derived class is using the default context
        defaultContext = true;
        if (logger.isDebugEnabled())
        {
            logger.debug("Getting config locations");
        }
        return ApplicationContextHelper.CONFIG_LOCATIONS;
    }

    /**
     * Ignores the key and loads the {@link ApplicationContextHelper application context}.
     */
    @Override
    protected ConfigurableApplicationContext loadContext(Object key) throws Exception
    {
        if (defaultContext)
        {
            return (ConfigurableApplicationContext) ApplicationContextHelper.getApplicationContext();
        }
        else
        {
            String[] configLocations = (String[]) key;
            return new ClassPathXmlApplicationContext(configLocations);
        }
    }
}
