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
package org.alfresco.repo.admin.patch.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.admin.patch.PatchExecuter;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.importer.ImporterBootstrap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authority.UnknownAuthorityException;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Migrates authority information previously stored in the user store to the spaces store, using the new structure used
 * by AuthorityService.
 * 
 * @author dward
 */
public class AuthorityMigrationPatch extends AbstractPatch
{
    /** The title we give to the batch process in progress messages / JMX. */
    private static final String MSG_PROCESS_NAME = "patch.authorityMigration.process.name";

    /** The warning message when a 'dangling' assoc is found that can't be created */
    private static final String MSG_WARNING_INVALID_ASSOC = "patch.authorityMigration.warning.assoc";

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

    /** The authority service. */
    private AuthorityService authorityService;

    /** The rule service. */
    private RuleService ruleService;

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
     * Sets the rule service.
     * 
     * @param ruleService
     *            the rule service
     */
    public void setRuleService(RuleService ruleService)
    {
        this.ruleService = ruleService;
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
     * @param parentAssocs
     *            the parent associations
     * @return count of the number of parent associations
     */
    private int retrieveAuthorities(String parentAuthority, NodeRef nodeRef, Map<String, String> authoritiesToCreate,
            Map<String, Set<String>> parentAssocs)
    {
        int assocCount = 0;

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

            // Record the parent association (or empty set if this is a root)
            Set<String> parents = parentAssocs.get(authorityName);
            if (parents == null)
            {
                parents = new TreeSet<String>();
                parentAssocs.put(authorityName, parents);
            }
            if (parentAuthority != null)
            {
                parents.add(parentAuthority);
                assocCount++;
            }
            
            // loop over properties
            Collection<String> members = DefaultTypeConverter.INSTANCE.getCollection(String.class, this.nodeService
                    .getProperty(current, AuthorityMigrationPatch.PROP_MEMBERS));
            if (members != null)
            {
                String tenantDomain = null;
                if (tenantAdminService.isEnabled())
                {
                    tenantDomain = tenantAdminService.getCurrentUserDomain();
                }
                
                for (String user : members)
                {
                    // Believe it or not, some old authorities have null members in them!
                    if (user != null)
                    {
                        if ((tenantDomain != null) && (! (tenantDomain.equals(TenantService.DEFAULT_DOMAIN))))
                        {
                            if (tenantAdminService.getUserDomain(user).equals(TenantService.DEFAULT_DOMAIN))
                            {
                                if (user.equals(tenantAdminService.getBaseNameUser(AuthenticationUtil.getAdminUserName())))
                                {
                                    // MT: workaround for CHK-11393 (eg. EMAIL_CONTRIBUTORS with member "admin" instead of "admin@tenant")
                                    user = tenantAdminService.getDomainUser(user, tenantDomain);
                                }
                            }
                        }
                        
                        Set<String> propParents = parentAssocs.get(user);
                        if (propParents == null)
                        {
                            propParents = new TreeSet<String>();
                            parentAssocs.put(user, propParents);
                        }
                        propParents.add(authorityName);
                        assocCount++;
                    }
                }
            }
            assocCount += retrieveAuthorities(authorityName, current, authoritiesToCreate, parentAssocs);
        }
        return assocCount;
    }

