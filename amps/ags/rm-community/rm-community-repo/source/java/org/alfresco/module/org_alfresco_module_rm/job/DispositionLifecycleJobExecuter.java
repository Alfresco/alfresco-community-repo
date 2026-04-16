/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
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

import static org.alfresco.module.org_alfresco_module_rm.action.RMDispositionActionExecuterAbstractBase.PARAM_NO_ERROR_CHECK;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionService;
import org.alfresco.module.org_alfresco_module_rm.freeze.FreezeService;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.QueryConsistency;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.util.DateUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The Disposition Lifecycle Job Finds all disposition action nodes which are for disposition actions specified Where asOf &gt; now OR dispositionEventsEligible = true; Runs the cut off or retain action for eligible records.
 *
 * @author mrogers
 * @author Roy Wetherall
 */
@Slf4j
public class DispositionLifecycleJobExecuter extends RecordsManagementJobExecuter
{

    /** batching properties */
    private int batchSize;
    public static final int DEFAULT_BATCH_SIZE = 500;

    /** list of disposition actions to automatically execute */
    private List<String> dispositionActions;

    /** query string */
    private String query;

    /** records management action service */
    private RecordsManagementActionService recordsManagementActionService;

    /** node service */
    private NodeService nodeService;

    /** search service */
    private SearchService searchService;

    /** person service */
    private PersonService personService;

    /** freeze service */
    private FreezeService freezeService;

    /** when true, query conditions are pushed into ES filter context (unscored, cacheable) for better performance */
    private boolean enableElasticOptimization = false;

    /**
     * @param freezeService
     *            freeze service
     */
    public void setFreezeService(FreezeService freezeService)
    {
        this.freezeService = freezeService;
    }

    public void setEnableElasticOptimization(boolean enableElasticOptimization)
    {
        this.enableElasticOptimization = enableElasticOptimization;
    }

    /**
     * List of disposition actions to automatically execute when eligible.
     *
     * @param dispositionActions
     *            disposition actions
     */
    public void setDispositionActions(List<String> dispositionActions)
    {
        this.dispositionActions = dispositionActions;
    }

    public void setBatchSize(int batchSize)
    {
        this.batchSize = batchSize;
    }

    /**
     * @param recordsManagementActionService
     *            records management action service
     */
    public void setRecordsManagementActionService(RecordsManagementActionService recordsManagementActionService)
    {
        this.recordsManagementActionService = recordsManagementActionService;
    }

    /**
     * @param nodeService
     *            node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param searchService
     *            search service
     */
    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    /**
     * Get the search query string.
     *
     * @return job query string
     */
    protected String getQuery()
    {
        if (query == null)
        {
            StringBuilder sb = new StringBuilder();

            sb.append("TYPE:\"rma:dispositionAction\" AND ");
            sb.append("(@rma\\:dispositionAction:(");

            boolean bFirst = true;
            for (String dispositionAction : dispositionActions)
            {
                if (bFirst)
                {
                    bFirst = false;
                }
                else
                {
                    sb.append(" OR ");
                }

                sb.append("\"").append(dispositionAction).append("\"");
            }

            sb.append("))");
            sb.append(" AND ISUNSET:\"rma:dispositionActionCompletedAt\" ");
            sb.append(" AND ( ");
            sb.append("@rma\\:dispositionEventsEligible:true ");
            sb.append("OR @rma\\:dispositionAsOf:[MIN TO NOW] ");
            sb.append(") ");

            query = sb.toString();
        }

        return query;
    }

    private String getActionFilterQuery()
    {
        String actionFilterQuery=null;
        StringBuilder sb = new StringBuilder("@rma\\:dispositionAction:(");
        boolean first = true;
        for (String dispositionAction : dispositionActions)
        {
            if (!first) sb.append(" OR ");
            sb.append("\"").append(dispositionAction).append("\"");
            first = false;
        }
        actionFilterQuery = sb.append(")").toString();

        return actionFilterQuery;
    }


