/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.wcm.webproject;

import java.util.List;
import java.util.Map;

import org.alfresco.service.PublicService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.Auditable;
import org.alfresco.service.NotAuditable;


/**
 * Web Project Service fundamental API.
 * <p>
 * This service API is designed to support the public facing Web Project APIs. 
 * 
 * @author janv
 */
public interface WebProjectService
{
    //
    // Web project operations
    //
	
    /**
     * Create a new web project (with a default ROOT webapp)
     * <p>
     * Note: the DNS name will be used to generate the web project store id, which can be subsequently retrieved via WebProjectInfo.getStoreId()
     * 
     * @param dnsName          DNS name (required, must be unique)
     * @param name             name (required, must be unique)
     * @param title            title
     * @param description      description
     * @return WebProjectInfo  the created web project info
     */
    @NotAuditable
    public WebProjectInfo createWebProject(String dnsName, String name, String title, String description);
    
    /**
     * Create a new web project (with a default ROOT webapp)
     * <p>
     * Note: the DNS name will be used to generate the web project store id, which can be subsequently retrieved via WebProjectInfo.getStoreId()
     * 
     * @param dnsName          DNS name (required, must be unique)
     * @param name             name (required, must be unique)
     * @param title            title
     * @param description      description
     * @param sourceNodeRef    web project node ref to branch from (can be null)
     * @return WebProjectInfo  the created web project info
     * 
     * @deprecated see createWebProject(String dnsName, WebProjectInfo wpInfo)
     */
    @NotAuditable
    public WebProjectInfo createWebProject(String dnsName, String name, String title, String description, NodeRef sourceNodeRef);
    
    /**
     * Create a new web project (with given default web app)
     * <p>
     * Note: the DNS name will be used to generate the web project store id, which can be subsequently retrieved via WebProjectInfo.getStoreId()
     * 
     * @param dnsName          DNS name (required, must be unique)
     * @param name             name (required, must be unique)
     * @param title            title
     * @param description      description
     * @param defaultWebApp    default webapp (if null, will default to ROOT webapp)
     * @param useAsTemplate    <tt>true</tt> if this web project can be used as a template to branch from
     * @param sourceNodeRef    web project node ref to branch from (can be null)
     * @return WebProjectInfo  the created web project info
     */
    @NotAuditable
    public WebProjectInfo createWebProject(String dnsName, String name, String title, String description, String defaultWebApp, boolean useAsTemplate, NodeRef sourceNodeRef);
    
    /**
     * Create a new web project (with given web project info)
     * <p>
     * Note: the DNS name will be used to generate the web project store id, which can be subsequently retrieved via WebProjectInfo.getStoreId()
     * 
     * @param wpInfo  web project info
     * 
     * Note:
     * 
     * @param dnsName          DNS name (required, must be unique)
     * @param name             name (required, must be unique)
     * @param title            title
     * @param description      description
     * @param defaultWebApp    default webapp (if null, will default to ROOT webapp)
     * @param useAsTemplate    <tt>true</tt> if this web project can be used as a template to branch from
     * @param sourceNodeRef    web project node ref to branch from (can be null)
     * @param previewProvider  preview URI service provider name (must correspond to registered name, if null will be set to default provider)
     * 
     * @return WebProjectInfo  the created web project info
     */
    @NotAuditable
    public WebProjectInfo createWebProject(WebProjectInfo wpInfo);
    
    /**
     * Determines whether the "Web Projects" container node is present.
     * 
     * @return true if the "Web Projects" container node is present
     */
    @NotAuditable
    public boolean hasWebProjectsRoot();
    
    /**
     * Returns the Web Projects container
     * 
     * @return NodeRef        the node ref of the "Web Projects" container node
     */
    @NotAuditable
    public NodeRef getWebProjectsRoot();
    
    /**
     * Returns the Web Project for the given AVM path
     * 
     * @param absoluteAVMPath the AVM path from which to determine the Web Project
     * @return NodeRef        the web project node ref for the path or null if it could not be determined
     */
    @NotAuditable
    public NodeRef getWebProjectNodeFromPath(String absoluteAVMPath);
    
    /**
     * Returns the Web Project for the given AVM store name (sandbox store id)
     * 
     * @param storeName       the AVM store name (sandbox store id) from which to determine the Web Project
     * @return NodeRef        the web project node ref for the path or null if it could not be determined
     */
    @NotAuditable
    public NodeRef getWebProjectNodeFromStore(String storeName);
    
