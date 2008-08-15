/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
package org.alfresco.repo.site.script;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.jscript.ScriptableHashMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.site.SiteInfo;
import org.alfresco.repo.site.SiteService;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 * Site JavaScript object
 * 
 * @author Roy Wetherall
 */
public class Site implements Serializable
{	
    /** Serializable serial verion UID */
    private static final long serialVersionUID = 8013569574120957923L;
 
    /** Site information */
    private SiteInfo siteInfo;
    
    /** Services Registry */
    private ServiceRegistry serviceRegistry;
    
    /** Site service */
    private SiteService siteService;

    /** Scriptable */
    private Scriptable scope;
    
    /** Indicates whether there are any outstanding changes that need to be saved */
    private boolean isDirty = false;
    
    /**
     * Constructor 
     * 
     * @param siteInfo      site information
     */
    /*package*/ Site(SiteInfo siteInfo, ServiceRegistry serviceRegistry, SiteService siteService, Scriptable scope)
    {
    	this.serviceRegistry = serviceRegistry;
        this.siteService = siteService;
        this.siteInfo = siteInfo;
        this.scope = scope;
    }
   
    /**
     * Get the site preset
     * 
     * @return  String  the site preset
     */
    public String getSitePreset()
    {
        return this.siteInfo.getSitePreset();
    }
    
    /**
     * Set the short name
     * 
     * @return  String  the short name
     */
    public String getShortName()
    {
        return this.siteInfo.getShortName();
    }
    
    /**
     * Get the title
     * 
     * @return  String  the site title
     */
    public String getTitle()
    {
        return this.siteInfo.getTitle();
    }

    /**
     * Set the title
     * 
     * @param title     the title
     */
    public void setTitle(String title)
    {
        this.isDirty = true;
        this.siteInfo.setTitle(title);
    }
    
    /**
     * Get the description
     * 
     * @return  String  the description
     */
    public String getDescription()
    {
        return this.siteInfo.getDescription();
    }
    
    /**
     * Set the description
     * 
     * @param description   the description
     */
    public void setDescription(String description)
    {
        this.isDirty = true;
        this.siteInfo.setDescription(description);
    }
    
    /**
     * Gets whether the site is public or not
     * 
     * @return  true is public false otherwise
     */
    public boolean getIsPublic()
    {
        return this.siteInfo.getIsPublic();
    }
    
    /**
     * Set whether the site is public or not
     * 
     * @param isPublic  true the site is public false otherwise
     */
    public void setIsPublic(boolean isPublic)
    {
        this.isDirty = true;
        this.siteInfo.setIsPublic(isPublic);
    }
    
    /**
     * Get the site node, null if none
     * 
     * @return  ScriptNode  site node
     */
    public ScriptNode getNode()
    {
        ScriptNode node = null;
        if (this.siteInfo.getNodeRef() != null)
        {
            node = new ScriptNode(this.siteInfo.getNodeRef(), this.serviceRegistry, this.scope);
        }
        
        return node;
    }
    
    /**
     * Saves any outstanding updates to the site details.  
     * <p>
     * If properties of the site are changed and save is not called, those changes will be lost.
     */
    public void save()
    {
        if (this.isDirty == true)
        {
            // Update the site details
            this.siteService.updateSite(this.siteInfo);
            
            // Reset the dirty flag
            this.isDirty = false;
        }
    }
    
    /**
     * Deletes the site
     */
    public void deleteSite()
    {
        // Delete the site
        this.siteService.deleteSite(this.siteInfo.getShortName());
    }
    
    /**
     * Gets a map of members of the site with their role within the site.  This list can
     * be filtered by name and/or role.
     * <p>
     * If no name or role filter is specified all members of the site are listed. 
     * 
     * @param nameFilter                            user name filter
     * @param roleFilter                            user role filter
     * @return ScriptableHashMap<String, String>    list of members of site with their roles
     */
    public ScriptableHashMap<String, String> listMembers(String nameFilter, String roleFilter)
    {
        Map<String, String> members =  this.siteService.listMembers(getShortName(), nameFilter, roleFilter);
        
        ScriptableHashMap<String, String> result = new ScriptableHashMap<String, String>();
        result.putAll(members);
        
        return result;
    }
    
    /**
     * Gets a user's role on this site.
     * <p>
     * If the user is not a member of the site then null is returned.
     * 
     * @param userName  user name
     * @return String   user's role or null if not a member
     */
    public String getMembersRole(String userName)
    {
        return this.siteService.getMembersRole(getShortName(), userName);
    }
    
    /**
     * Indicates whether a user is a member of the site.
     * 
     * @param userName  user name
     * @return boolean  true if the user is a member of the site, false otherwise
     */
    public boolean isMember(String userName)
    {
        return this.siteService.isMember(getShortName(), userName);
    }
    
