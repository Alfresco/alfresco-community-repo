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

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.Value;
import javax.jcr.Workspace;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;

import org.alfresco.jcr.api.JCRNodeRef;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PermissionService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;


/**
 * Example that demonstrates read and write of a simple WIKI model
 *
 * Please refer to http://www.alfresco.org/mediawiki/index.php/Introducing_the_Alfresco_Java_Content_Repository_API
 * for a complete description of this example.
 * 
 * @author David Caruana
 */
public class WIKIExample
{

    public static void main(String[] args)
        throws Exception
    {
        //
        // Repository Initialisation
        //
        
        // access the Alfresco JCR Repository (here it's via programmatic approach, but it could also be injected)
        System.out.println("Initialising Repository...");
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath:org/alfresco/jcr/example/wiki-context.xml");
        Repository repository = (Repository)context.getBean("JCR.Repository");

        // display information about the repository
        System.out.println("Repository Description...");
        String[] keys = repository.getDescriptorKeys();
        for (String key : keys)
        {
            String value = repository.getDescriptor(key);
            System.out.println(" " + key + " = " + value);
        }
        
        //
        // Create a WIKI structure
        //
        // Note: Here we're using the Alfresco Content Model and custom WIKI model to create
        //       WIKI pages and Content that are accessible via the Alfresco Web Client
        //
        
        // login to workspace (here we rely on the default workspace defined by JCR.Repository bean)
        Session session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));

        try
        {
            System.out.println("Creating WIKI...");

            // first, access the company home
            Node rootNode = session.getRootNode();
            System.out.println("Root node: path=" + rootNode.getPath() + ", type=" + rootNode.getPrimaryNodeType().getName());
            Node companyHome = rootNode.getNode("app:company_home");
            System.out.println("Company home node: path=" + companyHome.getPath() + ", type=" + companyHome.getPrimaryNodeType().getName());
            
            // remove the WIKI structure if it already exists
            try
            {
                Node encyclopedia = companyHome.getNode("wiki:encyclopedia");
                encyclopedia.remove();
                System.out.println("Existing WIKI found and removed");
            }
            catch(PathNotFoundException e)
            {
               // doesn't exist, no need to remove                
            }

            // create the root WIKI folder
            Node encyclopedia = companyHome.addNode("wiki:encyclopedia", "cm:folder");
            encyclopedia.setProperty("cm:name", "WIKI Encyclopedia");
            encyclopedia.setProperty("cm:description", "");

            // create first wiki page
            Node page1 = encyclopedia.addNode("wiki:entry1", "wiki:page");
            page1.setProperty("cm:name", "Rose");
            page1.setProperty("cm:description", "");
            page1.setProperty("cm:title", "The rose");
            page1.setProperty("cm:content", "A rose is a flowering shrub.");
            page1.setProperty("wiki:category", new String[] {"flower", "plant", "rose"});

            // create second wiki page
            Node page2 = encyclopedia.addNode("wiki:entry2", "wiki:page");
            page2.setProperty("cm:name", "Shakespeare");
            page2.setProperty("cm:description", "");
            page2.setProperty("cm:title", "William Shakespeare");
            page2.setProperty("cm:content", "A famous poet who likes roses.");
            page2.setProperty("wiki:restrict", true);
            page2.setProperty("wiki:category", new String[] {"poet"});
            
            // create an image (note: we're using an input stream to allow setting of binary content)
            Node contentNode = encyclopedia.addNode("wiki:image", "cm:content");
            contentNode.setProperty("cm:name", "Dog");
            contentNode.setProperty("cm:description", "");
            contentNode.setProperty("cm:title", "My dog at New Year party");
            ClassPathResource resource = new ClassPathResource("org/alfresco/jcr/example/wikiImage.gif");
            contentNode.setProperty("cm:content", resource.getInputStream());
            
            session.save();
            System.out.println("WIKI created");
        }
        finally
        {
            session.logout();
        }
        
        //
        // Access the WIKI structure
        //
        
        // login to workspace (here we rely on the default workspace defined by JCR.Repository bean)
        session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));
    
        try
        {
            System.out.println("Accessing WIKI...");
            
            // access a wiki node directly from root node (by path and by UUID)
            Node rootNode = session.getRootNode();
            Node encyclopedia = rootNode.getNode("app:company_home/wiki:encyclopedia");
            Node direct = session.getNodeByUUID(encyclopedia.getUUID());
            System.out.println("Found WIKI root correctly: " + encyclopedia.equals(direct));

            // access a wiki property directly from root node
            Node entry1 = rootNode.getNode("app:company_home/wiki:encyclopedia/wiki:entry1");
            String title = entry1.getProperty("cm:title").getString();
            System.out.println("Found WIKI page 1 title: " + title);
            Calendar modified = entry1.getProperty("cm:modified").getDate();
            System.out.println("Found WIKI page 1 last modified date: " + modified.getTime());

            // browse all wiki entries
            System.out.println("WIKI browser:");
            NodeIterator entries = encyclopedia.getNodes();
            while (entries.hasNext())
            {
                Node entry = entries.nextNode();
                outputContentNode(entry);
            }            

            // perform a search
            System.out.println("Search results:");
            Workspace workspace = session.getWorkspace();
            QueryManager queryManager = workspace.getQueryManager();
            Query query = queryManager.createQuery("//app:company_home/wiki:encyclopedia/*[@cm:title = 'The rose']", Query.XPATH);
            //Query query = queryManager.createQuery("//app:company_home/wiki:encyclopedia/*[jcr:contains(., 'rose')]", Query.XPATH);
            QueryResult result = query.execute();
            NodeIterator it = result.getNodes();
            while (it.hasNext())
            {
                Node n = it.nextNode();
                outputContentNode(n);
            }            

            // export content (system view format)
            File systemView = new File("systemview.xml");
            FileOutputStream systemViewOut = new FileOutputStream(systemView);
            session.exportSystemView("/app:company_home/wiki:encyclopedia", systemViewOut, false, false);
            
            // export content (document view format)
            File docView = new File("docview.xml");
            FileOutputStream docViewOut = new FileOutputStream(docView);
            session.exportDocumentView("/app:company_home/wiki:encyclopedia", docViewOut, false, false);
            
            System.out.println("WIKI exported");

        }
        finally
        {
            session.logout();
        }

        
        //
        // Advanced Usage
        //

        // 1) Check-out / Check-in and version history retrieval
        session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));
    
        try
        {
            //
            // Version WIKI Page 1
            //
            
            // first, access the page
            Node rootNode = session.getRootNode();
            Node entry1 = rootNode.getNode("app:company_home/wiki:encyclopedia/wiki:entry1");

            // enable versioning capability
            entry1.addMixin("mix:versionable");

            // update the properties and content
            entry1.setProperty("cm:title", "The Rise");
            entry1.setProperty("cm:content", "A rose is a flowering shrub of the genus Rosa.");
            Value[] categories = entry1.getProperty("wiki:category").getValues();
            Value[] newCategories = new Value[categories.length + 1];
            System.arraycopy(categories, 0, newCategories, 0, categories.length);
            newCategories[categories.length] = session.getValueFactory().createValue("poet");
            entry1.setProperty("wiki:category", newCategories);

            // and checkin the changes
            entry1.checkin();

            // checkout, fix wiki title and checkin again
            entry1.checkout();
            entry1.setProperty("cm:title", "The Rose");
            entry1.checkin();
            
            session.save();
            System.out.println("Versioned WIKI Page 1");
        }
        finally
        {
            session.logout();
        }

        // 2) Permission checks
        session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));
        
        try
        {
            //
            // Browse WIKI Page 1 Version History
            //
            
            // first, access the page
            Node rootNode = session.getRootNode();
            Node entry1 = rootNode.getNode("app:company_home/wiki:encyclopedia/wiki:entry1");

            // retrieve the history for thte page
            VersionHistory versionHistory = entry1.getVersionHistory();
            VersionIterator versionIterator = versionHistory.getAllVersions();

            // for each version, output the node as it was versioned 
            while (versionIterator.hasNext())
            {
                Version version = versionIterator.nextVersion();
                NodeIterator nodeIterator = version.getNodes();

                while (nodeIterator.hasNext())
                {
                    Node versionedNode = nodeIterator.nextNode();
                    System.out.println(" Version: " + version.getName());
                    System.out.println(" Created: " + version.getCreated().getTime());
                    outputContentNode(versionedNode);
                }
            }
            
            
            //
            // Permission Checks
            //

            System.out.println("Testing Permissions:");
            
            // check for JCR 'read' permission
            session.checkPermission("app:company_home/wiki:encyclopedia/wiki:entry1", "read");
            System.out.println("Session has 'read' permission on app:company_home/wiki:encyclopedia/wiki:entry1");

            // check for Alfresco 'Take Ownership' permission
            session.checkPermission("app:company_home/wiki:encyclopedia/wiki:entry1", PermissionService.TAKE_OWNERSHIP);
            System.out.println("Session has 'take ownership' permission on app:company_home/wiki:encyclopedia/wiki:entry1");            
        }
        finally
        {
            session.logout();
        }


        //
        // Mixing JCR and Alfresco API calls
        //
        // Provide mimetype for WIKI content properties
        //

        session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));
        
        try
        {
            // Retrieve the Alfresco Repository Service Registry
            ServiceRegistry registry = (ServiceRegistry)context.getBean(ServiceRegistry.SERVICE_REGISTRY);
            
            // set the mime type on both WIKI pages and Image
            Node rootNode = session.getRootNode();
            
            // note: we have to checkout entry1 first - it's versioned
            Node entry1 = rootNode.getNode("app:company_home/wiki:encyclopedia/wiki:entry1");
            entry1.checkout();
            setMimetype(registry, entry1, "cm:content", MimetypeMap.MIMETYPE_TEXT_PLAIN);
            entry1.checkin();
            
            Node entry2 = rootNode.getNode("app:company_home/wiki:encyclopedia/wiki:entry2");
            setMimetype(registry, entry2, "cm:content", MimetypeMap.MIMETYPE_TEXT_PLAIN); 
            Node image = rootNode.getNode("app:company_home/wiki:encyclopedia/wiki:image");
            setMimetype(registry, image, "cm:content", MimetypeMap.MIMETYPE_IMAGE_GIF); 

            // save the changes
            session.save();
            System.out.println("Updated WIKI mimetypes via Alfresco calls");
        }
        finally
        {
            session.logout();
        }
            
        // exit
        System.out.println("Completed successfully.");
        System.exit(0);
    }

    
    private static void outputContentNode(Node node)
        throws RepositoryException
    {
        // output common content properties
        System.out.println(" Node " + node.getUUID());
        System.out.println("  title: " + node.getProperty("cm:title").getString());
        
        // output properties specific to WIKI page
        if (node.getPrimaryNodeType().getName().equals("wiki:page"))
        {
            System.out.println("  content: " + node.getProperty("cm:content").getString());
            System.out.println("  restrict: " + node.getProperty("wiki:restrict").getString());
            
            // output multi-value property
            Property categoryProperty = node.getProperty("wiki:category");
            Value[] categories = categoryProperty.getValues();
            for (Value category : categories)
            {
                System.out.println("  category: " + category.getString());
            }
        }
    }

    
    private static void setMimetype(ServiceRegistry registry, Node node, String propertyName, String mimeType)
        throws RepositoryException
    {
        // convert the JCR Node to an Alfresco Node Reference
        NodeRef nodeRef = JCRNodeRef.getNodeRef(node);

        // retrieve the Content Property (represented as a ContentData object in Alfresco)
        NodeService nodeService = registry.getNodeService();
        ContentData content = (ContentData)nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT);
        
        // update the Mimetype
        content = ContentData.setMimetype(content, mimeType);
        nodeService.setProperty(nodeRef, ContentModel.PROP_CONTENT, content);
    }
    
}