    /**
     * @see org.alfresco.module.org_alfresco_module_rm.job.RecordsManagementJobExecuter#execute()
     */
    @Override
    public void executeImpl()
    {
        try
        {
            log.debug("Job Starting");

            if (dispositionActions == null || dispositionActions.isEmpty())
            {
                log.debug("Job Finished as disposition action is empty");
                return;
            }

            boolean hasMore = true;
            int skipCount = 0;

            if (batchSize < 1)
            {
                log.debug("Invalid value for batch size: " + batchSize + " default value used instead.");
                batchSize = DEFAULT_BATCH_SIZE;
            }

            log.trace("Using batch size of " + batchSize);

            while (hasMore)
            {
                SearchParameters params = new SearchParameters();
                params.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
                params.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);

                if (enableElasticOptimization)
                {
                    params.setQuery("TYPE:\"rma:dispositionAction\"");
                    params.addFilterQuery(getActionFilterQuery());
                    params.addFilterQuery("ISUNSET:\"rma:dispositionActionCompletedAt\"");
                    String tomorrow = LocalDate.now().plusDays(1).toString();
                    params.addFilterQuery("(@rma\\:dispositionEventsEligible:true OR -@rma\\:dispositionAsOf:[" + tomorrow + " TO MAX])");
                }
                else
                {
                    // Original combined FTS query — safe default, works with Solr and DB fallback.
                    params.setQuery(getQuery());
                }

                params.setSkipCount(skipCount);
                params.setMaxItems(batchSize);

                // execute search
                ResultSet results = searchService.query(params);
                if (results == null)
                {
                    log.warn("Disposition lifecycle search returned null; stopping pagination.");
                    break;
                }
                // Declared outside try so it remains in scope after results.close().
                Map<NodeRef, ChildAssociationRef> eligibleNodes;
                try
                {
                    List<NodeRef> rawPage = results.getNodeRefs();
                    // Advance skip by raw hit count so paging stays aligned with the index; post-search
                    // freeze filtering must not shrink the skip step (would duplicate or skip hits).
                    int rawPageLength = rawPage.size();
                    hasMore = results.hasMore();
                    skipCount += rawPageLength;

                    // Single getPrimaryParent call per node: result is carried forward so
                    // executeAction can reuse it without a second DB round-trip.
                    eligibleNodes = new LinkedHashMap<>(rawPageLength);
                    for (NodeRef node : rawPage)
                    {
                        ChildAssociationRef parent = nodeService.getPrimaryParent(node);
                        NodeRef freezeTarget = parent != null ? parent.getParentRef() : node;
                        if (!freezeService.isFrozenOrHasFrozenChildren(freezeTarget))
                        {
                            eligibleNodes.put(node, parent);
                        }
                    }
                }
                finally
                {
                    results.close();
                }

                log.debug("Processing " + eligibleNodes.size() + " nodes");

                // process search results
                if (!eligibleNodes.isEmpty())
                {
                    executeAction(eligibleNodes);
                }
            }
            log.debug("Job Finished");
        }
        catch (AlfrescoRuntimeException exception)
        {
            log.debug(exception.getMessage());
        }
    }

    /**
     * Helper method that executes a disposition action
     *
     * @param eligibleNodes
     *            map of disposition action node to its pre-computed primary parent;
     */
    private void executeAction(final Map<NodeRef, ChildAssociationRef> eligibleNodes)
    {
        RetryingTransactionCallback<Boolean> processTranCB = () -> {
            for (Map.Entry<NodeRef, ChildAssociationRef> entry : eligibleNodes.entrySet())
            {
                NodeRef actionNode = entry.getKey();
                // Reuse the parent computed during the freeze-filter pass — no extra DB call.
                ChildAssociationRef parent = entry.getValue();

                if (!nodeService.exists(actionNode))
                {
                    continue;
                }

                final String dispAction = (String) nodeService.getProperty(actionNode, PROP_DISPOSITION_ACTION);

                // Run disposition action
                if (dispAction == null || !dispositionActions.contains(dispAction))
                {
                    continue;
                }

                if (parent == null || !parent.getTypeQName().equals(ASSOC_NEXT_DISPOSITION_ACTION))
                {
                    continue;
                }
                Map<String, Serializable> props = Map.of(PARAM_NO_ERROR_CHECK, false);

                try
                {
                    // execute disposition action
                    recordsManagementActionService
                            .executeRecordsManagementAction(parent.getParentRef(), dispAction, props);

                    log.debug("Processed action: " + dispAction + "on" + parent);

                }
                catch (AlfrescoRuntimeException exception)
                {
                    log.debug(exception.getMessage());

                }
            }
            return Boolean.TRUE;
        };
        retryingTransactionHelper.doInTransaction(processTranCB, false, true);
    }

    public PersonService getPersonService()
    {
        return personService;
    }

    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }
}
