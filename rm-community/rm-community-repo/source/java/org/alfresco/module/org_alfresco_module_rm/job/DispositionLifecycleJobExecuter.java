/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
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

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.action.RMDispositionActionExecuterAbstractBase;
import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PersonService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The Disposition Lifecycle Job Finds all disposition action nodes which are for disposition actions specified Where
 * asOf  &gt; now OR dispositionEventsEligible = true; Runs the cut off or retain action for eligible records.
 *
 * @author mrogers
 * @author Roy Wetherall
 */
public class DispositionLifecycleJobExecuter extends RecordsManagementJobExecuter
{
    /** logger */
    private static Log logger = LogFactory.getLog(DispositionLifecycleJobExecuter.class);

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

    /**
     * List of disposition actions to automatically execute when eligible.
     *
     * @param dispositionActions disposition actions
     */
    public void setDispositionActions(List<String> dispositionActions)
    {
        this.dispositionActions = dispositionActions;
    }

    /**
     * @param recordsManagementActionService records management action service
     */
    public void setRecordsManagementActionService(RecordsManagementActionService recordsManagementActionService)
    {
        this.recordsManagementActionService = recordsManagementActionService;
    }

    /**
     * @param nodeService node service
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

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.job.RecordsManagementJobExecuter#execute()
     */
    public void executeImpl()
    {
        try
        {
            logger.debug("Job Starting");

            if (dispositionActions != null && !dispositionActions.isEmpty())
            {
                boolean hasMore = true;
                int skipCount = 0;
                while(hasMore)
                {
                    SearchParameters params = new SearchParameters();
                    params.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
                    params.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
                    params.setQuery(getQuery());
                    params.setSkipCount(skipCount);

                    // execute search
                    ResultSet results = searchService.query(params);
                    List<NodeRef> resultNodes = results.getNodeRefs();
                    hasMore = results.hasMore();
                    skipCount += resultNodes.size(); // increase by page size
                    results.close();

                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Processing " + resultNodes.size() + " nodes");
                    }

                    // process search results
                    for (NodeRef node : resultNodes)
                    {
                        executeAction(node);
                    }
                }
            }

            logger.debug("Job Finished");
        }
        catch (AlfrescoRuntimeException exception)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug(exception);
            }
        }
    }

    /**
     * Helper method that executes a disposition action
     *
     * @param actionNode - the disposition action to execute
     */
    private void executeAction(final NodeRef actionNode)
    {
        RetryingTransactionCallback<Boolean> processTranCB = new RetryingTransactionCallback<Boolean>()
        {
            public Boolean execute()
            {
                final String dispAction = (String) nodeService.getProperty(actionNode,
                            RecordsManagementModel.PROP_DISPOSITION_ACTION);

                // Run disposition action
                if (dispAction != null && dispositionActions.contains(dispAction))
                {
                    ChildAssociationRef parent = nodeService.getPrimaryParent(actionNode);
                    if (parent.getTypeQName().equals(RecordsManagementModel.ASSOC_NEXT_DISPOSITION_ACTION))
                    {
                        Map<String, Serializable> props = new HashMap<>(1);
                        props.put(RMDispositionActionExecuterAbstractBase.PARAM_NO_ERROR_CHECK,
                                    Boolean.FALSE);

                        try
                        {
                            // execute disposition action
                            recordsManagementActionService.executeRecordsManagementAction(
                                        parent.getParentRef(), dispAction, props);

                            if (logger.isDebugEnabled())
                            {
                                logger.debug("Processed action: " + dispAction + "on" + parent);
                            }
                        }
                        catch (AlfrescoRuntimeException exception)
                        {
                            if (logger.isDebugEnabled())
                            {
                                logger.debug(exception);
                            }
                        }
                    }
                }

                return Boolean.TRUE;
            }
        };

        // if exists
        if (nodeService.exists(actionNode))
        {
            retryingTransactionHelper.doInTransaction(processTranCB);
        }
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
