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
package org.alfresco.repo.tagging;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.lock.JobLockService;
import org.alfresco.repo.lock.LockAcquisitionException;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.audit.AuditQueryParameters;
import org.alfresco.service.cmr.audit.AuditService;
import org.alfresco.service.cmr.audit.AuditService.AuditQueryCallback;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.tagging.TagDetails;
import org.alfresco.service.cmr.tagging.TaggingService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;

/**
 * Update tag scopes action executer.
 * 
 * NOTE: This action is used to facilitate the async update of tag scopes. It is not intended for general usage.
 * 
 * @author Roy Wetherall
 */
public class UpdateTagScopesActionExecuter extends ActionExecuterAbstractBase
{
    private static final Log logger = LogFactory.getLog(UpdateTagScopesActionExecuter.class);

    /** Node Service */
    private NodeService nodeService;

    /** Content Service */
    private ContentService contentService;

    /** Tagging Service */
    private TaggingService taggingService;

    /** Audit Service, used to get the list of pending changes */
    private AuditService auditService;

    /** Job Lock Service, used to avoid contention on tag scope nodes */
    private JobLockService jobLockService;

    /** Transaction Service, used for retrying operations */
    private TransactionService transactionService;

    /** Used to disable policies/behaviours when changing tag scope properties */
    private BehaviourFilter behaviourFilter;

    /** Action name and parameters */
    public static final String NAME = "update-tagscope";
    public static final String PARAM_TAG_SCOPES = "tag_scopes";

    /** What's the largest number of updates we should claim for a tag scope in one transaction? */
    private static final int tagUpdateBatchSize = 100;

    /** How long to lock a tag scope for */
    private static final int tagScopeLockTime = 2500;

    // For searching
    private static final String noderefPath = TaggingServiceImpl.TAGGING_AUDIT_ROOT_PATH + "/" +
            TaggingServiceImpl.TAGGING_AUDIT_KEY_NODEREF + "/value";
    private static final String tagsPath = TaggingServiceImpl.TAGGING_AUDIT_ROOT_PATH + "/" +
            TaggingServiceImpl.TAGGING_AUDIT_KEY_TAGS + "/value";

    /**
     * Set the node service
     * 
     * @param nodeService
     *            node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * Set the content service
     * 
     * @param contentService
     *            the content service
     */
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    /**
     * Set the tagging service
     * 
     * @param taggingService
     *            the tagging service
     */
    public void setTaggingService(TaggingService taggingService)
    {
        this.taggingService = taggingService;
    }

    /**
     * Set the audit service
     * 
     * @param auditService
     *            the audit service
     */
    public void setAuditService(AuditService auditService)
    {
        this.auditService = auditService;
    }

    /**
     * Set the job lock service
     * 
     * @param jobLockService
     *            the job locking service
     */
    public void setJobLockService(JobLockService jobLockService)
    {
        this.jobLockService = jobLockService;
    }

    /**
     * Set the transaction service
     * 
     * @param transactionService
     *            the transaction service
     */
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    /**
     * Set the behaviour filter
     * 
     * @param behaviourFilter
     *            the behaviour filter
     */
    public void setBehaviourFilter(BehaviourFilter behaviourFilter)
    {
        this.behaviourFilter = behaviourFilter;
    }