    /**
     * List the available web projects for the current user
     * 
     * @return List<WebProjectInfo>  list of web project info
     */
    @NotAuditable
    public List<WebProjectInfo> listWebProjects();
    
    /**
     * List the web projects for the given user (based on the available web projects for the current user)
     * 
     * @param userName               user name
     * @return List<WebProjectInfo>  list of web project info
     */
    @NotAuditable
    public List<WebProjectInfo> listWebProjects(String userName);
    
    /**
     * Return true if web project node ref is a web project
     * 
     * @param wpNodeRef  web project store id
     * @return boolean   true, if web project
     */
    @NotAuditable
    public boolean isWebProject(String wpStoreId);
    
    /**
     * Return true if web project node ref is a web project
     * 
     * @param wpNodeRef  web project node ref
     * @return boolean   true, if web project
     */
    @NotAuditable
    public boolean isWebProject(NodeRef wpNodeRef);
    
    /**
     * Gets web project info based on the store id of a web project
     * <p>
     * Returns null if the web project can not be found
     * 
     * @param wpStoreId        web project store id
     * @return WebProjectInfo  web project info
     */
    @NotAuditable
    public WebProjectInfo getWebProject(String wpStoreId);
    
    /**
     * Gets web project info based on the DM nodeRef of a web project
     * <p>
     * Returns null if the web project can not be found
     * 
     * @param wpNodeRef        web project node ref
     * @return WebProjectInfo  web project info
     */
    @NotAuditable
    public WebProjectInfo getWebProject(NodeRef wpNodeRef);
    
    /**
     * Get preview provider name configured for given web project (if not configured then return default preview provider)
     * @param wpStoreId             web project store id
     * @return previewProviderName  preview URI service provide name
     */
    @NotAuditable
    public String getPreviewProvider(String wpStoreId);
    
    /**
     * Update the web project info
     * <p>
     * Note: the nodeRef and storeId (dnsName) of a web project cannot be updated once the web project has been created
     * 
     * @param wpInfo  web project info
     */
    @NotAuditable
    public void updateWebProject(WebProjectInfo wpInfo);
    
    /**
     * Delete the web project
     * <p>
     * If the web project does not exist, will log a warning and succeed
     * <p>
     * Current user must be a content manager for the web project
     * 
     * @param name  web project store id
     */
    @NotAuditable
    public void deleteWebProject(String wpStoreId);
    
    /**
     * Delete the web project
     * <p>
     * If the web project does not exist, will log a warning and succeed
     * <p>
     * Current user must be a content manager for the web project
     * <p>
     * Note: this will cascade delete all sandboxes associated with a web project
     * 
     * @param name  web project node ref
     */
    @NotAuditable
    public void deleteWebProject(NodeRef wpNodeRef);
    
    //
    // Web app operations
    //
    
    /**
     * Create webapp for the given web project. 
     * <p>
     * Current user must be a content manager for the web project
     * 
     * @param wpStoreId    web project store id
     * @param name         webapp name (must be unique within a web project)
     * @param description  webapp description
     */
    @NotAuditable
    public void createWebApp(String wpStoreId, String name, String description);
    
    /**
     * Create webapp for the given web project. 
     * <p>
     * Current user must be a content manager for the web project
     * 
     * @param wpNodeRef   web project node ref
     * @param name        webapp name (must be unique within a web project)
     * @param description webapp description
     */
    @NotAuditable
    public void createWebApp(NodeRef wpNodeRef, String name, String description);
    
    /**
     * List webapps for the web project
     * 
     * @param wpStoreId      web project store id
     * @return List<String>  list of webapp names
     */
    @NotAuditable
    public List<String> listWebApps(String wpStoreId);
    
    /**
     * List webapps for the web project
     * 
     * @param wpNodeRef      web project node ref
     * @return List<String>  list of webapp names
     */
    @NotAuditable
    public List<String> listWebApps(NodeRef wpNodeRef);
    
    /**
     * Delete webapp from the given web project
     * <p>
     * Current user must be a content manager for the web project
     * <p>
     * Note: this will cascade delete all assets within a webapp
     * 
     * @param wpStoreId   web project store id
     * @param name        webapp name
     */
    @NotAuditable
    public void deleteWebApp(String wpStoreId, String name);
    