    /**
     * Sets the membership details for a user.
     * <p>
     * If the user is not already a member of the site then they are added with the role
     * given.  If the user is already a member of the site then their role is updated to the new role.
     * <p>
     * Only a site manager can modify memberships and there must be at least one site manager at
     * all times.
     * 
     * @param userName  user name
     * @param role      site role
     */
    public void setMembership(String userName, String role)
    {
        this.siteService.setMembership(getShortName(), userName, role);
    }
    
    /**
     * Removes a users membership of the site.
     * <p>
     * Only a site manager can remove a user's membership and the last site manager can not be removed.
     * 
     * @param userName  user name
     */
    public void removeMembership(String userName)
    {
        this.siteService.removeMembership(getShortName(), userName);
    }

    /**
     * Gets (or creates) the "container" folder for the specified component id
     * 
     * @param componentId
     * @return node representing the "container" folder (or null, if for some reason 
     *         the container can not be created)
     */
    public ScriptNode getContainer(String componentId)
    {
    	ScriptNode container = null;
    	try
		{
    		NodeRef containerNodeRef = this.siteService.getContainer(getShortName(), componentId);
    		if (containerNodeRef != null)
    		{
    		    container = new ScriptNode(containerNodeRef, this.serviceRegistry, this.scope);
    		}
		}
    	catch(AlfrescoRuntimeException e)
    	{
    		// NOTE: not good practice to catch all, but in general we're not throwing exceptions
    		//       into the script layer
    	}
    	return container;
    }
    
    /**
     * Creates a new site container 
     * 
     * @param componentId   component id
     * @return ScriptNode   the created container
     */
    public ScriptNode createContainer(String componentId)
    {
        return createContainer(componentId, null, null);
    }
    
    /**
     * Creates a new site container
     * 
     * @param componentId   component id
     * @param folderType    folder type to create
     * @return ScriptNode   the created container
     */
    public ScriptNode createContainer(String componentId, String folderType)
    {
        return createContainer(componentId, folderType, null);
    }

    /**
     * Creates a new site container
     * 
     * @param componentId   component id
     * @param folderType    folder type to create
     * @return ScriptNode   the created container
     */
    public ScriptNode createContainer(final String componentId, final String folderType, final Object permissions)
    {
        ScriptNode container = null;
        try
        {
            NodeRef containerNodeRef = AuthenticationUtil.runAs(new RunAsWork<NodeRef>()
            {
                public NodeRef doWork() throws Exception
                {
                    // Get the container type
                    QName folderQName = (folderType == null) ? null : QName.createQName(folderType, serviceRegistry.getNamespaceService());
                    
                    // Create the container node
                    NodeRef containerNodeRef = Site.this.siteService.createContainer(getShortName(), componentId, folderQName, null);
                    
                    // Set any permissions that might have been provided for the container
                    if (permissions != null && permissions instanceof ScriptableObject)
                    {
                        ScriptableObject scriptable = (ScriptableObject)permissions;
                        Object[] propIds = scriptable.getIds();
                        for (int i = 0; i < propIds.length; i++)
                        {
                            // work on each key in turn
                            Object propId = propIds[i];
                            
                            // we are only interested in keys that are formed of Strings
                            if (propId instanceof String)
                            {
                                // get the value out for the specified key - it must be String
                                final String key = (String)propId;
                                final Object value = scriptable.get(key, scriptable);
                                if (value instanceof String)
                                {                                   
                                    // Set the permission on the container
                                    Site.this.serviceRegistry.getPermissionService().setPermission(containerNodeRef, key, (String)value, true);
                                }
                            }
                        }
                    }            
            
                    return containerNodeRef;
                }
            }, AuthenticationUtil.SYSTEM_USER_NAME);
            
            // Create the script node for the container
            container = new ScriptNode(containerNodeRef, this.serviceRegistry, this.scope); 
        }
        catch(AlfrescoRuntimeException e)
        {
            // NOTE: not good practice to catch all, but in general we're not throwing exceptions
            //       into the script layer
        }
        return container;        
    }
    
    /**
     * Determine if the "container" folder for the specified component exists
     * 
     * @param componentId
     * @return  true => "container" folder exists
     */
    public boolean hasContainer(String componentId)
    {
    	boolean hasContainer = false;
    	try
    	{
    		hasContainer = this.siteService.hasContainer(getShortName(), componentId);
    	}
    	catch(AlfrescoRuntimeException e)
    	{
    		// NOTE: not good practice to catch all, but in general we're not throwing exceptions
    		//       into the script layer
    	}
    	return hasContainer;
    }
    
}
