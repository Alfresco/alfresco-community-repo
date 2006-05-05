/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