    /**
     * Migrates the authorities and their associations.
     * 
     * @param authoritiesToCreate
     *            the authorities to create
     * @param parentAssocs
     *            the parent associations to create (if they don't exist already)
     * @return the number of authorities migrated
     */
    private void migrateAuthorities(final Map<String, String> authoritiesToCreate, Map<String, Set<String>> parentAssocs)
    {
        final String tenantDomain = tenantAdminService.getCurrentUserDomain();

        BatchProcessor.BatchProcessWorker<Map.Entry<String, Set<String>>> worker = new BatchProcessor.BatchProcessWorker<Map.Entry<String, Set<String>>>()
        {
            public String getIdentifier(Entry<String, Set<String>> entry)
            {
                return entry.getKey();
            }

            public void beforeProcess() throws Throwable
            {
                // Disable rules
                ruleService.disableRules();
                // Authentication
                String systemUser = AuthenticationUtil.getSystemUserName();
                systemUser = tenantAdminService.getDomainUser(systemUser, tenantDomain);
                AuthenticationUtil.setRunAsUser(systemUser);
            }

            public void afterProcess() throws Throwable
            {
                // Enable rules
                ruleService.enableRules();
                // Clear authentication
                AuthenticationUtil.clearCurrentSecurityContext();
            }

            public void process(Entry<String, Set<String>> authority) throws Throwable
            {
                String authorityName = authority.getKey();
                boolean existed = AuthorityMigrationPatch.this.authorityService.authorityExists(authorityName);
                Set<String> knownParents;
                if (existed)
                {
                    knownParents = AuthorityMigrationPatch.this.authorityService.getContainingAuthorities(
                            AuthorityType.GROUP, authorityName, true);
                }
                else
                {
                    knownParents = Collections.emptySet();
                    AuthorityType authorityType = AuthorityType.getAuthorityType(authorityName);
                    // We have associations to a non-existent authority. If it is a user, just skip it because it must
                    // have been a 'dangling' reference
                    if (authorityType == AuthorityType.USER)
                    {
                        AuthorityMigrationPatch.progress_logger.warn(I18NUtil.getMessage(
                                AuthorityMigrationPatch.MSG_WARNING_INVALID_ASSOC, authorityName));
                        return;
                    }
                    AuthorityMigrationPatch.this.authorityService.createAuthority(authorityType,
                            AuthorityMigrationPatch.this.authorityService.getShortName(authorityName),
                            authoritiesToCreate.get(authorityName), null);
                }
                Set<String> parentAssocsToCreate = authority.getValue();
                parentAssocsToCreate.removeAll(knownParents);
                if (!parentAssocsToCreate.isEmpty())
                {
                    try
                    {
                        AuthorityMigrationPatch.this.authorityService.addAuthority(parentAssocsToCreate, authorityName);
                    }
                    catch (UnknownAuthorityException e)
                    {
                        // Let's force a transaction retry if a parent doesn't exist. It may be because we are
                        // waiting for another worker thread to create it
                        throw new ConcurrencyFailureException("Forcing batch retry for unknown authority", e);
                    }
                    catch (InvalidNodeRefException e)
                    {
                        // Another thread may have written the node, but it is not visible to this transaction
                        // See: ALF-5471: 'authorityMigration' patch can report 'Node does not exist'
                        throw new ConcurrencyFailureException("Forcing batch retry for invalid node", e);
                    }
                }
            }
        };
        // Migrate using 2 threads, 20 authorities per transaction. Log every 100 entries.
        new BatchProcessor<Map.Entry<String, Set<String>>>(
                I18NUtil.getMessage(AuthorityMigrationPatch.MSG_PROCESS_NAME),
                transactionHelper,
                parentAssocs.entrySet(),
                2, 20,
                AuthorityMigrationPatch.this.applicationEventPublisher,
                AuthorityMigrationPatch.progress_logger, 100).process(worker, true);
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

    /**
     * TODO: The walking of the group associations should be wrapped up in a BatchProcessWorkProvider, if possible
     */
    @Override
    protected String applyInternal() throws Exception
    {
        NodeRef authorityContainer = getAuthorityContainer();
        int authorities = 0, assocs = 0;
        if (authorityContainer != null)
        {
            // Crawl the old tree of authorities
            Map<String, String> authoritiesToCreate = new TreeMap<String, String>();
            Map<String, Set<String>> parentAssocs = new TreeMap<String, Set<String>>();
            assocs = retrieveAuthorities(null, authorityContainer, authoritiesToCreate, parentAssocs);

            // Sort the group associations in parent-first order (root groups first)
            Map<String, Set<String>> sortedParentAssocs = new LinkedHashMap<String, Set<String>>(
                    parentAssocs.size() * 2);
            List<String> authorityPath = new ArrayList<String>(5);
            for (String authority : parentAssocs.keySet())
            {
                authorityPath.add(authority);
                visitGroupAssociations(authorityPath, parentAssocs, sortedParentAssocs);
                authorityPath.clear();
            }

            // Recreate the authorities and their associations in parent-first order
            migrateAuthorities(authoritiesToCreate, sortedParentAssocs);
            authorities = authoritiesToCreate.size();
        }
        // build the result message
        return I18NUtil.getMessage(AuthorityMigrationPatch.MSG_SUCCESS, authorities, assocs);
    }

    /**
     * Visits the last authority in the given list by recursively visiting its parents in associationsOld and then
     * adding the authority to associationsNew. Used to sort associationsOld into 'parent-first' order.
     * 
     * @param authorityPath
     *            The authority to visit, preceeded by all its descendants. Allows detection of cyclic child
     *            associations.
     * @param associationsOld
     *            the association map to sort
     * @param associationsNew
     *            the association map to add to in parent-first order
     */
    private static void visitGroupAssociations(List<String> authorityPath, Map<String, Set<String>> associationsOld,
            Map<String, Set<String>> associationsNew)
    {
        String authorityName = authorityPath.get(authorityPath.size() - 1);
        if (!associationsNew.containsKey(authorityName))
        {
            Set<String> associations = associationsOld.get(authorityName);

            if (!associations.isEmpty())
            {
                int insertIndex = authorityPath.size();
                for (String parentAuthority : associations)
                {
                    // Prevent cyclic paths
                    if (!authorityPath.contains(parentAuthority))
                    {
                        authorityPath.add(parentAuthority);
                        visitGroupAssociations(authorityPath, associationsOld, associationsNew);
                        authorityPath.remove(insertIndex);
                    }
                }
            }

            associationsNew.put(authorityName, associations);
        }
    }

}