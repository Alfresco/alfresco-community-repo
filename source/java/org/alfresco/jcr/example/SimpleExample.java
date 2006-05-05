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
package org.alfresco.jcr.example;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.Repository;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;



/**
 * Simple Example that demonstrate login and retrieval of top-level Spaces
 * under Company Home.
 * 
 * @author David Caruana
 */
public class SimpleExample
{

    public static void main(String[] args)
        throws Exception
    {
        // Setup Spring and Transaction Service
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath:alfresco/application-context.xml");
        
        // Retrieve Repository
        Repository repository = (Repository)context.getBean("JCR.Repository");

        // Login to workspace
        // Note: Default workspace is the one used by Alfresco Web Client which contains all the Spaces
        //       and their documents
        Session session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));

        try
        {
            // Retrieve Company Home
            Node root = session.getRootNode();
            Node companyHome = root.getNode("app:company_home");
            
            // Iterator through children of Company Home
            NodeIterator iterator = companyHome.getNodes();
            while(iterator.hasNext())
            {
                Node child = iterator.nextNode();
                System.out.println(child.getName());

                PropertyIterator propIterator = child.getProperties();
                while(propIterator.hasNext())
                {
                    Property prop = propIterator.nextProperty();
                    if (!prop.getDefinition().isMultiple())
                    {
                        System.out.println(" " + prop.getName() + " = " + prop.getString());
                    }
                }
            }
        }
        finally
        {
            session.logout();
            System.exit(0);
        }
        
    }
    
}
