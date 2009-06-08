/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.admin.patch.impl;

import java.util.Collection;
import java.util.List;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.importer.ImporterBootstrap;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;

/**
 * Migrates authority information previously stored in the user store to the spaces store, using the new structure used
 * by AuthorityService.
 * 
 * @author dward
 */
public class AuthorityMigrationPatch extends AbstractPatch
{

    /** Success message. */
    private static final String MSG_SUCCESS = "patch.authorityMigration.result";

    /** The old authority name property */
    private static final QName PROP_AUTHORITY_NAME = QName.createQName(ContentModel.USER_MODEL_URI, "authorityName");

    /** The old authority display name property */
    private static final QName PROP_AUTHORITY_DISPLAY_NAME = QName.createQName(ContentModel.USER_MODEL_URI,
            "authorityDisplayName");

    /** The old authority members property */
    private static final QName PROP_MEMBERS = QName.createQName(ContentModel.USER_MODEL_URI, "members");

    /** The authority service. */
    private AuthorityService authorityService;

    /** The user bootstrap. */
    private ImporterBootstrap userBootstrap;

    /**
     * Sets the authority service.
     * 
     * @param authorityService
     *            the authority service
     */
    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }

    /**
     * Sets the user bootstrap.
     * 
     * @param userBootstrap
     *            the user bootstrap
     */
    public void setUserBootstrap(ImporterBootstrap userBootstrap)
    {
        this.userBootstrap = userBootstrap;
    }

    /**
     * Recursively migrates the authorities under the given node
     * 
     * @param parentAuthority
     *            the full name of the parent authority corresponding to the given node, or <code>null</code> if it is
     *            not an authority node.
     * @param nodeRef
     *            the node to find authorities below
     * @return the number of processed authorities
     */
    private int migrateAuthorities(String parentAuthority, NodeRef nodeRef)
    {
        int processedCount = 0;
        List<ChildAssociationRef> cars = this.nodeService.getChildAssocs(nodeRef);
        for (ChildAssociationRef car : cars)
        {
            NodeRef current = car.getChildRef();
            String authorityName = DefaultTypeConverter.INSTANCE.convert(String.class, this.nodeService.getProperty(
                    current, AuthorityMigrationPatch.PROP_AUTHORITY_NAME));
            boolean existed = this.authorityService.authorityExists(authorityName);
            if (!existed)
            {
                String authorityDisplayName = DefaultTypeConverter.INSTANCE.convert(String.class, this.nodeService
                        .getProperty(current, AuthorityMigrationPatch.PROP_AUTHORITY_DISPLAY_NAME));
                this.authorityService.createAuthority(AuthorityType.getAuthorityType(authorityName),
                        this.authorityService.getShortName(authorityName), authorityDisplayName, null);
                processedCount++;
            }
            if (parentAuthority != null
                    && (!existed || !this.authorityService.getContainingAuthorities(AuthorityType.GROUP, authorityName,
                            true).contains(parentAuthority)))
            {
                this.authorityService.addAuthority(parentAuthority, authorityName);
            }

            // loop over properties
            Collection<String> members = DefaultTypeConverter.INSTANCE.getCollection(String.class, this.nodeService
                    .getProperty(current, AuthorityMigrationPatch.PROP_MEMBERS));
            if (members != null)
            {
                for (String user : members)
                {
                    // Believe it or not, some old authorities have null members in them!
                    if (user != null
                            && (!existed || !this.authorityService.getContainingAuthorities(AuthorityType.GROUP, user,
                                    true).contains(authorityName)))
                    {
                        this.authorityService.addAuthority(authorityName, user);
                    }
                }
            }
            processedCount += migrateAuthorities(authorityName, current);
        }
        return processedCount;
    }

    /**
     * Gets the old authority container.
     * 
     * @return Returns the old authority container or <code>null</code> if not found
     */
    private NodeRef getAuthorityContainer()
    {
        NodeRef rootNodeRef = this.nodeService.getRootNode(this.userBootstrap.getStoreRef());
        QName qnameAssocSystem = QName.createQName("sys", "system", this.namespaceService);
        List<ChildAssociationRef> results = this.nodeService.getChildAssocs(rootNodeRef, RegexQNamePattern.MATCH_ALL,
                qnameAssocSystem);
        NodeRef sysNodeRef = null;
        if (results.size() == 0)
        {
            return null;
        }
        else
        {
            sysNodeRef = results.get(0).getChildRef();
        }
        QName qnameAssocAuthorities = QName.createQName("sys", "authorities", this.namespaceService);
        results = this.nodeService.getChildAssocs(sysNodeRef, RegexQNamePattern.MATCH_ALL, qnameAssocAuthorities);
        NodeRef authNodeRef = null;
        if (results.size() == 0)
        {
            return null;
        }
        else
        {
            authNodeRef = results.get(0).getChildRef();
        }
        return authNodeRef;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.admin.patch.AbstractPatch#applyInternal()
     */
    @Override
    protected String applyInternal() throws Exception
    {
        int processedCount = 0;
        NodeRef authorityContainer = getAuthorityContainer();
        if (authorityContainer != null)
        {
            processedCount = migrateAuthorities(null, authorityContainer);
        }
        // build the result message
        return I18NUtil.getMessage(AuthorityMigrationPatch.MSG_SUCCESS, processedCount);
    }
}