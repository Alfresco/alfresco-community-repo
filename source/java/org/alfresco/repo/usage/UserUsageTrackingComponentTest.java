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

package org.alfresco.repo.usage;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.usage.ContentUsageService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;

/**
 * Test enabling (recalculating) and disabling (clearing) and user usages, and also collapsing user usage deltas.
 */
public class UserUsageTrackingComponentTest extends TestCase
{
    private static ApplicationContext applicationContext = ApplicationContextHelper.getApplicationContext();

    private boolean clean = true;
    
    private MutableAuthenticationService authenticationService;
    private ContentService contentService;
    private TransactionService transactionService;
    private PersonService personService;
    private NodeService nodeService;
    private UserUsageTrackingComponent userUsageTrackingComponent;
    private ContentUsageService contentUsageService;

    private UserTransaction testTX;
    
    public static StoreRef SPACES_STORE = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");

    private static final String TEST_USER_PREFIX = "user-";
    
    private static final int MAX_USERS = 5;
    private static final int BATCH_SIZE = 5;
    private static final int PROGRESS_SIZE = 100;
    
    protected void setUp() throws Exception
    {
        nodeService = (NodeService)applicationContext.getBean("NodeService");
        authenticationService = (MutableAuthenticationService)applicationContext.getBean("authenticationService");   
        transactionService = (TransactionService)applicationContext.getBean("transactionComponent");
        personService = (PersonService)applicationContext.getBean("PersonService");
        contentService = (ContentService)applicationContext.getBean("ContentService");
        contentUsageService = (ContentUsageService)applicationContext.getBean("ContentUsageService");
        
        userUsageTrackingComponent = (UserUsageTrackingComponent)applicationContext.getBean("userUsageTrackingComponent");
        
        AuthenticationUtil.setRunAsUserSystem();
        
        createUsersAndContent();
    }

    protected void tearDown() throws Exception
    {
        if (clean)
        {
            deleteUsersAndContent();
        }
        
        super.tearDown();
    }

    private void createUsersAndContent()
    {
        long start = System.currentTimeMillis();
        long progressStart = System.currentTimeMillis();
        
        try
        {
            int count = 0;
            int batch = 0;
            //long batchStart = 0;
               
            for (int i = 1; i <= MAX_USERS; i++)
            {
                if (count == 0)
                {
                    batch++;
                    
                    testTX = transactionService.getUserTransaction();
                    testTX.begin();
                    
                    //batchStart = System.currentTimeMillis();
                }
                
                count++;
                String userName = TEST_USER_PREFIX+i;

                if (! authenticationService.authenticationExists(userName))
                {
                    // Note: this will auto-create the user home
                    HashMap<QName, Serializable> props = new HashMap<QName, Serializable>();
                    props.put(ContentModel.PROP_USERNAME, userName);
                    personService.createPerson(props);
                    
                    authenticationService.createAuthentication(userName, userName.toCharArray());
                    
                    NodeRef homeFolder = getHomeSpaceFolderNode(userName);
                    
                    StringBuilder sb = new StringBuilder();
                    for (int j = 1; j <= i; j++)
                    {
                        int k = j % 10;
                        sb.append(k);
                    }
                    
                    AuthenticationUtil.setFullyAuthenticatedUser(userName);
                    
                    addTextContent(homeFolder, "a-"+userName+".txt", sb.toString());
                    addTextContent(homeFolder, "b-"+userName+".txt", sb.toString());
                }
                
                AuthenticationUtil.setRunAsUserSystem();
                
                if ((count == BATCH_SIZE) || (i == MAX_USERS))
                {
                    testTX.commit();
                    count = 0;
                    
                    //System.out.println("Batch "+batch+" took "+((System.currentTimeMillis()-batchStart)/1000)+" secs");
                }
                
                if (((i % PROGRESS_SIZE) == 0) && (i != MAX_USERS))
                {
                    System.out.println("Progress: "+PROGRESS_SIZE+" users created in "+((System.currentTimeMillis()-progressStart)/1000)+" secs");
                    progressStart = System.currentTimeMillis();
                }
            }
            
            System.out.println("Total: "+MAX_USERS+" users created in "+((System.currentTimeMillis()-start)/1000)+" secs");
        }
        catch (Throwable t)
        {
            t.printStackTrace();
            
            try { testTX.rollback(); } catch (Exception e) { e.printStackTrace(); }
        }
    }
    
