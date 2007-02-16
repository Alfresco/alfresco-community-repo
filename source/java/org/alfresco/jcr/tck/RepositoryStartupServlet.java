/*
 * Copyright (C) 2005 Alfresco, Inc.
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
package org.alfresco.jcr.tck;

import java.util.Hashtable;

import javax.jcr.Repository;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.alfresco.jcr.repository.RepositoryFactory;
import org.alfresco.jcr.repository.RepositoryImpl;
import org.alfresco.jcr.test.TestData;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;


/**
 * Setup Repository for access via JNDI by TCK Web Application
 * 
 * @author David Caruana
 */
public class RepositoryStartupServlet extends HttpServlet
{
    private static final long serialVersionUID = -4763518135895358778L;

    private static InitialContext jndiContext;
    
    private final static String repositoryName = "Alfresco.Repository";

    
    /**
     * Initializes the servlet
     * 
     * @throws ServletException
     */
    public void init()
        throws ServletException
    {
        super.init();
        
        WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
        RepositoryImpl repository = (RepositoryImpl)context.getBean(RepositoryFactory.REPOSITORY_BEAN);
        repository.setDefaultWorkspace(TestData.TEST_WORKSPACE);
        
        try
        {
            Hashtable<String, String> env = new Hashtable<String, String>();
            env.put(Context.PROVIDER_URL, "http://www.alfresco.org");
            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.day.crx.jndi.provider.MemoryInitialContextFactory");
            jndiContext = new InitialContext(env);
            jndiContext.bind(repositoryName, (Repository)repository);
        }
        catch (NamingException e)
        {
            throw new ServletException(e);
        }
    }

    /**
     * Destroy the servlet
     */
    public void destroy()
    {
        super.destroy();

        if (jndiContext != null)
        {
            try
            {
                jndiContext.unbind(repositoryName);
            }
            catch (NamingException e)
            {
                // Note: Itentionally ignore...
            }
        }
    }


}
