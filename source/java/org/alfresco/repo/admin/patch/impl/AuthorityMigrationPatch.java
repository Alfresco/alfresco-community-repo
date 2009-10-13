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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.admin.patch.PatchExecuter;
import org.alfresco.repo.importer.ImporterBootstrap;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Migrates authority information previously stored in the user store to the spaces store, using the new structure used
 * by AuthorityService.
 * 
 * @author dward
 */
public class AuthorityMigrationPatch extends AbstractPatch
{

    /** Progress message for authorities. */
    private static final String MSG_PROGRESS_AUTHORITY = "patch.authorityMigration.progress.authority";

    /** Progress message for associations. */
    private static final String MSG_PROGRESS_ASSOC = "patch.authorityMigration.progress.assoc";

    /** Success message. */
    private static final String MSG_SUCCESS = "patch.authorityMigration.result";

    /** The progress_logger. */
    private static Log progress_logger = LogFactory.getLog(PatchExecuter.class);

    /** The old authority name property. */
    private static final QName PROP_AUTHORITY_NAME = QName.createQName(ContentModel.USER_MODEL_URI, "authorityName");

    /** The old authority display name property. */
    private static final QName PROP_AUTHORITY_DISPLAY_NAME = QName.createQName(ContentModel.USER_MODEL_URI,
            "authorityDisplayName");

    /** The old authority members property. */
    private static final QName PROP_MEMBERS = QName.createQName(ContentModel.USER_MODEL_URI, "members");

    /** The number of items to create in a transaction. */
    private static final int BATCH_SIZE = 10;

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
     * Recursively retrieves the authorities under the given node and their associations.
     * 
     * @param parentAuthority
     *            the full name of the parent authority corresponding to the given node, or <code>null</code> if it is
     *            not an authority node.
     * @param nodeRef
     *            the node to find authorities below
     * @param authoritiesToCreate
     *            the authorities to create
     * @param childAssocs
     *            the child associations
     */
    private void retrieveAuthorities(String parentAuthority, NodeRef nodeRef, Map<String, String> authoritiesToCreate,
            Map<String, Set<String>> childAssocs)
    {
        // If we have a parent authority, prepare a list for recording its children
        Set<String> children = parentAuthority == null ? null : childAssocs.get(parentAuthority);

        // Process all children
        List<ChildAssociationRef> cars = this.nodeService.getChildAssocs(nodeRef);

        for (ChildAssociationRef car : cars)
        {
            NodeRef current = car.getChildRef();

            // Record an authority to create
            String authorityName = DefaultTypeConverter.INSTANCE.convert(String.class, this.nodeService.getProperty(
                    current, AuthorityMigrationPatch.PROP_AUTHORITY_NAME));
            authoritiesToCreate.put(authorityName, DefaultTypeConverter.INSTANCE.convert(String.class, this.nodeService
                    .getProperty(current, AuthorityMigrationPatch.PROP_AUTHORITY_DISPLAY_NAME)));

            // If we have a parent, remember the child association
            if (parentAuthority != null)
            {
                if (children == null)
                {
                    children = new TreeSet<String>();
                    childAssocs.put(parentAuthority, children);
                }
                children.add(authorityName);
            }

            // loop over properties
            Set<String> propChildren = childAssocs.get(authorityName);

            Collection<String> members = DefaultTypeConverter.INSTANCE.getCollection(String.class, this.nodeService
                    .getProperty(current, AuthorityMigrationPatch.PROP_MEMBERS));
            if (members != null)
            {
                for (String user : members)
                {
                    // Believe it or not, some old authorities have null members in them!
                    if (user != null)
                    {
                        if (propChildren == null)
                        {
                            propChildren = new TreeSet<String>();
                            childAssocs.put(authorityName, propChildren);
                        }
                        propChildren.add(user);
                    }
                }
            }
            retrieveAuthorities(authorityName, current, authoritiesToCreate, childAssocs);
        }
    }

