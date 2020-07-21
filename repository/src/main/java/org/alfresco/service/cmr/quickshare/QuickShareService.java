/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.service.cmr.quickshare;

import java.util.Date;
import java.util.Map;

import org.alfresco.repo.quickshare.QuickShareServiceImpl.QuickShareEmailRequest;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.Pair;

/**
 * The QuickShare service.
 * 
 * Responsible for creating, updating and retrieving Quick share metadata,
 *
 * @author Alex Miller, janv
 * @since Cloud/4.2
 */
public interface QuickShareService
{
    /**
     * Share content identified by nodeRef.
     * 
     * @param nodeRef The NodeRef of the content to share
     * @return QuickDTO with details of the share
     */
    public QuickShareDTO shareContent(NodeRef nodeRef) throws QuickShareDisabledException, InvalidNodeRefException;

    /**
     * Share content identified by nodeRef and optionally set an expiry date for the shared link.
     *
     * @param nodeRef The NodeRef of the content to share
     * @param expiryDate The expiry date of the shared link
     * @return QuickDTO with details of the share
     */
    QuickShareDTO shareContent(NodeRef nodeRef, Date expiryDate) throws QuickShareDisabledException, InvalidNodeRefException;

    /**
     * Get QuickShare related metadata for the given node.
     *  
     * @param nodeRef NodeRef
     * @return Map
     */
    public Map<String, Object> getMetaData(NodeRef nodeRef) throws QuickShareDisabledException, InvalidNodeRefException;

    /**
     * Get QuickShare related metadata for the given shareId.
     *
     * @param shareId String
     * @return Map
     */
    public Map<String, Object> getMetaData(String shareId) throws QuickShareDisabledException, InvalidSharedIdException;

    /**
     * Get the tenant domain and node reference for the the given share id.
     * 
     * @param sharedId String
     * @return Pair
     */
    public Pair<String, NodeRef> getTenantNodeRefFromSharedId(String sharedId) throws QuickShareDisabledException, InvalidSharedIdException;

    /**
     * Unshare the content identified by sharedId
     *  
     * @param sharedId The shared id of the content to unshare.
     */
    public void unshareContent(String sharedId) throws QuickShareDisabledException, InvalidSharedIdException;

    /**
     * Determine if the current user has permission to read the shared content.
     */
    public boolean canRead(String sharedId);

    /**
     * Notifies users by email that a content has been shared with them, and the details of it.
     *
     * @param emailRequest The email details including its template details
     */
    public void sendEmailNotification(QuickShareEmailRequest emailRequest);

    /**
     * Determine if the current user has permission to delete the shared link.
     */
    public boolean canDeleteSharedLink(NodeRef nodeRef, String sharedByUserId);

    /**
     * Whether the quick share is enabled or not.
     *
     * @return <tt>true</tt> if quick share is enabled, false otherwise.
     * @since 5.2
     */
    boolean isQuickShareEnabled();

    /**
     * Removes (hard deletes) the previously persisted {@link QuickShareLinkExpiryAction} and its related
     * schedule {@link org.alfresco.service.cmr.action.scheduled.ScheduledPersistedAction} from the repository.
     *
     * @param quickShareLinkExpiryAction The {@link QuickShareLinkExpiryAction} to be deleted.
     */
    void deleteQuickShareLinkExpiryAction(QuickShareLinkExpiryAction quickShareLinkExpiryAction);
}
