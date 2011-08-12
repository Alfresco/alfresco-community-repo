/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.util;

import java.io.Serializable;
import java.util.HashMap;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Utility class containing some useful methods to help when writing tests that require authenticated users
 * 
 * @author Roy Wetherall
 */
public abstract class TestWithUserUtils 
{
    /**
     * Create a new user, including the corresponding person node.
     * 
     * @param userName                  the user name
     * @param password                  the password
     * @param rootNodeRef               the root node reference
     * @param nodeService               the node service
     * @param authenticationService     the authentication service
     */
    public static void createUser(
            String userName, 
            String password, 
            NodeRef rootNodeRef,
            NodeService nodeService,
            MutableAuthenticationService authenticationService)
    {
        createUser(userName, password, null, rootNodeRef, nodeService, authenticationService);
    }
    
    public static void createUser(
            String userName, 
            String password, 
            String email,
            NodeRef rootNodeRef,
            NodeService nodeService,
            MutableAuthenticationService authenticationService)
    {    
        // ignore if the user's authentication already exists
        if (authenticationService.authenticationExists(userName))
        {
            // ignore
            return;
        }
        QName children = ContentModel.ASSOC_CHILDREN;
        QName system = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "system");
        QName container = ContentModel.TYPE_CONTAINER;
        QName types = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "people");
        
        NodeRef systemNodeRef = nodeService.createNode(rootNodeRef, children, system, container).getChildRef();
        NodeRef typesNodeRef = nodeService.createNode(systemNodeRef, children, types, container).getChildRef();
        
        HashMap<QName, Serializable> properties = new HashMap<QName, Serializable>();
        properties.put(ContentModel.PROP_USERNAME, userName);
        if (email != null && email.length() != 0)
        {
            properties.put(ContentModel.PROP_EMAIL, email);
        }
        nodeService.createNode(typesNodeRef, children, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, userName) , container, properties);
        
        // Create the users
        authenticationService.createAuthentication(userName, password.toCharArray()); 
    }

    /**
     * Authenticate the user with the specified password
     * 
     * @param userName                  the user name
     * @param password                  the password
     * @param rootNodeRef               the root node reference
     * @param authenticationService     the authentication service
     */
    public static void authenticateUser(
            String userName, 
            String password,
            NodeRef rootNodeRef,
            AuthenticationService authenticationService)
    {
        authenticationService.authenticate(userName, password.toCharArray());
    }
    
    /**
     * Authenticate as the given user.  If the user does not exist, then authenticate as the system user
     * and create the authentication first.
     */
    public static void authenticateUser(
            String userName,
            String password,
            MutableAuthenticationService authenticationService,
            AuthenticationComponent authenticationComponent)
    {
        // go system
        try
        {
            authenticationComponent.setSystemUserAsCurrentUser();
            if (!authenticationService.authenticationExists(userName))
            {
                authenticationService.createAuthentication(userName, password.toCharArray());
            }
        }
        finally
        {
            authenticationComponent.clearCurrentSecurityContext();
        }
        authenticationService.authenticate(userName, password.toCharArray());
    }
    
    /**
     * Get the current user node reference
     * 
     * @param authenticationService     the authentication service
     * @return                          the currenlty authenticated user's node reference
     */
    public static String getCurrentUser(AuthenticationService authenticationService)
    {
        String un = authenticationService.getCurrentUserName();
        if (un != null)
        {
            return un;
        }
        else
        {
            throw new RuntimeException("The current user could not be retrieved.");
        }
        
    }

    public static void deleteUser(String user_name, String pwd, NodeRef ref, NodeService service, MutableAuthenticationService service2)
    {
        service2.deleteAuthentication(user_name);
    }

}
