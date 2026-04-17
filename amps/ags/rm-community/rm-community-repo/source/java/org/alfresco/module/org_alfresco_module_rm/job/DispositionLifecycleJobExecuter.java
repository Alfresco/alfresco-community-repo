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
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionService;
import org.alfresco.module.org_alfresco_module_rm.freeze.FreezeService;
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
import org.apache.commons.lang3.StringUtils;

/**
 * The Disposition Lifecycle Job Finds all disposition action nodes which are for disposition actions specified Where asOf &gt; now OR dispositionEventsEligible = true; Runs the cut off or retain action for eligible records.
 *
 * @author mrogers
 * @author Roy Wetherall
 */
@Slf4j
public class DispositionLifecycleJobExecuter extends RecordsManagementJobExecuter
{

    public enum QueryMode
    {
        FTS_DEFAULT, FTS_OPTIMIZED_QUERY, FTS_OPTIMIZED_CUTOFF, CMIS;

        public static QueryMode getQueryMode(String queryMode)
        {
            if (StringUtils.isBlank(queryMode))
            {
                return FTS_DEFAULT;
            }

            String normalizedQueryMode = queryMode.trim();
            return EnumSet.allOf(QueryMode.class).stream()
                    .filter(mode -> mode.name().equalsIgnoreCase(normalizedQueryMode))
                    .findFirst()
                    .orElse(FTS_DEFAULT);
        }
    }


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

    private QueryMode queryMode;


    /**
     *
     * @param queryMode
     */
    public void setQueryMode(String queryMode) {
        this.queryMode = QueryMode.getQueryMode(queryMode);
    }


