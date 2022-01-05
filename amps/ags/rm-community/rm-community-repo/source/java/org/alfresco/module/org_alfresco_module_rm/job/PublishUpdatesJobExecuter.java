/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.job;

import java.util.Date;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.job.publish.PublishExecutor;
import org.alfresco.module.org_alfresco_module_rm.job.publish.PublishExecutorRegistry;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.QueryConsistency;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Job to publish any pending updates on marked node references.
 *
 * @author Roy Wetherall
 */
public class PublishUpdatesJobExecuter extends RecordsManagementJobExecuter
{
    /** Logger */
    private static Log logger = LogFactory.getLog(PublishUpdatesJobExecuter.class);

    /** Node service */
    private NodeService nodeService;

    /** Search service */
    private SearchService searchService;

    /** Publish executor register */
    private PublishExecutorRegistry publishExecutorRegistry;

    /** Dictionary service */
    private DictionaryService dictionaryService;

    /** Behaviour filter */
    private BehaviourFilter behaviourFilter;

    /**
     * @param nodeService   node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param searchService search service
     */
    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    /**
     * @param publishExecutorRegistry   public executor registry
     */
    public void setPublishExecutorRegistry(PublishExecutorRegistry publishExecutorRegistry)
    {
        this.publishExecutorRegistry = publishExecutorRegistry;
    }

    /**
     * @param behaviourFilter   behaviour filter
     */
    public void setBehaviourFilter(BehaviourFilter behaviourFilter)
    {
        this.behaviourFilter = behaviourFilter;
    }