    public void testEnableDisableCollapse()
    {         
        userUsageTrackingComponent.setEnabled(false);
        userUsageTrackingComponent.bootstrapInternal(); // false => clear
        
        System.out.println("Cleared usages");
        
        checkCleared();
        
        userUsageTrackingComponent.setEnabled(true);
        userUsageTrackingComponent.bootstrapInternal(); // true => recalculate
        
        System.out.println("Recalculated usages");
        
        checkCalculated(2L);
        
        checkUsage(2L);
        
        // add content
        for (int i = 1; i <= MAX_USERS; i++)
        {
            String userName = TEST_USER_PREFIX+i;

            NodeRef homeFolder = getHomeSpaceFolderNode(userName);
            
            AuthenticationUtil.setFullyAuthenticatedUser(userName);
            
            StringBuilder sb = new StringBuilder();
            for (int j = 1; j <= i; j++)
            {
                int k = j % 10;
                sb.append(k);
            }
            
            addTextContent(homeFolder, "c-"+userName+".txt", sb.toString());
            addTextContent(homeFolder, "d-"+userName+".txt", sb.toString());
        
            AuthenticationUtil.setRunAsUserSystem();
        }
        
        System.out.println("Added content");

        checkUsage(4L);
        
        userUsageTrackingComponent.execute(); // collapse usages
        
        System.out.println("Collapsed usages");
        
        checkUsage(4L);

        // delete content
        for (int i = 1; i <= MAX_USERS; i++)
        {
            String userName = TEST_USER_PREFIX+i;

            NodeRef homeFolder = getHomeSpaceFolderNode(userName);
            
            AuthenticationUtil.setFullyAuthenticatedUser(userName);
            
            NodeRef childNodeRef = nodeService.getChildByName(homeFolder, ContentModel.ASSOC_CONTAINS, "a-"+userName+".txt");
            nodeService.deleteNode(childNodeRef);
            
            childNodeRef = nodeService.getChildByName(homeFolder, ContentModel.ASSOC_CONTAINS, "b-"+userName+".txt");
            nodeService.deleteNode(childNodeRef);
        
            AuthenticationUtil.setRunAsUserSystem();
        }
        
        System.out.println("Deleted content");
        
        checkUsage(2L);
        
        userUsageTrackingComponent.execute(); // collapse usages
        
        System.out.println("Collapsed usages");
        
        checkUsage(2L);
        
        userUsageTrackingComponent.setEnabled(false);
        userUsageTrackingComponent.bootstrapInternal(); // false => clear
        
        System.out.println("Cleared usages");
        checkCleared();
    }
    
    private void checkCalculated(long factor)
    {
        for (int i = 1; i <= MAX_USERS; i++)
        {
            String userName = TEST_USER_PREFIX+i;
            NodeRef personNodeRef = personService.getPerson(userName);
            
            // get user stored usage (not including deltas, if any)
            Long sizeProp = (Long)nodeService.getProperty(personNodeRef, ContentModel.PROP_SIZE_CURRENT);
            assertNotNull("Property " + ContentModel.PROP_SIZE_CURRENT + "does not exist, yet", sizeProp);
            assertEquals(userName, i*factor, sizeProp.longValue());
        }
    }
    
    private void checkUsage(long factor)
    {
        for (int i = 1; i <= MAX_USERS; i++)
        {
            String userName = TEST_USER_PREFIX+i;
            
             // get user usage (including deltas, if any)
            assertEquals(userName, i*factor, contentUsageService.getUserUsage(userName));
        }
    }
    
    private void checkCleared()
    {
        for (int i = 1; i <= MAX_USERS; i++)
        {
            String userName = TEST_USER_PREFIX+i;
            NodeRef personNodeRef = personService.getPerson(userName);
            
            assertNull(nodeService.getProperty(personNodeRef, ContentModel.PROP_SIZE_CURRENT));
        }
    }
    
    private void deleteUsersAndContent()
    {   
        long start = System.currentTimeMillis();
        
        try
        {
            int count = 0;
            int batch = 0;
            //long batchStart = 0;
            
            int deleteCount = 0;
               
            for (int i = 1; i <= MAX_USERS; i++)
            {
                if (count == 0)
                {
                    batch++;
                    
                    testTX = transactionService.getUserTransaction();
                    testTX.begin();
                    //batchStart = System.currentTimeMillis();
                }
                
                count++;
                String userName = TEST_USER_PREFIX+i;

                
                if (authenticationService.authenticationExists(userName))
                {
                    NodeRef homeFolder = getHomeSpaceFolderNode(userName);
                    nodeService.deleteNode(homeFolder);
                    
                    personService.deletePerson(userName);
                    deleteCount++;
                }
                
                if ((count == BATCH_SIZE) || (i == MAX_USERS))
                {
                    testTX.commit();
                    count = 0;
                    
                    //System.out.println("Batch "+batch+" took "+((System.currentTimeMillis()-batchStart)/1000)+" secs");
                }
            }
            
            System.out.println("Total: "+deleteCount+" users deleted in "+((System.currentTimeMillis()-start)/1000)+" secs");
        }
        catch (Throwable t)
        {
            t.printStackTrace();
            
            try { testTX.rollback(); } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private NodeRef getHomeSpaceFolderNode(String userName)
    {
        return (NodeRef)this.nodeService.getProperty(personService.getPerson(userName), ContentModel.PROP_HOMEFOLDER);
    }
    
    private NodeRef addTextContent(NodeRef spaceRef, String fileName, String textData)
    {
        Map<QName, Serializable> contentProps = new HashMap<QName, Serializable>();
        contentProps.put(ContentModel.PROP_NAME, fileName);

        ChildAssociationRef association = nodeService.createNode(spaceRef,
                ContentModel.ASSOC_CONTAINS, 
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, fileName), 
                ContentModel.TYPE_CONTENT,
                contentProps);

        NodeRef content = association.getChildRef();

        // add titled aspect (for Web Client display)
        Map<QName, Serializable> titledProps = new HashMap<QName, Serializable>();
        titledProps.put(ContentModel.PROP_TITLE, fileName);
        titledProps.put(ContentModel.PROP_DESCRIPTION, fileName);
        this.nodeService.addAspect(content, ContentModel.ASPECT_TITLED, titledProps);

        ContentWriter writer = contentService.getWriter(content, ContentModel.PROP_CONTENT, true);

        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.setEncoding("UTF-8");

        writer.putContent(textData);
        
        return content;
    }
}