    /**
     * @param freezeService
     *            freeze service
     */
    public void setFreezeService(FreezeService freezeService)
    {
        this.freezeService = freezeService;
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
     * Builds a transactional CMIS query for eligible disposition action nodes.
     * The date cutoff is evaluated at call time so each job run uses the current date.
     * Never cached — unlike {@link #getQuery()} — because the timestamp changes every run.
     *
     * @return CMIS SQL string
     */
    protected String getCmisQuery()
    {
        // Use end-of-current-day in UTC so that records due today are always included
        // regardless of what time within the day the job fires.
        String cutoff = LocalDate.now() + "T23:59:59.999Z";

        StringBuilder sb = new StringBuilder("SELECT * FROM rma:dispositionAction WHERE ");
        sb.append("rma:dispositionAction IN (");
        boolean first = true;
        for (String action : dispositionActions)
        {
            if (!first) sb.append(",");
            sb.append("'").append(action).append("'");
            first = false;
        }
        sb.append(") ");
        sb.append("AND rma:dispositionActionCompletedAt IS NULL ");
        sb.append("AND (rma:dispositionEventsEligible = true ");
        sb.append("OR rma:dispositionAsOf <= TIMESTAMP '").append(cutoff).append("')");

        log.debug("Constructed CMIS query: " + sb.toString());
        return sb.toString();
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.job.RecordsManagementJobExecuter#execute()
     */
    @Override
    public void executeImpl()
    {
        switch (queryMode)
        {
            case CMIS:
                executeImplCmis();
                break;
            case FTS_OPTIMIZED_QUERY:
            case FTS_OPTIMIZED_CUTOFF:
            case FTS_DEFAULT:
            default:
                executeImplFts();
                break;
        }
    }

    /**
     * FTS/index-based execution path (default).
     * Paginates through search results using skipCount, relying on the search index
     * (Elasticsearch or Solr) to resolve eligible nodes.
     */
    private void executeImplFts()
    {
        try
        {
            log.debug("Job Starting (FTS mode)");

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

            int batchNumber = 0;
            int totalReturned = 0;
            int totalEligible = 0;
            int totalProcessed = 0;

            while (hasMore)
            {
                batchNumber++;

                SearchParameters params = new SearchParameters();
                params.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
                params.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
                params.setSkipCount(skipCount);
                params.setMaxItems(batchSize);
                switch (queryMode)
                {
                    case FTS_OPTIMIZED_QUERY:
                        params.setQuery("TYPE:\"rma:dispositionAction\"");
                        params.addFilterQuery(getActionFilterQuery());
                        params.addFilterQuery("ISUNSET:\"rma:dispositionActionCompletedAt\"");
                        params.addFilterQuery("(@rma\\:dispositionEventsEligible:true OR @rma\\:dispositionAsOf:[MIN TO NOW])");
                        params.setTrackScore(false);
                        break;
                    case FTS_OPTIMIZED_CUTOFF:
                        params.setQuery("TYPE:\"rma:dispositionAction\"");
                        params.addFilterQuery(getActionFilterQuery());
                        params.addFilterQuery("ISUNSET:\"rma:dispositionActionCompletedAt\"");
                        // Negated future range — semantically equivalent to [MIN TO NOW] but may
                        // produce a different ES execution plan worth benchmarking.
                        params.addFilterQuery("(@rma\\:dispositionEventsEligible:true OR -@rma\\:dispositionAsOf:["
                                + LocalDate.now().plusDays(1) + " TO MAX])");
                        params.setTrackScore(false);
                        break;
                    case FTS_DEFAULT:
                    default:
                        params.setQuery(getQuery());
                        break;
                }

                // execute search
                ResultSet results = searchService.query(params);
                if (results == null)
                {
                    log.warn("Disposition lifecycle search returned null; stopping pagination.");
                    break;
                }
                // Declared outside try so it remains in scope after results.close().
                Map<NodeRef, ChildAssociationRef> eligibleNodes;
                int rawPageLength;
                try
                {
                    List<NodeRef> rawPage = results.getNodeRefs();
                    // Advance skip by raw hit count so paging stays aligned with the index; post-search
                    // freeze filtering must not shrink the skip step (would duplicate or skip hits).
                    rawPageLength = rawPage.size();
                    hasMore = results.hasMore();
                    skipCount += rawPageLength;
                    totalReturned += rawPageLength;

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

                int batchEligible = eligibleNodes.size();
                totalEligible += batchEligible;

                log.debug("Batch {} — returned: {}, eligible (not frozen): {}, hasMore: {}",
                        batchNumber, rawPageLength, batchEligible, hasMore);

                int batchProcessed = 0;
                if (!eligibleNodes.isEmpty())
                {
                    batchProcessed = executeAction(eligibleNodes);
                }
                totalProcessed += batchProcessed;

                log.debug("Batch {} — actioned: {}", batchNumber, batchProcessed);
            }

            log.debug("Job Finished (FTS mode) — batches: {}, total returned by search: {}, total eligible: {}, total actioned: {}",
                    batchNumber, totalReturned, totalEligible, totalProcessed);
        }
        catch (AlfrescoRuntimeException exception)
        {
            log.debug(exception.getMessage());
        }
    }

    /**
     * CMIS/DB-based execution path (enabled by {@code useDbQuery=true}).
     *
     * <p>Key differences from the FTS path:</p>
     * <ul>
     *   <li><b>Transactional consistency</b>: {@link QueryConsistency#TRANSACTIONAL} is forced
     *       so the query always hits the DB. Processed nodes (with {@code dispositionActionCompletedAt}
     *       set) are immediately invisible to the next query — no risk of re-processing.</li>
     *   <li><b>Skip reset on progress</b>: When at least one node is actioned, skip resets to 0
     *       because those nodes have vanished from the result set. The next query naturally returns
     *       the next page of eligible nodes from the beginning.</li>
     *   <li><b>Skip advance on no progress</b>: When a non-empty batch yields zero actioned nodes
     *       (all frozen, deleted, or malformed) the skip advances by the raw page size. This prevents
     *       an infinite loop over unprocessable records.</li>
     *   <li><b>No index cap</b>: The 10 000-record Elasticsearch limit does not apply; the batch
     *       size is the only cap and is fully configurable.</li>
     * </ul>
     */
    private void executeImplCmis()
    {
        try
        {
            log.debug("Job Starting (CMIS/DB mode)");

            if (dispositionActions == null || dispositionActions.isEmpty())
            {
                log.debug("Job Finished as disposition action is empty");
                return;
            }

            if (batchSize < 1)
            {
                log.debug("Invalid value for batch size: " + batchSize + " default value used instead.");
                batchSize = DEFAULT_BATCH_SIZE;
            }

            log.trace("Using batch size of " + batchSize);

            int skipCount = 0;
            int batchNumber = 0;
            int totalReturned = 0;
            int totalEligible = 0;
            int totalProcessed = 0;

            while (true)
            {
                batchNumber++;

                SearchParameters params = new SearchParameters();
                params.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
                params.setLanguage(SearchService.LANGUAGE_CMIS_ALFRESCO);
                params.setQuery(getCmisQuery());
                params.setMaxItems(batchSize);
                params.setSkipCount(skipCount);
                // Force DB routing. CMIS goes to the DB by default but being explicit here
                // guards against any future routing change that could silently reintroduce
                // the async-indexing lag and the need for skip arithmetic.
                params.setQueryConsistency(QueryConsistency.TRANSACTIONAL);

                ResultSet results = searchService.query(params);
                if (results == null)
                {
                    log.warn("Disposition lifecycle CMIS query returned null; stopping.");
                    break;
                }

                int rawPageSize;
                Map<NodeRef, ChildAssociationRef> eligibleNodes;
                try
                {
                    List<NodeRef> rawPage = results.getNodeRefs();
                    rawPageSize = rawPage.size();
                    if (rawPageSize == 0)
                    {
                        log.debug("No more eligible nodes found; job complete.");
                        break;
                    }
                    totalReturned += rawPageSize;

                    eligibleNodes = new LinkedHashMap<>(rawPageSize);
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

                int batchEligible = eligibleNodes.size();
                totalEligible += batchEligible;

                log.debug("Batch {} — returned: {}, eligible (not frozen): {}, skip: {}",
                        batchNumber, rawPageSize, batchEligible, skipCount);

                int processed = 0;
                if (!eligibleNodes.isEmpty())
                {
                    processed = executeAction(eligibleNodes);
                }
                totalProcessed += processed;

                log.debug("Batch {} — actioned: {}", batchNumber, processed);

                if (processed > 0)
                {
                    // Actioned nodes have dispositionActionCompletedAt set and will not reappear.
                    // Reset skip so the next query starts from position 0 and picks up any nodes
                    // that shifted into earlier positions as processed ones were removed.
                    skipCount = 0;
                }
                else
                {
                    // The entire batch was unprocessable (frozen, non-existent, or malformed data).
                    // Advance skip past these nodes to avoid fetching the same stuck batch forever.
                    log.warn("No nodes processed from a batch of {}; advancing skip by {} to bypass unprocessable records.", rawPageSize, rawPageSize);
                    skipCount += rawPageSize;
                }
            }
            log.debug("Job Finished (CMIS/DB mode) — batches: {}, total returned by query: {}, total eligible: {}, total actioned: {}",
                    batchNumber, totalReturned, totalEligible, totalProcessed);
        }
        catch (AlfrescoRuntimeException exception)
        {
            log.debug(exception.getMessage());
        }
    }

    /**
     * Executes the disposition action for each eligible node.
     *
     * <p>Returns the number of nodes for which the action was actually executed.
     * Nodes discarded by the three internal guards (does not exist, wrong action,
     * wrong parent association) are not counted. The caller uses this count to
     * detect a stuck batch — a non-empty batch that yields zero processed nodes —
     * and advance the skip offset accordingly.</p>
     *
     * @param eligibleNodes map of disposition action node to its pre-computed primary parent
     * @return number of nodes for which the disposition action was successfully invoked
     */
    private int executeAction(final Map<NodeRef, ChildAssociationRef> eligibleNodes)
    {
        // int[] used so the lambda can mutate the counter.
        final int[] processedCount = {0};

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
                    recordsManagementActionService
                            .executeRecordsManagementAction(parent.getParentRef(), dispAction, props);
                    processedCount[0]++;
                    log.trace("Processed action: {} on {}", dispAction, parent);
                }
                catch (AlfrescoRuntimeException exception)
                {
                    log.debug(exception.getMessage());
                }
            }
            return Boolean.TRUE;
        };
        retryingTransactionHelper.doInTransaction(processTranCB, false, true);
        return processedCount[0];
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