    /**
     * @param dictionaryService dictionary service
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.job.RecordsManagementJobExecuter#executeImpl()
     */
    public void executeImpl()
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Job Starting");
        }

        AuthenticationUtil.runAs(new RunAsWork<Object>()
        {
            public Object doWork()
            {
                if (rmLoaded())
                {
                    // Get a list of the nodes that have updates that need to be published
                    List<NodeRef> nodeRefs = getUpdatedNodes();

                    // Deal with each updated disposition action in turn
                    for (NodeRef nodeRef : nodeRefs)
                    {
                        if (nodeService.exists(nodeRef))
                        {
                            boolean publishing = ((Boolean)nodeService.getProperty(nodeRef, PROP_PUBLISH_IN_PROGRESS)).booleanValue();
                            if (!publishing)
                            {
                                // Mark the update node as publishing in progress
                                markPublishInProgress(nodeRef);
                                try
                                {
                                    Date start = new Date();
                                    if (logger.isDebugEnabled())
                                    {
                                        logger.debug("Starting publish of updates ...");
                                        logger.debug("   - for " + nodeRef.toString());
                                        logger.debug("   - at " + start.toString());
                                    }

                                    // Publish updates
                                    publishUpdates(nodeRef);


                                    if (logger.isDebugEnabled())
                                    {
                                        Date end = new Date();
                                        long duration = end.getTime() - start.getTime();
                                        logger.debug("Completed publish of updates ...");
                                        logger.debug("   - for " + nodeRef.toString());
                                        logger.debug("   - at " + end.toString());
                                        logger.debug("   - duration " + Long.toString(duration));
                                    }
                                }
                                finally
                                {
                                    // Ensure the update node has either completed the publish or is marked as no longer in progress
                                    unmarkPublishInProgress(nodeRef);
                                }
                            }
                        }
                    }
                }
                return null;
            }
        }, AuthenticationUtil.getSystemUserName());

        if (logger.isDebugEnabled())
        {
            logger.debug("Job Finished");
        }
    }

    /**
     * Helper method to determine whether the RM content model has been loaded yet.
     *
     * @return  boolean true if RM content model loaded, false otherwise
     */
    private boolean rmLoaded()
    {
        boolean result = false;

        // ensure that the rm content model has been loaded
        if (dictionaryService != null &&
            dictionaryService.getAspect(ASPECT_UNPUBLISHED_UPDATE) != null)
        {
            result = true;
        }

        return result;
    }

    /**
     * Get a list of the nodes with updates pending publish
     * @return  List<NodeRef>   list of node refences with updates pending publication
     */
    private List<NodeRef> getUpdatedNodes()
    {
        RetryingTransactionCallback<List<NodeRef>> execution =
            new RetryingTransactionHelper.RetryingTransactionCallback<List<NodeRef>>()
            {
                @Override
                public List<NodeRef> execute()
                {
                    // Build the query string
                    StringBuilder sb = new StringBuilder();
                    sb.append("ASPECT:\"rma:").append(ASPECT_UNPUBLISHED_UPDATE.getLocalName()).append("\"");
                    String query = sb.toString();

                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Executing query " + query);
                    }

                    // Execute query to find updates awaiting publishing
                    List<NodeRef> resultNodes = null;

                    SearchParameters searchParameters = new SearchParameters();
                    searchParameters.setQuery(query);
                    searchParameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
                    searchParameters.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);

                    try
                    {
                        ResultSet results = searchService.query(searchParameters);
                    try
                    {
                        resultNodes = results.getNodeRefs();
                    }
                    finally
                    {
                        results.close();
                    }
                    }
                    catch (AlfrescoRuntimeException exception)
                    {
                        if (logger.isDebugEnabled())
                        {
                            logger.debug("Error executing query, " + exception.getMessage());
                        }
                        throw exception;
                    }

                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Found " + resultNodes.size() + " disposition action definitions updates awaiting publishing.");
                    }

                    return resultNodes;
                }
            };
        return retryingTransactionHelper.doInTransaction(execution, true);
    }

    /**
     * Mark the node as publish in progress.  This is often used as a marker to prevent any further updates
     * to a node.
     * @param nodeRef   node reference
     */
    private void markPublishInProgress(final NodeRef nodeRef)
    {
        RetryingTransactionHelper.RetryingTransactionCallback<Void> execution =
            new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
            {
                @Override
                public Void execute()
                {
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Marking updated node as publish in progress. (node=" + nodeRef.toString() + ")");
                    }

                    behaviourFilter.disableBehaviour(nodeRef, TYPE_DISPOSITION_ACTION_DEFINITION);
                    try
                    {
                        if (nodeService.exists(nodeRef))
                        {
                            // Mark the node as publish in progress
                            nodeService.setProperty(nodeRef, PROP_PUBLISH_IN_PROGRESS, true);
                        }
                    }
                    finally
                    {
                        behaviourFilter.enableBehaviour(nodeRef, TYPE_DISPOSITION_ACTION_DEFINITION);
                    }
                    return null;
                }
            };
        retryingTransactionHelper.doInTransaction(execution);
    }

    /**
     * Publish the updates made to the node.
     * @param nodeRef   node reference
     */
    private void publishUpdates(final NodeRef nodeRef)
    {
        RetryingTransactionHelper.RetryingTransactionCallback<Void> execution =
            new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
            {
                @Override
                public Void execute()
                {
                    behaviourFilter.disableBehaviour(nodeRef, TYPE_DISPOSITION_ACTION_DEFINITION);
                    try
                    {
                        // Get the update to value for the node
                        String updateTo = (String)nodeService.getProperty(nodeRef, PROP_UPDATE_TO);

                        if (updateTo != null)
                        {
                            if (logger.isDebugEnabled())
                            {
                                logger.debug("Node update to " +  updateTo + " (noderef=" + nodeRef.toString() + ")");
                            }

                            // Get the publish executor
                            PublishExecutor executor = publishExecutorRegistry.get(updateTo);
                            if (executor == null)
                            {
                                if (logger.isDebugEnabled())
                                {
                                    logger.debug("Unable to find a corresponding publish executor. (noderef=" + nodeRef.toString() + ", updateTo=" + updateTo + ")");
                                }
                                throw new AlfrescoRuntimeException("Unable to find a corresponding publish executor. (noderef=" + nodeRef.toString() + ", updateTo=" + updateTo + ")");
                            }

                            if (logger.isDebugEnabled())
                            {
                                logger.debug("Attempting to publish updates. (nodeRef=" + nodeRef.toString() + ")");
                            }

                            // Publish
                            executor.publish(nodeRef);
                        }
                        else
                        {
                            if (logger.isDebugEnabled())
                            {
                                logger.debug("Unable to publish, because publish executor is not set.");
                            }
                        }

                        // Remove the unpublished update aspect
                        nodeService.removeAspect(nodeRef, ASPECT_UNPUBLISHED_UPDATE);

                        if (logger.isDebugEnabled())
                        {
                            logger.debug("Publish updates complete. (nodeRef=" + nodeRef.toString() + ")");
                        }
                    }
                    finally
                    {
                        behaviourFilter.enableBehaviour(nodeRef, TYPE_DISPOSITION_ACTION_DEFINITION);
                    }

                    return null;
                }
            };
        retryingTransactionHelper.doInTransaction(execution);
    }

    /**
     * Unmark node as publish in progress, assuming publish failed.
     * @param nodeRef   node reference
     */
    private void unmarkPublishInProgress(final NodeRef nodeRef)
    {
        RetryingTransactionHelper.RetryingTransactionCallback<Void> execution =
            new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
            {
                @Override
                public Void execute()
                {
                    behaviourFilter.disableBehaviour(nodeRef, TYPE_DISPOSITION_ACTION_DEFINITION);
                    try
                    {
                        // Assuming the node still has unpublished information, then unmark it in progress
                        if (nodeService.exists(nodeRef) &&
                            nodeService.hasAspect(nodeRef, ASPECT_UNPUBLISHED_UPDATE))
                        {
                            if (logger.isDebugEnabled())
                            {
                                logger.debug("Removing publish in progress marker from updated node, because update was not successful. (node=" + nodeRef.toString() + ")");
                            }

                            nodeService.setProperty(nodeRef, PROP_PUBLISH_IN_PROGRESS, false);
                        }
                    }
                    finally
                    {
                        behaviourFilter.enableBehaviour(nodeRef, TYPE_DISPOSITION_ACTION_DEFINITION);
                    }

                    return null;
                }
            };
        retryingTransactionHelper.doInTransaction(execution);
    }
}
