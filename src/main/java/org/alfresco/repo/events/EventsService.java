/*
 * Copyright 2014 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.repo.events;

import java.util.Map;
import java.util.Set;

import org.alfresco.events.types.Property;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Event generation service.
 * 
 * @author steveglover
 *
 */
public interface EventsService
{
	/**
	 * Generate a node moved event.
	 * 
	 * @param oldChildAssocRef
	 * @param newChildAssocRef
	 */
	void nodeMoved(ChildAssociationRef oldChildAssocRef, ChildAssociationRef newChildAssocRef);

	/**
	 * Generate a node renamed event.
	 * 
	 */
	void nodeRenamed(NodeRef nodeRef, String oldName, String newName);

	/**
	 * Generate a node created event.
	 * 
	 * @param nodeRef
	 */
	void nodeCreated(final NodeRef nodeRef);
	
	/**
	 * Generate a node deleted event.
	 * 
	 * @param nodeRef
	 */
	void nodeDeleted(final NodeRef nodeRef);
	
	/**
	 * Generate a node updated event (property and aspect changes).
	 * 
	 * @param nodeRef
	 * @param propertiesAdded
	 * @param propertiesRemoved
	 * @param propertiesChanged
	 * @param aspectsAdded
	 * @param aspectsRemoved
	 */
	void nodeUpdated(NodeRef nodeRef, Map<String, Property> propertiesAdded,
			final Set<String> propertiesRemoved, Map<String, Property> propertiesChanged,
			Set<String> aspectsAdded, Set<String> aspectsRemoved);
	
	/**
	 * Generate a node tag added event.
	 * 
	 * @param nodeRef
	 * @param tag
	 */
	void nodeTagged(NodeRef nodeRef, String tag);
	
	/**
	 * Generate a node tag removed event.
	 * 
	 * @param nodeRef
	 */
	void nodeTagRemoved(NodeRef nodeRef, String tag);
	
	/**
	 * Generate a node liked event.
	 * 
	 * @param nodeRef
	 */
	void nodeLiked(NodeRef nodeRef);
	
	/**
	 * Generate a node unliked event.
	 * @param nodeRef
	 */
	void nodeUnLiked(NodeRef nodeRef);
	
	/**
	 * Generate a node favourited event.
	 * 
	 * @param nodeRef
	 */
	void nodeFavourited(NodeRef nodeRef);
	
	/**
	 * Generate a node un-favourited event.
	 * 
	 * @param nodeRef
	 */
	void nodeUnFavourited(NodeRef nodeRef);
	
	/**
	 * Generate a node commented event.
	 * 
	 * @param nodeRef
	 */
	void nodeCommented(final NodeRef nodeRef, final String comment);
	
	/**
	 * Generate a node content get/read event.
	 * 
	 * @param nodeRef
	 */
	void contentGet(NodeRef nodeRef);
	
	/**
	 * Generate a node write/update event.
	 * 
	 * @param nodeRef
	 */
	void contentWrite(NodeRef nodeRef, QName propertyQName, ContentData value);

	void nodeCheckedOut(NodeRef workingCopyNodeRef);

	void nodeCheckOutCancelled(NodeRef nodeRef);

	void nodeCheckedIn(NodeRef nodeRef);

    /**
     * Generate an authority removed from group event
     * 
     * @param parentGroup the group the authority is removed from
     * @param childAuthority the authority which leaves a certain group
     */
    void authorityRemovedFromGroup(String parentGroup, String childAuthority);

    /**
     * Generate an authority added to a group
     * 
     * @param parentGroup the group the authority is added to
     * @param childAuthority the authority which is added to the group
     */
    void authorityAddedToGroup(String parentGroup, String childAuthority);

    /**
     * Generate an inherit permissions enabled event
     * 
     * @param nodeRef the node which has the permission inheritance enabled
     */
    void inheritPermissionsEnabled(NodeRef nodeRef);

    /** 
     * Generate an inherit permissions disabled event
     * 
     * @param nodeRef the node which has the permission inheritance disabled
     * @param async whether the disabling is done asynchronously or not
     */
    void inheritPermissionsDisabled(NodeRef nodeRef, boolean async);

    /**
     * Generate a revoke local permission event
     * 
     * @param nodeRef the node on which certain local permissions are revoked
     * @param authority the authority which has the permissions revoked
     * @param permission the permissions which are revoked
     */
    void revokeLocalPermissions(NodeRef nodeRef, String authority, String permission);

    /**
     * Generate a grant local permission event
     * 
     * @param nodeRef the node to which certain local permissions are granted
     * @param authority the authority which has the permissions granted
     * @param permission the permissions which are granted
     */
    void grantLocalPermission(NodeRef nodeRef, String authority, String permission);

    /**
     * Generate a group deleted event
     * 
     * @param groupName the group being deleted
     * @param cascade whether it's a cascading delete or not
     */
    void groupDeleted(String groupName, boolean cascade);

    /**
     * Generated a node created event for a secondary child
     * 
     * @param secAssociation the child association being created
     */
    void secondaryAssociationCreated(ChildAssociationRef secAssociation);

    /**
     * Generate a delete event for a secondary child
     * 
     * @param secAssociation the child association being deleted
     */
    void secondaryAssociationDeleted(ChildAssociationRef secAssociation);
}
