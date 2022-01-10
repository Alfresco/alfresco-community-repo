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

package org.alfresco.module.org_alfresco_module_rm.action;

import java.util.Calendar;
import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Scheduled disposition job.
 *
 * Automatically cuts off eligible nodes.
 *
 * @author Roy Wetherall
 */
public class ScheduledDispositionJob implements Job
{
    /** Logger */
    private static Log logger = LogFactory.getLog(ScheduledDispositionJob.class);

    /**
     * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
     */
    public void execute(JobExecutionContext context) throws JobExecutionException
    {
    	RecordsManagementActionService rmActionService
    	    = (RecordsManagementActionService)context.getJobDetail().getJobDataMap().get("recordsManagementActionService");
    	NodeService nodeService = (NodeService)context.getJobDetail().getJobDataMap().get("nodeService");


    	// Calculate the date range used in the query
    	Calendar cal = Calendar.getInstance();
    	String year = String.valueOf(cal.get(Calendar.YEAR));
    	String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
    	String dayOfMonth = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));

    	//TODO These pad() calls are in RMActionExecuterAbstractBase. I've copied them
    	//     here as I have no access to that class.

    	final String currentDate = padString(year, 2) + "-" + padString(month, 2) +
    	    "-" + padString(dayOfMonth, 2) + "T00:00:00.00Z";

    	if (logger.isDebugEnabled())
    	{
    		StringBuilder msg = new StringBuilder();
    		msg.append("Executing ")
    		    .append(this.getClass().getSimpleName())
    		    .append(" with currentDate ")
    		    .append(currentDate);
    		logger.debug(msg.toString());
    	}

    	//TODO Copied the 1970 start date from the old RM JavaScript impl.
    	String dateRange = "[\"1970-01-01T00:00:00.00Z\" TO \"" + currentDate + "\"]";

    	// Execute the query and process the results
    	String query = "+ASPECT:\"rma:record\" +ASPECT:\"rma:dispositionSchedule\" +@rma\\:dispositionAsOf:" + dateRange;

    	SearchService search = (SearchService)context.getJobDetail().getJobDataMap().get("searchService");
    	ResultSet results = search.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_FTS_ALFRESCO, query);

    	List<NodeRef> resultNodes = results.getNodeRefs();
    	results.close();

    	if (logger.isDebugEnabled())
    	{
    		StringBuilder msg = new StringBuilder();
    		msg.append("Found ")
    		    .append(resultNodes.size())
    		    .append(" records eligible for disposition.");
    		logger.debug(msg.toString());
    	}

    	for (NodeRef node : resultNodes	)
    	{
    		String dispActionName = (String)nodeService.getProperty(node, RecordsManagementModel.PROP_DISPOSITION_ACTION_NAME);

    		// Only automatically execute "cutoff" actions.
    		// destroy and transfer and anything else should be manual for now
    		if (dispActionName != null && dispActionName.equalsIgnoreCase("cutoff"))
    		{
    			rmActionService.executeRecordsManagementAction(node, dispActionName);

    			if (logger.isDebugEnabled())
    			{
    			    logger.debug("Performing " + dispActionName + " dispoition action on disposable item " + node.toString());
    			}
    		}
    	}
    }

    //TODO This has been pasted out of RMActionExecuterAbstractBase. To be relocated.
    private String padString(String s, int len)
    {
        String result = s;
        for (int i=0; i<(len - s.length()); i++)
        {
            result = "0" + result;
        }
        return result;
     }
}
