/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
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
package org.alfresco.repo.tenant;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.CategoryService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ApplicationContextHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;

public class MultiTDemoSystemTest extends TestCase
{
    private static Log logger = LogFactory.getLog(MultiTDemoSystemTest.class);

    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();

    private NodeService nodeService;
    private AuthenticationService authenticationService;
    private PersonService personService;
    private SearchService searchService;
    private ContentService contentService;
    private PermissionService permissionService;
    private OwnableService ownableService;
    private TenantAdminService tenantAdminService;
    private TenantService tenantService;
    private AuthorityService authorityService;
    private CategoryService categoryService;

    public static final String TEST_TENANT_DOMAIN1 = "yyy.com";
    public static final String TEST_TENANT_DOMAIN2 = "zzz.com";
    
    private static List<String> tenants;
    
    static {
        tenants = new ArrayList<String>(2);
        tenants.add(TEST_TENANT_DOMAIN1);
        tenants.add(TEST_TENANT_DOMAIN2);
    }
    
    public static final String ROOT_DIR = "./tenantstores";

    public static final String TEST_ADMIN_BASENAME = "admin";
    public static final String TEST_ADMIN_PASSWORD = "admin";
    
    public static final String TEST_USER1 = "alice";
    public static final String TEST_USER2 = "bob";
    public static final String TEST_USER3 = "eve";
    
    
    public static StoreRef SPACES_STORE = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");

    
    public MultiTDemoSystemTest()
    {
    }


    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        nodeService = (NodeService) ctx.getBean("NodeService");
        authenticationService = (AuthenticationService) ctx.getBean("AuthenticationService");
        tenantAdminService = (TenantAdminService) ctx.getBean("tenantAdminService");
        tenantService = (TenantService) ctx.getBean("tenantService");
        personService = (PersonService) ctx.getBean("PersonService");
        searchService = (SearchService) ctx.getBean("SearchService");
        contentService = (ContentService) ctx.getBean("ContentService");
        permissionService = (PermissionService) ctx.getBean("PermissionService");
        ownableService = (OwnableService) ctx.getBean("OwnableService");
        authorityService = (AuthorityService) ctx.getBean("AuthorityService");
        categoryService = (CategoryService) ctx.getBean("CategoryService");
        
        AuthenticationUtil.setCurrentUser(AuthenticationUtil.getSystemUserName()); // force, to clear real user from previous test (runAs issue ?)
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    
    public void testCreateTenants() throws Throwable
    {   
        logger.info("Create tenants");
        
        try 
        {   
            for (final String tenantDomain : tenants)
            {        
                // create tenants (if not already created)
                AuthenticationUtil.runAs(new RunAsWork<Object>()
                {
                    public Object doWork() throws Exception
                    {
                        if (! tenantAdminService.existsTenant(tenantDomain))
                        {
                            //tenantAdminService.createTenant(tenantDomain, TEST_ADMIN_PASSWORD.toCharArray(), ROOT_DIR + "/" + tenantDomain);
                            tenantAdminService.createTenant(tenantDomain, TEST_ADMIN_PASSWORD.toCharArray(), null); // use default root dir
                            
                            logger.info("Created tenant " + tenantDomain);
                        }
                        
                        return null;
                    }
                }, AuthenticationUtil.getSystemUserName());
            }
        }   
        catch (Throwable t)
        {
            StringWriter stackTrace = new StringWriter();
            t.printStackTrace(new PrintWriter(stackTrace));
            System.err.println(stackTrace.toString());
            throw t;
        }        
    }
    
    public void testCreateUsers() throws Throwable
    {
        logger.info("Create demo users");
        
        try
        {
            for (final String tenantDomain : tenants)
            {        
                String tenantAdminName = tenantService.getDomainUser(TenantService.ADMIN_BASENAME, tenantDomain);
                
                AuthenticationUtil.runAs(new RunAsWork<Object>()
                        {
                            public Object doWork() throws Exception
                            {
                                createUser(TEST_USER1, tenantDomain, "welcome");
                                
                                createUser(TEST_USER2, tenantDomain, "welcome");
                                
                                if (tenantDomain.equals(TEST_TENANT_DOMAIN2))
                                {
                                    createUser(TEST_USER3, tenantDomain, "welcome");
                                }
                                
                                return null;                      
                            }
                        }, tenantAdminName);  
                
            }
        }   
        catch (Throwable t)
        {
            StringWriter stackTrace = new StringWriter();
            t.printStackTrace(new PrintWriter(stackTrace));
            System.err.println(stackTrace.toString());
            throw t;
        }   
    }
    
