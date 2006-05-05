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
import javax.jcr.Property;
import javax.jcr.Repository;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.alfresco.jcr.api.JCRNodeRef;
import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;



/**
 * Example that demonstrate use of JCR and Alfresco API calls.
 * 
 * @author David Caruana
 */
public class MixedExample
{

    public static void main(String[] args)
        throws Exception
    {
        // Setup Spring and Transaction Service
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath:alfresco/application-context.xml");
        ServiceRegistry registry = (ServiceRegistry)context.getBean(ServiceRegistry.SERVICE_REGISTRY);
        NodeService nodeService = (NodeService)registry.getNodeService();
        
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

            // Read Company Home Name
            Property name = companyHome.getProperty("cm:name");
            System.out.println("Name = " + name.getString());
            
            // Update Node via Alfresco Node Service API
            NodeRef companyHomeRef = JCRNodeRef.getNodeRef(companyHome);
            nodeService.setProperty(companyHomeRef, ContentModel.PROP_NAME, "Updated Company Home Name");
            
            // Re-read via JCR
            System.out.println("Updated name = " + name.getString());
        }
        finally
        {
            session.logout();
            System.exit(0);
        }
    }
    
}