    /**
     * Migrates the authorities.
     * 
     * @param authoritiesToCreate
     *            the authorities to create
     * @return the number of authorities migrated
     */
    private int migrateAuthorities(Map<String, String> authoritiesToCreate)
    {
        int processedCount = 0;
        final Iterator<Map.Entry<String, String>> i = authoritiesToCreate.entrySet().iterator();
        RetryingTransactionHelper retryingTransactionHelper = this.transactionService.getRetryingTransactionHelper();

        // Process batches in separate transactions for maximum performance
        while (i.hasNext())
        {
            processedCount += retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Integer>()
            {
                public Integer execute() throws Throwable
                {
                    int processedCount = 0;
                    do
                    {
                        Map.Entry<String, String> authority = i.next();
                        String authorityName = authority.getKey();
                        boolean existed = AuthorityMigrationPatch.this.authorityService.authorityExists(authorityName);
                        if (existed)
                        {
                            i.remove();
                        }
                        else
                        {
                            AuthorityMigrationPatch.this.authorityService.createAuthority(AuthorityType
                                    .getAuthorityType(authorityName), AuthorityMigrationPatch.this.authorityService
                                    .getShortName(authorityName), authority.getValue(), null);
                            processedCount++;
                        }
                    }
                    while (processedCount < AuthorityMigrationPatch.BATCH_SIZE && i.hasNext());
                    return processedCount;
                }
            }, false, true);

            // Report progress
            AuthorityMigrationPatch.progress_logger.info(I18NUtil.getMessage(
                    AuthorityMigrationPatch.MSG_PROGRESS_AUTHORITY, processedCount));
        }
        return processedCount;
    }

    /**
     * Migrates the group associations.
     * 
     * @param authoritiesCreated
     *            the authorities created
     * @param childAssocs
     *            the child associations
     * @return the number of associations migrated
     */
    private int migrateAssocs(final Map<String, String> authoritiesCreated, Map<String, Set<String>> childAssocs)
    {
        int processedCount = 0;
        final Iterator<Map.Entry<String, Set<String>>> j = childAssocs.entrySet().iterator();
        RetryingTransactionHelper retryingTransactionHelper = this.transactionService.getRetryingTransactionHelper();

        // Process batches in separate transactions for maximum performance
        while (j.hasNext())
        {
            processedCount += retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Integer>()
            {
                public Integer execute() throws Throwable
                {
                    int processedCount = 0;
                    do
                    {
                        Map.Entry<String, Set<String>> childAssoc = j.next();
                        String parentAuthority = childAssoc.getKey();
                        Set<String> knownChildren = authoritiesCreated.containsKey(parentAuthority) ? Collections
                                .<String> emptySet() : AuthorityMigrationPatch.this.authorityService
                                .getContainedAuthorities(AuthorityType.GROUP, parentAuthority, true);
                        for (String authorityName : childAssoc.getValue())
                        {
                            if (!knownChildren.contains(authorityName))
                            {
                                AuthorityMigrationPatch.this.authorityService.addAuthority(parentAuthority,
                                        authorityName);
                                processedCount++;
                            }
                        }
                    }
                    while (processedCount < AuthorityMigrationPatch.BATCH_SIZE && j.hasNext());
                    return processedCount;
                }
            }, false, true);

            // Report progress
            AuthorityMigrationPatch.progress_logger.info(I18NUtil.getMessage(
                    AuthorityMigrationPatch.MSG_PROGRESS_ASSOC, processedCount));
        }
        return processedCount;
    }

    /**
     * Gets the old authority container.
     * 
     * @return the old authority container or <code>null</code> if not found
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
        int authorities = 0;
        int assocs = 0;
        NodeRef authorityContainer = getAuthorityContainer();
        if (authorityContainer != null)
        {
            Map<String, String> authoritiesToCreate = new TreeMap<String, String>();
            Map<String, Set<String>> childAssocs = new TreeMap<String, Set<String>>();
            retrieveAuthorities(null, authorityContainer, authoritiesToCreate, childAssocs);
            authorities = migrateAuthorities(authoritiesToCreate);
            assocs = migrateAssocs(authoritiesToCreate, childAssocs);
        }
        // build the result message
        return I18NUtil.getMessage(AuthorityMigrationPatch.MSG_SUCCESS, authorities, assocs);
    }
}