    /**
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    @SuppressWarnings("unchecked")
    protected void executeImpl(final Action action, final NodeRef actionedUponNodeRef)
    {
        try
        {
            // Grab the list of tag scopes to work on
            List<NodeRef> tagScopeNodes = (List<NodeRef>) action.getParameterValue(PARAM_TAG_SCOPES);
            if (logger.isDebugEnabled())
            {
                logger.debug("About to process tag scope updates for scopes " + tagScopeNodes);
            }

            // Process each tag scope in turn
            for (NodeRef tmpTagScope : tagScopeNodes)
            {
                final NodeRef tagScope = tmpTagScope;

                // Try for an exclusive tagging lock on the tag scope
                // If someone else already has the lock, then we don't need
                // to worry as they'll handle the update for us!
                try
                {
                    String lock = lockTagScope(tagScope);

                    // If we got here, we're the only thread currently
                    // processing this tag scope
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Locked tag scope " + tagScope + " for updates");
                    }

                    // Grab all the pending work in chunks, and process
                    AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>() {
                        public Void doWork() throws Exception
                        {
                            final MutableInt updatesRemain = new MutableInt(1);
                            while (updatesRemain.intValue() > 0)
                            {
                                transactionService.getRetryingTransactionHelper().doInTransaction(
                                        new RetryingTransactionCallback<Void>() {
                                            public Void execute() throws Throwable
                                            {
                                                // Search for updates
                                                Map<String, Integer> updates = new HashMap<String, Integer>();
                                                List<Long> entryIds = searchForUpdates(tagScope, updates);

                                                // Log what we found
                                                if (logger.isDebugEnabled())
                                                {
                                                    if (updates.size() > 0)
                                                    {
                                                        logger.debug("Found updates for tag scope " + tagScope + " : " + updates);
                                                    }
                                                    else if (updatesRemain.intValue() > 1)
                                                    {
                                                        logger.debug("All updates now processed for tag scope " + tagScope);
                                                    }
                                                    else
                                                    {
                                                        logger.debug("No updates needed for tag scope " + tagScope);
                                                    }
                                                }

                                                // Does any work remain?
                                                if (entryIds.size() == 0)
                                                {
                                                    updatesRemain.setValue(0);
                                                    return null;
                                                }
                                                updatesRemain.setValue(updatesRemain.intValue() + 1);

                                                // Update the tags
                                                performUpdates(tagScope, updates);

                                                // Mark these entries as finished with
                                                markUpdatesPerformed(entryIds);

                                                // Done for now
                                                return null;
                                            }
                                        }, false, true);
                            }

                            // We're done searching+updating for this tag scope
                            return null;
                        }
                    }, AuthenticationUtil.getSystemUserName());

                    // We're done with this tag scope
                    unlockTagScope(tagScope, lock);
                }
                catch (LockAcquisitionException e)
                {
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Tag scope " + tagScope + " is already being processed by another action, skipping");
                    }
                }

                // Now proceed to the next tag scope
            }
        }
        catch (RuntimeException exception)
        {
            exception.printStackTrace();
            throw new RuntimeException("Unable to update the tag scopes.", exception);
        }
    }

    /**
     * For the given tag scope node, which should have been locked, ask the Audit Service for work that needs to be done on it. Should fetch up to one chunk of work, and should coalesce multiple updates of one tag into a single change.
     */
    private List<Long> searchForUpdates(final NodeRef tagScopeNode, final Map<String, Integer> updates)
    {
        // Build the query
        final AuditQueryParameters params = new AuditQueryParameters();
        params.setApplicationName(TaggingServiceImpl.TAGGING_AUDIT_APPLICATION_NAME);
        params.addSearchKey(noderefPath, tagScopeNode.toString());

        // Execute the query, in a new transaction
        // (Avoid contention issues with repeated runs / updates)
        final List<Long> ids = new ArrayList<Long>();
        auditService.auditQuery(new AuditQueryCallback() {
            @Override
            public boolean valuesRequired()
            {
                return true;
            }

            @Override
            public boolean handleAuditEntryError(Long entryId, String errorMsg,
                    Throwable error)
            {
                logger.warn("Error fetching tagging update entry - " + errorMsg, error);
                // Keep trying
                return true;
            }

            @Override
            @SuppressWarnings("unchecked")
            public boolean handleAuditEntry(Long entryId, String applicationName,
                    String user, long time, Map<String, Serializable> values)
            {
                // Save the ID
                ids.add(entryId);

                // Save the updates list
                if (values.containsKey(tagsPath))
                {
                    // Process each tag in turn from the change list
                    HashMap<String, Integer> changes = (HashMap<String, Integer>) values.get(tagsPath);
                    for (String tagName : changes.keySet())
                    {
                        // Merge various updates to the same count together
                        int count = 0;
                        if (updates.containsKey(tagName))
                        {
                            count = updates.get(tagName);
                        }
                        count += changes.get(tagName);

                        updates.put(tagName, count);
                    }
                }
                else
                {
                    logger.warn("Unexpected Tag Scope update entry of " + values);
                }

                // Next entry please!
                return true;
            }
        }, params, tagUpdateBatchSize);

        // Return the ids
        return ids;
    }

    /**
     * Mark the audit service entries as handled
     */
    private void markUpdatesPerformed(List<Long> ids)
    {
        auditService.clearAudit(ids);
    }