    public void testCreateGroups()
    {
        logger.info("Create demo groups");

        for (final String tenantDomain : tenants)
        {        
            String tenantAdminName = tenantService.getDomainUser("admin", tenantDomain);
            
            AuthenticationUtil.runAs(new RunAsWork<Object>()
                    {
                        public Object doWork() throws Exception
                        {
                            createGroup("GrpA-"+tenantDomain, null);
                            createGroup("SubGrpA-"+tenantDomain, "GrpA-"+tenantDomain);
                            
                            createGroup("GrpB-"+tenantDomain, null);
                            createGroup("SubGrpB-"+tenantDomain, "GrpB-"+tenantDomain);
                                                        
                            if (tenantDomain.equals(TEST_TENANT_DOMAIN2))
                            {
                                createGroup("GrpC-"+tenantDomain, null);
                                createGroup("SubGrpC-"+tenantDomain, "GrpC-"+tenantDomain);
                            }
                            
                            return null;                      
                        }
                    }, tenantAdminName);  
            
        }
    }
    
    public void testCreateCategories()
    {
        logger.info("Create demo categories");
        
        for (final String tenantDomain : tenants)
        {        
            String tenantAdminName = tenantService.getDomainUser(TenantService.ADMIN_BASENAME, tenantDomain);
            
            AuthenticationUtil.runAs(new RunAsWork<Object>()
                    {
                        public Object doWork() throws Exception
                        {
                            NodeRef catRef = createCategory(SPACES_STORE, null, "CatA", "CatA-"+tenantDomain);
                            createCategory(SPACES_STORE, catRef, "SubCatA", "SubCatA-"+tenantDomain); // ignore return
                            
                            catRef = createCategory(SPACES_STORE, null, "CatB", "CatB-"+tenantDomain);
                            createCategory(SPACES_STORE, catRef, "SubCatB", "SubCatB-"+tenantDomain); // ignore return

                            if (tenantDomain.equals(TEST_TENANT_DOMAIN2))
                            {
                                catRef = createCategory(SPACES_STORE, null, "CatC", "CatC-"+tenantDomain);
                                createCategory(SPACES_STORE, catRef, "SubCatC", "SubCatC-"+tenantDomain); // ignore return
                            }
                            
                            return null;                      
                        }
                    }, tenantAdminName);  
            
        }
    }
    
    public void testCreateFolders()
    {
        logger.info("Create demo folders");
        
        List<String> users = new ArrayList<String>(3);
        users.add(TEST_USER1);
        users.add(TEST_USER2);
        users.add(TEST_USER3);
        
        for (final String tenantDomain : tenants)
        {    
            for (String baseUserName : users)
            {
                if ((! baseUserName.equals(TEST_USER3)) || (tenantDomain.equals(TEST_TENANT_DOMAIN2)))
                {
                    final String tenantUserName = tenantService.getDomainUser(baseUserName, tenantDomain);
                    
                    AuthenticationUtil.runAs(new RunAsWork<Object>()
                    {
                        public Object doWork() throws Exception
                        {
                            NodeRef homeSpaceRef = getHomeSpaceFolderNode(tenantUserName);
                            
                            NodeRef folderRef = createFolderNode(homeSpaceRef, "myfolder1");                                
                            createFolderNode(folderRef, "mysubfolder1"); // ignore return
                            
                            folderRef = createFolderNode(homeSpaceRef, "myfolder2"); 
                            createFolderNode(folderRef, "mysubfolder2"); // ignore return
                            
                            if (tenantDomain.equals(TEST_TENANT_DOMAIN2))
                            {
                                folderRef = createFolderNode(homeSpaceRef, "myfolder3"); 
                                createFolderNode(folderRef, "mysubfolder3"); // ignore return
                            }
                            
                            return null;                      
                        }
                    }, tenantUserName);  
                }
            }
        }
    }
    