    /**
     * Delete webapp from the given web project
     * <p>
     * Current user must be a content manager for the web project
     * <p>
     * Note: this will cascade delete all assets within a webapp
     * 
     * @param wpNodeRef   web project node ref
     * @param name        webapp name
     */
    @NotAuditable
    public void deleteWebApp(NodeRef wpNodeRef, String name);
    
    //
    // Web user operations
    //
    
    /**
     * Returns <tt>true</tt> if the current user is a manager of this web project
     * <p>
     * Note: This includes admin users but does not include the System user
     * 
     * @param wpStoreId   web project store id
     * @return boolean    <tt>true</tt> if the user is a manager (role = WCMUtil.ROLE_CONTENT_MANAGER), <tt>false</tt> otherwise
     */
    @NotAuditable
    public boolean isContentManager(String wpStoreId);
    
    /**
     * Returns <tt>true</tt> if the current user is a manager of this web project
     *
     * @param  wpNodeRef   web project node ref
     * @return boolean     <tt>true</tt> if the user is a manager (role = WCMUtil.ROLE_CONTENT_MANAGER), <tt>false</tt> otherwise
     */
    @NotAuditable
    public boolean isContentManager(NodeRef wpNodeRef);
    
    /**
     * Returns <tt>true</tt> if the user is a manager of this web project
     * <p>
     * Note: This includes admin users but does not include the System user
     * 
     * @param storeName   web project store id
     * @param username    user name
     * @return boolean    <tt>true</tt> if the user is a manager, <tt>false</tt> otherwise
     */
    @NotAuditable
    public boolean isContentManager(String wpStoreId, String username);
    
    /**
     * Returns <tt>true</tt> if the user is a manager of this web project
     * <p>
     * Note: This includes admin users but does not include the System user
     * 
     * @param wpNodeRef    web project node ref
     * @param userName     user name
     * @return boolean     <tt>true</tt> if the user is a manager (role = WCMUtil.ROLE_CONTENT_MANAGER), <tt>false</tt> otherwise
     */
    @NotAuditable
    public boolean isContentManager(NodeRef wpNodeRef, String userName);
    
    /**
     * List the web users of the web project
     * <p>
     * Current user must be a content manager for the web project
     *      
     * @param wpStoreId    web project store id
     * @return Map<String, String> map of <authority name, role name> pairs
     */
    @NotAuditable
    public Map<String, String> listWebUsers(String wpStoreId);
    
    /**
     * List the web users of the web project
     * <p>
     * Current user must be a content manager for the web project
     *      
     * @param wpNodeRef    web project node ref
     * @return Map<String, String> map of <authority name, role name> pairs
     */
    @NotAuditable
    public Map<String, String> listWebUsers(NodeRef wpNodeRef);
    
    /**
     * Get the number of web users invited to this web project
     *      
     * @param wpNodeRef    web project node ref
     * @return int         number of invited web users
     */
    @NotAuditable
    public int getWebUserCount(NodeRef wpNodeRef);
    
    /**
     * Gets the role of the specified user
     * 
     * @param wpStoreId     web project store id
     * @param userName      user name
     * @return String       web project role for this user, null if no assigned role
     */
    @NotAuditable
    public String getWebUserRole(String wpStoreId, String userName);
    
    /**
     * Gets the role of the specified user
     * 
     * @param wpNodeRef     web project node ref
     * @param userName      user name
     * @return String       web project role for this user, null if no assigned role
     */
    @NotAuditable
    public String getWebUserRole(NodeRef wpNodeRef, String userName);
    
    /**
     * Indicates whether current user is a web user of the web project or not
     * 
     * @param store id      web project store id
     * @return boolean      <tt>true</tt> if the current user is a web user of the web project, <tt>false</tt> otherwise
     */
    @NotAuditable
    public boolean isWebUser(String wpStoreId);
    
    /**
     * Indicates whether current user is a web user of the web project or not
     * 
     * @param wpNodeRef     web project node ref
     * @return boolean      <tt>true</tt> if the current user is a web user of the web project, <tt>false</tt> otherwise
     */
    @NotAuditable
    public boolean isWebUser(NodeRef wpNodeRef);
    
    /**
     * Indicates whether given user is a web user of the web project or not
     * 
     * @param store id      web project store id
     * @param userName      user name
     * @return boolean      <tt>true</tt> if the user is a web user of the web project, <tt>false</tt> otherwise
     */
    @NotAuditable
    public boolean isWebUser(String wpStoreId, String userName);
    
