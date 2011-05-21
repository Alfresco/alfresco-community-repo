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
package org.alfresco.repo.site.script;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.invitation.InvitationSearchCriteriaImpl;
import org.alfresco.repo.invitation.script.ScriptInvitation;
import org.alfresco.repo.invitation.script.ScriptInvitationFactory;
import org.alfresco.repo.jscript.ContentAwareScriptableQNameMap;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.jscript.ScriptableHashMap;
import org.alfresco.repo.jscript.ScriptableQNameMap;
import org.alfresco.repo.jscript.ScriptNode.NodeValueConverter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.invitation.Invitation;
import org.alfresco.service.cmr.invitation.InvitationException;
import org.alfresco.service.cmr.invitation.InvitationService;
import org.alfresco.service.cmr.invitation.InvitationSearchCriteria.InvitationType;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.namespace.QName;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.springframework.extensions.surf.util.ParameterCheck;

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
    
    /** Site group information */
    private String siteGroup;
    private ScriptableHashMap<String, String> siteRoleGroups;
    
    /** The custom properties of the site */
    private ScriptableQNameMap<String, CustomProperty> customProperties = null;
    
    /** Services Registry */
    private ServiceRegistry serviceRegistry;
    
    /** Site service */
    private SiteService siteService;

    /** Scriptable */
    private Scriptable scope;
    
    /** Indicates whether there are any outstanding changes that need to be saved */
    private boolean isDirty = false;

    private final ScriptInvitationFactory scriptInvitationFactory;

    private final InvitationService invitationService;
    
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
        this.invitationService = serviceRegistry.getInvitationService();
        NodeService nodeService = serviceRegistry.getNodeService();
        PersonService personService = serviceRegistry.getPersonService();
        this.scriptInvitationFactory = new ScriptInvitationFactory(invitationService, nodeService, personService);
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
     * @return      true is public false otherwise
     * @deprecated  since version 3.2, replaced by {@link #getVisibility()}
     */
    public boolean getIsPublic()
    {
        return this.siteInfo.getIsPublic();
    }
    
    /**
     * Set whether the site is public or not
     * 
     * @param isPublic  true the site is public false otherwise
     * @deprecated      since version 3.2, replaced by {@link #setVisibility(String)}
     */
    public void setIsPublic(boolean isPublic)
    {
        this.isDirty = true;
        this.siteInfo.setIsPublic(isPublic);
    }
    
    /**
     * Get the site visibility
     * 
     * @return  String  site visibility
     */
    public String getVisibility()
    {
        return this.siteInfo.getVisibility().toString();
    }
    
    /**
     * Set the site visibility
     * 
     * @param visibility    site visibility (public|moderated|private)
     */
    public void setVisibility(String visibility)
    {
        ParameterCheck.mandatoryString("visibility", visibility);
        SiteVisibility siteVisibility = SiteVisibility.valueOf(visibility);
        this.siteInfo.setVisibility(siteVisibility);
        this.isDirty = true;
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
     * Get the site group name
     * 
     * @return  String  site group name
     */
    public String getSiteGroup()
    {
        if (this.siteGroup == null)
        {
            this.siteGroup = this.siteService.getSiteGroup(this.siteInfo.getShortName());
        }
        return this.siteGroup;
    }
    
    /**
     * Gets a map of role name mapping to associated group name.
     * 
     * @return  ScriptableMap<String, String>   map of role to group name
     */
    public ScriptableHashMap<String, String> getSitePermissionGroups()
    {
        if (this.siteRoleGroups == null)
        {
            List<String> roles = this.siteService.getSiteRoles(
                    this.siteInfo.getShortName()
            );
            this.siteRoleGroups = new ScriptableHashMap<String, String>();
            for (String role : roles)
            {
                this.siteRoleGroups.put(
                        role, 
                        this.siteService.getSiteRoleGroup(this.siteInfo.getShortName(), role));
            }
        }
        return this.siteRoleGroups;
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
     * <p>
     * This list includes both users and groups if collapseGroups is set to false, otherwise all
     * groups that are members are collapsed into their component users and listed.
     * 
     * @param nameFilter               user name filter
     * @param roleFilter               user role filter
     * @param size                     max results size crop if >0
     * @param collapseGroups           true if collapse member groups into user list, false otherwise
     * 
     * @return ScriptableHashMap<String, String>    list of members of site with their roles
     */
    public ScriptableHashMap<String, String> listMembers(String nameFilter, String roleFilter, int size, boolean collapseGroups)
    {
        Map<String, String> members =  this.siteService.listMembers(getShortName(), nameFilter, roleFilter, size, collapseGroups);
        
        ScriptableHashMap<String, String> result = new ScriptableHashMap<String, String>();
        result.putAll(members);
        
        return result;
    }
    
    /**
     * Gets a user's role on this site.
     * <p>
     * If the user is not a member of the site then null is returned.
     * 
     * @param authorityName  authority name
     * @return String   user's role or null if not a member
     */
    public String getMembersRole(String authorityName)
    {
        return this.siteService.getMembersRole(getShortName(), authorityName);
    }
    
    /**
     * Indicates whether a user is a member of the site.
     * 
     * @param authorityName  user name
     * @return boolean  true if the user is a member of the site, false otherwise
     */
    public boolean isMember(String authorityName)
    {
        return this.siteService.isMember(getShortName(), authorityName);
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
     * @param authorityName  authority name
     * @param role      site role
     */
    public void setMembership(String authorityName, String role)
    {
        this.siteService.setMembership(getShortName(), authorityName, role);
    }
    
    /**
     * Removes a user or group membership from a site.
     * <p>
     * Only a site manager can remove a user's membership and the last site manager can not be removed.
     * 
     * @param authorityName  authority name
     */
    public void removeMembership(String authorityName)
    {
        this.siteService.removeMembership(getShortName(), authorityName);
    }

    /**
     * Gets (or creates) the "container" folder for the specified component id
     * 
     * @param componentId
     * @return node representing the "container" folder (or null, if for some reason 
     *         the container can not be created - probably due to permissions)
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
        catch (AccessDeniedException ade)
        {
            return null;
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
        NodeRef containerNodeRef = AuthenticationUtil.runAs(new RunAsWork<NodeRef>()
        {
            public NodeRef doWork() throws Exception
            {
                // Get the container type
                QName folderQName = (folderType == null) ? null : QName.createQName(folderType, serviceRegistry.getNamespaceService());
                
                // Create the container node
                NodeRef containerNode = Site.this.siteService.createContainer(getShortName(), componentId, folderQName, null);
                
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
                                Site.this.serviceRegistry.getPermissionService().setPermission(containerNode, key, (String)value, true);
                            }
                        }
                    }
                }
                
                // Make the "admin" the owner of the node
                serviceRegistry.getOwnableService().setOwner(containerNode, AuthenticationUtil.getAdminUserName());
        
                return containerNode;
            }
        }, AuthenticationUtil.SYSTEM_USER_NAME);
        
        if (Site.this.serviceRegistry.getPermissionService().hasPermission(containerNodeRef, PermissionService.READ_PROPERTIES) == AccessStatus.ALLOWED) 
        {
            return getContainer(componentId); 
        }
        else
        {
            // current user has no access.
            return null;
        }          
    
    }

    /**
     * This method creates a container of the specified id and type, sets the cm:description
     * on that container node to the specified value and saves the container node updates to the repository.
     * All of this is run as system.
     * 
     * @param containerId an id for the container node.
     * @param containerType the type for the container node.
     * @param description a value for the cm:description property on the container node.
     * 
     * @return the newly created and saved container {@link ScriptNode}.
     * @since 3.4
     */
    public ScriptNode createAndSaveContainer(String containerId, String containerType, final String description)
    {
    	// Implementation node. See ALF-4282 for details.
    	//
    	// The container for the "data lists" page within a Share site is lazily created the first time
    	// that a user navigates to that page. However if the first Share user to look at the data lists
    	// page for a site is not a site manager then they will not have the necessary permissions to
    	// create the container node.
    	// For this reason we need to create the node, set its cm:description and save those changes
    	// as system.
    	
    	// The container creation is already run as system, so we don't need to double-wrap this first call
    	// in a RunAs class.
    	final ScriptNode result = this.createContainer(containerId, containerType);
    	
    	if (result == null)
    	{
    		return null;
    	}

        AuthenticationUtil.runAs(new RunAsWork<Void>()
            {
                public Void doWork() throws Exception
                {
                	serviceRegistry.getNodeService().setProperty(result.getNodeRef(),
                			                                     ContentModel.PROP_DESCRIPTION, description);
                	result.save();
                	return null;
                }
            }, AuthenticationUtil.SYSTEM_USER_NAME);
        
        return result;
    }
    
    /**
     * Determine if the "container" folder for the specified component exists
     * 
     * @param componentId
     * @return  true => "container" folder exists
     */
    public boolean hasContainer(String componentId)
    {
    	return this.siteService.hasContainer(getShortName(), componentId);
    }
    
    /**
     * Apply a set of permissions to the node.
     * 
     * @param nodeRef   node reference
     */
    public void setPermissions(final ScriptNode node, final Object permissions)
    {
        final NodeRef nodeRef = node.getNodeRef();
        
        if (permissions != null && permissions instanceof ScriptableObject)
        {
            // Get the permission service
            final PermissionService permissionService = this.serviceRegistry.getPermissionService();
            
            if (!permissionService.getInheritParentPermissions(nodeRef))
            {
                // remove existing permissions
                permissionService.deletePermissions(nodeRef);
            }
            
            // Assign the correct permissions
            ScriptableObject scriptable = (ScriptableObject)permissions;
            Object[] propIds = scriptable.getIds();
            for (int i = 0; i < propIds.length; i++)
            {
                // Work on each key in turn
                Object propId = propIds[i];
                
                // Only interested in keys that are formed of Strings
                if (propId instanceof String)
                {
                    // Get the value out for the specified key - it must be String
                    final String key = (String)propId;
                    final Object value = scriptable.get(key, scriptable);
                    if (value instanceof String)
                    {                                   
                        // Set the permission on the node
                        permissionService.setPermission(nodeRef, key, (String)value, true);
                    }
                }
            }
            
            // always add the site managers group with SiteManager permission
            String managers = this.siteService.getSiteRoleGroup(getShortName(), SiteModel.SITE_MANAGER);
            permissionService.setPermission(nodeRef, managers, SiteModel.SITE_MANAGER, true);
            
            // now turn off inherit to finalize our permission changes
            permissionService.setInheritParentPermissions(nodeRef, false);
        }
        else
        {
        	// No permissions passed-in
        	this.resetAllPermissions(node);
        }
    }
    
    /**
     * Reset any permissions that have been set on the node.  
     * <p>
     * All permissions will be deleted and the node set to inherit permissions.
     * 
     * @param nodeRef   node reference
     */
    public void resetAllPermissions(ScriptNode node)
    {
        final NodeRef nodeRef = node.getNodeRef();
        
        PermissionService permissionService = serviceRegistry.getPermissionService();
        try
        {
            // Ensure node isn't inheriting permissions from an ancestor before deleting
            if (!permissionService.getInheritParentPermissions(nodeRef))
            {
                permissionService.deletePermissions(nodeRef);
                permissionService.setInheritParentPermissions(nodeRef, true);
            }
        }
        catch (AccessDeniedException e)
        {
            throw new AlfrescoRuntimeException("You do not have the authority to update permissions on this node.", e);
        }
    }  
    
    /**
     * Get the value of a custom property, null if the custom property has not been set or doesn't exist.
     * 
     * @param  name             qname of the property 
     * @return Serializable     value of the property, null if not set
     */
    public CustomProperty getCustomProperty(String name)
    {
        return (CustomProperty)getCustomProperties().get(name);
    }
    
    /**
     * Get a map of the sites custom properties
     * 
     * @return ScriptableQNameMap<String, Serializable>     map of names and values
     */
    public ScriptableQNameMap<String, CustomProperty> getCustomProperties()
    {
        if (this.customProperties == null)
        {
            // create the custom properties map
            ScriptNode siteNode = new ScriptNode(this.siteInfo.getNodeRef(), this.serviceRegistry);
            this.customProperties = new ContentAwareScriptableQNameMap<String, CustomProperty>(siteNode, this.serviceRegistry);
            
            Map<QName, Serializable> props = siteInfo.getCustomProperties();
            for (QName qname : props.keySet())
            {
                // get the property value
                Serializable propValue = props.get(qname);
                
                // convert the value
                NodeValueConverter valueConverter = siteNode.new NodeValueConverter();
                Serializable value = valueConverter.convertValueForScript(qname, propValue);
                
                // get the type and label information from the dictionary
                String title = null;
                String type = null;
                PropertyDefinition propDef = this.serviceRegistry.getDictionaryService().getProperty(qname);
                if (propDef != null)
                {
                    type = propDef.getDataType().getName().toString();
                    title = propDef.getTitle();
                }
                
                // create the custom property and add to the map
                CustomProperty customProp = new CustomProperty(qname.toString(), value, type, title);
                this.customProperties.put(qname.toString(), customProp);
            }
        }
        return this.customProperties;
    }
    
    /**
     * Create new moderated invitation to this web site
     * @return the new invitation
     */
    public ScriptInvitation<?> inviteModerated(String inviteeComments, String inviteeUserName, String inviteeRole)
    {
    	Invitation invitation = invitationService.inviteModerated(inviteeComments, inviteeUserName, Invitation.ResourceType.WEB_SITE, getShortName(), inviteeRole);
    	return scriptInvitationFactory.toScriptInvitation(invitation);
    }
    
    /**
     * Create new nominated invitation to this web site
     * @return the new invitation
     */
    public ScriptInvitation<?> inviteNominated(String inviteeFirstName, String inviteeLastName, String inviteeEmail, String inviteeRole, String serverPath, String acceptUrl, String rejectUrl)
    {    	
    	Invitation invitation = invitationService.inviteNominated(inviteeFirstName, inviteeLastName, inviteeEmail, Invitation.ResourceType.WEB_SITE, getShortName(), inviteeRole, serverPath, acceptUrl, rejectUrl);
    	return scriptInvitationFactory.toScriptInvitation(invitation);
    }
    
    /**
     * Create new nominated invitation to this web site
     * @return the new invitation
     */
    public ScriptInvitation<?> inviteNominated(String inviteeUserName, String inviteeRole, String serverPath, String acceptUrl, String rejectUrl)
    {    	
    	Invitation invitation = invitationService.inviteNominated(inviteeUserName, Invitation.ResourceType.WEB_SITE, getShortName(), inviteeRole, serverPath, acceptUrl, rejectUrl);
    	return scriptInvitationFactory.toScriptInvitation(invitation);
    }
    
    /**
     * Get an invitation to this web site
     * @return the invitation or null if it does not exist
     */
    public ScriptInvitation<?> getInvitation(String invitationId)
    {    	
    	try 
    	{
    		Invitation invitation = invitationService.getInvitation(invitationId);
    		return scriptInvitationFactory.toScriptInvitation(invitation);
    	} 
    	catch (InvitationException e)
    	{
    		return null;
        }
    }
    
    /**
     * list the outstanding invitations for this site
     * 
     * Map of name / invitation
     */
    public ScriptInvitation<?>[] listInvitations()
    {
    	List<Invitation> invitations = invitationService.listPendingInvitationsForResource(Invitation.ResourceType.WEB_SITE, getShortName());
    	ScriptInvitation<?>[] ret = new ScriptInvitation[invitations.size()];
        int i = 0;
		for(Invitation item : invitations)
		{
			ret[i++] = scriptInvitationFactory.toScriptInvitation(item);
		}
    	return ret;
    }
    
    /**
     * List the open invitations for this web site.
     * props specifies optional properties to be searched.
     * 
     * @param props inviteeUserName
     *
     * @return the invitations
     */
    public ScriptInvitation<?>[] listInvitations(Scriptable props)
    {
    	InvitationSearchCriteriaImpl crit = new InvitationSearchCriteriaImpl();
    	crit.setResourceName(getShortName());
    	crit.setResourceType(Invitation.ResourceType.WEB_SITE);
    	
    	if (props.has("inviteeUserName", props))
    	{
    		crit.setInvitee((String)props.get("inviteeUserName", props));
      	}
    	if (props.has("invitationType", props))
    	{
    		String invitationType = (String)props.get("invitationType", props);
    		crit.setInvitationType(InvitationType.valueOf(invitationType));
        }

    	List<Invitation> invitations = invitationService.searchInvitation(crit);
    	ScriptInvitation<?>[] ret = new ScriptInvitation[invitations.size()];
        int i = 0;
		for(Invitation item : invitations)
		{
			ret[i++] = scriptInvitationFactory.toScriptInvitation(item);
		}
    	return ret;
    }
    
    /**
     * Custom property helper class
     * 
     * @author Roy Wetherall
     */
    public class CustomProperty
    {
        /** Details of the custom property */
        private String name;
        private Serializable value;
        private String type;
        private String title;
        
        /**
         * Constructor
         * 
         * @param name      property name
         * @param value     property value
         * @param type      property type
         * @param title     property title
         */
        public CustomProperty(String name, Serializable value, String type, String title)
        {
            this.name = name;
            this.value = value;
            this.type = type;
            this.title = title;
        }
        
        public String getName()
        {
            return name;
        }
        
        public Serializable getValue()
        {
            return value;
        }
        
        public String getType()
        {
            return type;
        }
        
        public String getTitle()
        {
            return title;
        }
    }
}