    public void testCreateUserContent()
    {
        logger.info("Create demo content");
        
        List<String> users = new ArrayList<String>(3);
        users.add(TEST_USER1);
        users.add(TEST_USER2);
        users.add(TEST_USER3);
        
        for (final String tenantDomain : tenants)
        {    
            for (String baseUserName : users)
            {
                if ((! baseUserName.equals(TEST_USER3)) || (tenantDomain.equals(TEST_TENANT_DOMAIN2)))
                {
                    final String tenantUserName = tenantService.getDomainUser(baseUserName, tenantDomain);
                    
                    AuthenticationUtil.runAs(new RunAsWork<Object>()
                    {
                        public Object doWork() throws Exception
                        {
                            NodeRef homeSpaceRef = getHomeSpaceFolderNode(tenantUserName);
                            addTextContent(homeSpaceRef, tenantUserName+" quick brown fox.txt", "The quick brown fox jumps over the lazy dog (tenant " + tenantDomain + ")");
                            
                            if (tenantDomain.equals(TEST_TENANT_DOMAIN2))
                            {
                                addTextContent(homeSpaceRef, tenantUserName+" quick brown fox ANO.txt", "The quick brown fox jumps over the lazy dog ANO (tenant " + tenantDomain + ")");                                   
                            }
                            
                            return null;                      
                        }
                    }, tenantUserName);  
                }
            }
        }
    }

    
    private void createGroup(String shortName, String parentShortName)
    {
        // create new Group using authority Service
        String groupName = this.authorityService.getName(AuthorityType.GROUP, shortName);
        if (this.authorityService.authorityExists(groupName) == false)
        {
           String parentGroupName = null;
           if (parentShortName != null)
           {
               parentGroupName = this.authorityService.getName(AuthorityType.GROUP, parentShortName);
               if (this.authorityService.authorityExists(parentGroupName) == false)
               {
                   logger.warn("Parent group does not exist: " + parentShortName);
                   return;
               }
           }           
           
           this.authorityService.createAuthority(AuthorityType.GROUP, parentGroupName, shortName);

        }
        else
        {
            logger.warn("Group already exists: " + shortName);
        }
    }

    
    private void createUser(String baseUserName, String tenantDomain, String password)
    {
        String userName = tenantService.getDomainUser(baseUserName, tenantDomain);
        
        if (! this.authenticationService.authenticationExists(userName))
        {
            NodeRef baseHomeFolder = getUserHomesNodeRef(SPACES_STORE);

            // Create the users home folder
            NodeRef homeFolder = createHomeSpaceFolderNode(
                                                baseHomeFolder,
                                                baseUserName,
                                                userName);
            
            // Create the authentication
            this.authenticationService.createAuthentication(userName, password.toCharArray());
            
            // Create the person
            Map<QName, Serializable> personProperties = new HashMap<QName, Serializable>();
            personProperties.put(ContentModel.PROP_USERNAME, userName);
            personProperties.put(ContentModel.PROP_HOMEFOLDER, homeFolder);
            personProperties.put(ContentModel.PROP_FIRSTNAME, baseUserName);
            personProperties.put(ContentModel.PROP_LASTNAME, baseUserName+"-"+tenantDomain); // add domain suffix here for demo only
            personProperties.put(ContentModel.PROP_EMAIL, userName);
            
            NodeRef newPerson = this.personService.createPerson(personProperties);
            
            // ensure the user can access their own Person object
            this.permissionService.setPermission(newPerson, userName, permissionService.getAllPermission(), true);
            
            logger.info("Created user " + userName);
        }
    }
    
    private NodeRef getUserHomesNodeRef(StoreRef storeRef)
    {
        // get the users' home location
        String path = "/app:company_home/app:user_homes";
        
        ResultSet rs = this.searchService.query(storeRef, SearchService.LANGUAGE_XPATH, path);
        
        NodeRef usersHomeNodeRef = null;
        if (rs.length() == 0)
        {
            throw new AlfrescoRuntimeException("Cannot find user homes location: " + path);
        }
        else
        {
            usersHomeNodeRef = rs.getNodeRef(0);
        }
        return usersHomeNodeRef;
    }
    
    private NodeRef createFolderNode(NodeRef parentFolderNodeRef, String nameValue)
    {
        if (nameValue != null)
        {       
            Map<QName, Serializable> folderProps = new HashMap<QName, Serializable>();
            folderProps.put(ContentModel.PROP_NAME, nameValue);
            
            return this.nodeService.createNode(
                    parentFolderNodeRef, 
                    ContentModel.ASSOC_CONTAINS, 
                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, nameValue),
                    ContentModel.TYPE_FOLDER,
                    folderProps).getChildRef();
        }
        