    /**
     * Indicates whether given user is a web user of the web project or not
     * 
     * @param wpNodeRef     web project node ref
     * @param userName      user name
     * @return boolean      <tt>true</tt> if the user is a web user of the web project, <tt>false</tt> otherwise
     */
    @NotAuditable
    public boolean isWebUser(NodeRef wpNodeRef, String userName);
    
    /**
     * Invite users/groups to web project
     * <p>
     * Note: authority name can be user or group, although a group is flattened into a set of users
     * <p>
     * Note: author sandbox will NOT be auto created for each invited user
     * 
     * @param wpStoreId                web project store id
     * @param userGroupRoles           map of <authority name, role name> pairs
     */
    @NotAuditable
    public void inviteWebUsersGroups(String wpStoreId, Map<String, String> userGroupRoles);
    
    /**
     * Invite users/groups to web project
     * <p>
     * Note: authority name can be user or group, although a group is flattened into a set of users
     * 
     * @param wpStoreId                web project store id
     * @param userGroupRoles           map of <authority name, role name> pairs
     * @param autoCreateAuthorSandbox  if <tt>true</tt> then auto create an author sandbox for each invited user
     */
    @NotAuditable
    public void inviteWebUsersGroups(String wpStoreId, Map<String, String> userGroupRoles, boolean autoCreateAuthorSandbox);
    
    /**
     * Invite users/groups to web project
     * <p>
     * Note: authority name can be user or group, although a group is flattened into a set of users
     * 
     * @param wpNodeRef                web project node ref
     * @param userGroupRoles           map of <authority name, role name> pairs
     * @param autoCreateAuthorSandbox  if <tt>true</tt> then auto create the author sandbox for each invited user
     */
    @NotAuditable
    public void inviteWebUsersGroups(NodeRef wpNodeRef, Map<String, String> userGroupRoles, boolean autoCreateAuthorSandbox);
  
    /**
     * Invite user to web project
     * <p>
     * Note: author sandbox will NOT be auto created for each invited user
     * 
     * @param wpStoreId                web project store id
     * @param userName                 user name (not a group)
     * @param userRole                 web project role
     */
    @NotAuditable
    public void inviteWebUser(String wpStoreId, String userName, String userRole);
    
    /**
     * Invite user to web project
     * 
     * @param wpStoreId                web project store id
     * @param userName                 user name (not a group)
     * @param userRole                 web project role
     * @param autoCreateAuthorSandbox  if <tt>true</tt> then auto create the author sandbox for each invited user
     */
    @NotAuditable
    public void inviteWebUser(String wpStoreId, String userName, String userRole, boolean autoCreateAuthorSandbox);
    
    /**
     * Invite user to web project
     * 
     * @param wpNodeRef                web project node ref
     * @param userName                 user name (not a group)
     * @param userRole                 web project role
     * @param autoCreateAuthorSandbox  if <tt>true</tt> then auto create the author sandbox for each invited user
     */
    @NotAuditable
    public void inviteWebUser(NodeRef wpNodeRef, String userName, String userRole, boolean autoCreateAuthorSandbox);
    
    /**
     * Uninvite user from a web project
     * <p>
     * Note: author sandbox will NOT be auto deleted
     * 
     * @param wpStoreId                web project store id
     * @param userName                 user name
     */
    @NotAuditable
    public void uninviteWebUser(String wpStoreId, String userName);
    
    /**
     * Uninvite user from a web project
     * <p>
     * Note: if author sandbox is auto deleted then this will cascade delete without warning (even if there are changed items)
     * 
     * @param wpStoreId                web project store id
     * @param userName                 user name
     * @param autoDeleteAuthorSandbox  if <tt>true</tt> then auto delete the author sandbox
     */
    @NotAuditable
    public void uninviteWebUser(String wpStoreId, String userName, boolean autoDeleteAuthorSandbox);
    
    /**
     * Uninvite user from a web project
     * <p>
     * Note: if author sandbox is auto deleted then this will cascade delete without warning (even if there are changed items)
     * 
     * @param wpNodeRef                web project node ref
     * @param userName                 user name
     * @param autoDeleteAuthorSandbox  if <tt>true</tt> then auto delete the author sandbox
     */
    @NotAuditable
    public void uninviteWebUser(NodeRef wpNodeRef, String userName, boolean autoDeleteAuthorSandbox);
}