    /**
     * For the given tag scope node, which should have been locked, update the tag scope cache with the given updates.
     */
    private void performUpdates(NodeRef tagScopeNode, Map<String, Integer> updates)
    {
        if (nodeService.exists(tagScopeNode))
        {
            List<TagDetails> tags = null;

            // Changing the tag scope values is a system operation
            // As such, don't fire policies/behaviours during this
            behaviourFilter.disableBehaviour();

            // Get the current tags
            ContentReader contentReader = contentService.getReader(tagScopeNode, ContentModel.PROP_TAGSCOPE_CACHE);
            if (contentReader == null)
            {
                tags = new ArrayList<TagDetails>(1);
            }
            else
            {
                tags = TaggingServiceImpl.readTagDetails(contentReader.getContentInputStream());
            }
            String previousTagState = tags.toString();

            // Figure out what changes to make
            for (String tagName : updates.keySet())
            {
                int change = updates.get(tagName);
                if (change == 0)
                    continue;

                // Get the current count for this tag, if we can
                TagDetailsImpl currentTag = null;
                for (TagDetails tag : tags)
                {
                    if (tag.getName().equals(tagName) == true)
                    {
                        currentTag = (TagDetailsImpl) tag;
                        break;
                    }
                }

                // If we're incrementing, increase our counts
                if (change > 0)
                {
                    if (currentTag == null)
                    {
                        currentTag = new TagDetailsImpl(tagName, 0);
                        tags.add(currentTag);
                    }

                    for (int i = 0; i < change; i++)
                        currentTag.incrementCount();
                }
                else
                {
                    if (currentTag != null)
                    {
                        for (int i = change; i < 0; i++)
                        {
                            currentTag.decrementCount();
                        }

                        if (currentTag.getCount() <= 0)
                        {
                            // All gone
                            tags.remove(currentTag);
                        }
                    }
                }
            }

            // ACE-1979: emptying tag scope cache by setting content property for the cache to null to avoid zero-size writes. Orphaned content will be deleted with content store
            // cleaner job
            if (tags.isEmpty())
            {
                nodeService.removeProperty(tagScopeNode, ContentModel.PROP_TAGSCOPE_CACHE);

                if (logger.isDebugEnabled())
                {
                    logger.debug("Updated tag scope: '" + tagScopeNode + "'. No tags were found. Emptying tags cache by setting content property to null...");
                }
            }
            else
            {
                // Order the list
                Collections.sort(tags);

                // Write new content back to tag scope
                String tagContent = TaggingServiceImpl.tagDetailsToString(tags);
                ContentWriter contentWriter = contentService.getWriter(tagScopeNode, ContentModel.PROP_TAGSCOPE_CACHE, true);
                contentWriter.setEncoding("UTF-8");
                contentWriter.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
                contentWriter.putContent(tagContent);

                // Log this if required
                if (logger.isDebugEnabled())
                {
                    logger.debug(
                            "Updated tag scope " + tagScopeNode + " with " + updates + ", " +
                                    "new contents are { " + tagContent.replace("\n", " : ") + " } " +
                                    "from old contents of " + previousTagState);
                }
            }

            // We're done making our changes
            // Allow behaviours to fire again if they want to
            behaviourFilter.enableBehaviour();
        }
    }

    /**
     * Checks several batches of updates in the Audit event log, and returns the list of Tag Scope Node References found there. You should generally call this action to process the list of tag nodes before re-calling this method. You may need to call this method several times if lots of work is backed up, when an empty list is returned then you know that all work is handled.
     */
    public List<NodeRef> searchForTagScopesPendingUpdates()
    {
        final Set<String> tagNodesStrs = new HashSet<String>();

        // We want all entries for tagging
        final AuditQueryParameters params = new AuditQueryParameters();
        params.setApplicationName(TaggingServiceImpl.TAGGING_AUDIT_APPLICATION_NAME);

        // Execute the query, in a new transaction
        // (Avoid contention issues with repeated runs / updates)
        transactionService.getRetryingTransactionHelper().doInTransaction(
                new RetryingTransactionCallback<Void>() {
                    public Void execute() throws Throwable
                    {
                        auditService.auditQuery(new AuditQueryCallback() {
                            @Override
                            public boolean valuesRequired()
                            {
                                return true;
                            }

                            @Override
                            public boolean handleAuditEntryError(Long entryId, String errorMsg,
                                    Throwable error)
                            {
                                logger.warn("Error fetching tagging update entry - " + errorMsg, error);
                                // Keep trying
                                return true;
                            }

                            @Override
                            public boolean handleAuditEntry(Long entryId, String applicationName,
                                    String user, long time, Map<String, Serializable> values)
                            {
                                // Save the NodeRef
                                if (values.containsKey(noderefPath))
                                {
                                    String nodeRefStr = (String) values.get(noderefPath);
                                    if (!tagNodesStrs.contains(nodeRefStr))
                                        tagNodesStrs.add(nodeRefStr);
                                }
                                else
                                {
                                    logger.warn("Unexpected Tag Scope update entry of " + values);
                                }

                                // Next entry please!
                                return true;
                            }
                        }, params, 4 * tagUpdateBatchSize);
                        return null;
                    }
                }, false, true);

        // Turn it into NodeRefs
        List<NodeRef> tagNodes = new ArrayList<NodeRef>();
        for (String nodeRefStr : tagNodesStrs)
        {
            tagNodes.add(new NodeRef(nodeRefStr));
        }
        return tagNodes;
    }

    private QName tagScopeToLockQName(NodeRef tagScope)
    {
        QName lockQName = QName.createQName("TagScope_" + tagScope.toString());
        return lockQName;
    }

    protected String lockTagScope(NodeRef tagScope)
    {
        String lock = jobLockService.getLock(
                tagScopeToLockQName(tagScope), tagScopeLockTime, 0, 0);
        return lock;
    }

    protected void updateTagScopeLock(NodeRef tagScope, String lockToken)
    {
        jobLockService.refreshLock(lockToken, tagScopeToLockQName(tagScope), tagScopeLockTime);
    }

    protected void unlockTagScope(NodeRef tagScope, String lockToken)
    {
        jobLockService.releaseLock(lockToken, tagScopeToLockQName(tagScope));
    }

    /**
     * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#addParameterDefinitions(java.util.List)
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
        paramList.add(new ParameterDefinitionImpl(PARAM_TAG_SCOPES, DataTypeDefinition.ANY, true, getParamDisplayLabel(PARAM_TAG_SCOPES)));
    }
}