        return null;
    }
    
    private NodeRef createCategory(StoreRef storeRef, NodeRef parentCategoryRef, String name, String description)
    {
        // create category using categoryservice
        NodeRef ref;
        if (parentCategoryRef == null)
        {
           ref = this.categoryService.createRootCategory(storeRef, ContentModel.ASPECT_GEN_CLASSIFIABLE, name);
        }
        else
        {
           ref = categoryService.createCategory(parentCategoryRef, name);
        }
        
        // apply the titled aspect - for description
        Map<QName, Serializable> titledProps = new HashMap<QName, Serializable>(1, 1.0f);
        titledProps.put(ContentModel.PROP_DESCRIPTION, description);
        this.nodeService.addAspect(ref, ContentModel.ASPECT_TITLED, titledProps);
        
        return ref;
    }
    
    private NodeRef createHomeSpaceFolderNode(NodeRef folderNodeRef, String spaceName, String userName)
    {
        if (spaceName != null)
        {       
            Map<QName, Serializable> folderProps = new HashMap<QName, Serializable>();
            folderProps.put(ContentModel.PROP_NAME, spaceName);
            
            NodeRef nodeRef = this.nodeService.createNode(
                                                folderNodeRef, 
                                                ContentModel.ASSOC_CONTAINS, 
                                                QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, spaceName),
                                                ContentModel.TYPE_FOLDER,
                                                folderProps).getChildRef();
            
            // apply the uifacets aspect - icon and title props
            Map<QName, Serializable> uiFacetsProps = new HashMap<QName, Serializable>(3);
            uiFacetsProps.put(ApplicationModel.PROP_ICON, "space-icon-default");
            uiFacetsProps.put(ContentModel.PROP_TITLE, spaceName);
            this.nodeService.addAspect(nodeRef, ApplicationModel.ASPECT_UIFACETS, uiFacetsProps);
            
            setupHomeSpacePermissions(nodeRef, userName);

            return nodeRef;
        }
        
        return null;
    }
    
    private void setupHomeSpacePermissions(NodeRef homeSpaceRef, String userName)
    {
       // Admin Authority has full permissions by default (automatic - set in the permission config)
       // give full permissions to the new user
       this.permissionService.setPermission(homeSpaceRef, userName, permissionService.getAllPermission(), true);

       // by default other users will only have GUEST access to the space contents
       String permission = "Consumer";
       
       if (permission != null && permission.length() != 0)
       {
          this.permissionService.setPermission(homeSpaceRef, permissionService.getAllAuthorities(), permission, true);
       }

       // the new user is the OWNER of their own space and always has full permissions
       this.ownableService.setOwner(homeSpaceRef, userName);
       this.permissionService.setPermission(homeSpaceRef, permissionService.getOwnerAuthority(), permissionService.getAllPermission(), true);

       // now detach (if we did this first we could not set any permissions!)
       this.permissionService.setInheritParentPermissions(homeSpaceRef, false);
    }
    
    private NodeRef getHomeSpaceFolderNode(String userName)
    {
        return (NodeRef)this.nodeService.getProperty(personService.getPerson(userName), ContentModel.PROP_HOMEFOLDER);
    }
    
    private void addTextContent(NodeRef spaceRef, String name, String textData)
    {
        Map<QName, Serializable> contentProps = new HashMap<QName, Serializable>();
        contentProps.put(ContentModel.PROP_NAME, name);

        ChildAssociationRef association = nodeService.createNode(spaceRef,
                ContentModel.ASSOC_CONTAINS, 
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name), 
                ContentModel.TYPE_CONTENT,
                contentProps);

        NodeRef content = association.getChildRef();

        // add titled aspect (for Web Client display)
        Map<QName, Serializable> titledProps = new HashMap<QName, Serializable>();
        titledProps.put(ContentModel.PROP_TITLE, name);
        titledProps.put(ContentModel.PROP_DESCRIPTION, name);
        this.nodeService.addAspect(content, ContentModel.ASPECT_TITLED, titledProps);

        ContentWriter writer = contentService.getWriter(content, ContentModel.PROP_CONTENT, true);

        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.setEncoding("UTF-8");

        writer.putContent(textData);
    }
    

    // comment-in to run from command line
    public static void main(String args[]) 
    {
        System.out.println(new Date());
        junit.textui.TestRunner.run(MultiTDemoSystemTest.class);
        System.out.println(new Date());
    }
     
}
