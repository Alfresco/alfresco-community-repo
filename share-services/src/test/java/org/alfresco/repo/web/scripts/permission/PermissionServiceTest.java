/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.web.scripts.permission;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.alfresco.util.PropertyMap;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.TestWebScriptServer.PostRequest;

/**
 * Test for RestAPI permission services
 * 
 * @author alex.mukha
 * @since 4.2.3
 */
public class PermissionServiceTest extends BaseWebScriptTest
{
    private MutableAuthenticationService authenticationService;
    private AuthenticationComponent authenticationComponent;
    private PersonService personService;
    private NodeService nodeService;
    private PermissionService permissionService;

    private static final String USER_ONE = "USER" + GUID.generate();
    private static final String URL_DOCLIB_PERMISSIONS = "/slingshot/doclib/permissions";
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        this.authenticationService = (MutableAuthenticationService)getServer().getApplicationContext().getBean("AuthenticationService");
        this.authenticationComponent = (AuthenticationComponent)getServer().getApplicationContext().getBean("authenticationComponent");
        this.personService = (PersonService)getServer().getApplicationContext().getBean("PersonService");
        this.nodeService = (NodeService)getServer().getApplicationContext().getBean("NodeService");
        this.permissionService = (PermissionService)getServer().getApplicationContext().getBean("PermissionService");
        
        this.authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
        // Create users
        createUser(USER_ONE);
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        this.authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
        
        // Clear the users
        deleteUser(USER_ONE);     
    }
    
    /**
     * Test for MNT-11725
     */
    public void testDowngradePermissions() throws Exception
    {
        NodeRef rootNodeRef = this.nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        NodeRef folderRef = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName(NamespaceService.ALFRESCO_URI, "testFolder"),
                ContentModel.TYPE_FOLDER).getChildRef();
        permissionService.setPermission(folderRef, USER_ONE, PermissionService.COORDINATOR, true);
        permissionService.setInheritParentPermissions(folderRef, false);

        authenticationComponent.setCurrentUser(USER_ONE);
        
        // JSON fromat
        //  {"permissions":
        //  [{"authority":"userA",
        //  "role":"Consumer"},
        //  {"authority":"userA",
        //  "role":"Coordinator",
        //  "remove":true}],
        //  "isInherited":true}
        
        /*  negative test, we are first deleting the coordinator role and then try to add consumer */
        JSONObject changePermission = new JSONObject();
        JSONArray permissions = new JSONArray();
        // First delete permission, then add
        JSONObject addPermission = new JSONObject();
        addPermission.put("authority", USER_ONE);
        addPermission.put("role", PermissionService.CONSUMER);
        JSONObject removePermission = new JSONObject();
        removePermission.put("authority", USER_ONE);
        removePermission.put("role", PermissionService.COORDINATOR);
        removePermission.put("remove","true");
        permissions.put(removePermission);
        permissions.put(addPermission);
        changePermission.put("permissions", permissions);
        changePermission.put("isInherited", "true");
        
        sendRequest(new PostRequest(URL_DOCLIB_PERMISSIONS +
                "/" + StoreRef.STORE_REF_WORKSPACE_SPACESSTORE.getProtocol() +
                "/" + StoreRef.STORE_REF_WORKSPACE_SPACESSTORE.getIdentifier() +
                "/" + folderRef.getId(),  changePermission.toString(), "application/json"), Status.STATUS_INTERNAL_SERVER_ERROR);  
        
        /*  positive test  */
        changePermission = new JSONObject();
        permissions = new JSONArray();
        // First add permission, then delete
        addPermission = new JSONObject();
        addPermission.put("authority", USER_ONE);
        addPermission.put("role", PermissionService.CONSUMER);
        removePermission = new JSONObject();
        removePermission.put("authority", USER_ONE);
        removePermission.put("role", PermissionService.COORDINATOR);
        removePermission.put("remove","true");
        permissions.put(addPermission);
        permissions.put(removePermission);
        changePermission.put("permissions", permissions);
        changePermission.put("isInherited", "true");
        
        sendRequest(new PostRequest(URL_DOCLIB_PERMISSIONS +
                "/" + StoreRef.STORE_REF_WORKSPACE_SPACESSTORE.getProtocol() +
                "/" + StoreRef.STORE_REF_WORKSPACE_SPACESSTORE.getIdentifier() +
                "/" + folderRef.getId(),  changePermission.toString(), "application/json"), Status.STATUS_OK);   
        
        AccessStatus accessStatus = permissionService.hasPermission(folderRef, PermissionService.CONSUMER);
        assertTrue("The permission was not set correctly", accessStatus == AccessStatus.ALLOWED);

        this.authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
        nodeService.deleteNode(folderRef);
    }
    
    private void createUser(String userName)
    {
        if (this.authenticationService.authenticationExists(userName) == false)
        {
            this.authenticationService.createAuthentication(userName, "PWD".toCharArray());
            
            PropertyMap properties = new PropertyMap(4);
            properties.put(ContentModel.PROP_USERNAME, userName);
            properties.put(ContentModel.PROP_FIRSTNAME, "firstName");
            properties.put(ContentModel.PROP_LASTNAME, "lastName");
            properties.put(ContentModel.PROP_EMAIL, "email@email.com");
            properties.put(ContentModel.PROP_JOBTITLE, "jobTitle");
            
            this.personService.createPerson(properties);
        }
    }
    
    private void deleteUser(String username)
    {
       this.personService.deletePerson(username);
       if(this.authenticationService.authenticationExists(username))
       {
          this.authenticationService.deleteAuthentication(username);
       }
    }
}
