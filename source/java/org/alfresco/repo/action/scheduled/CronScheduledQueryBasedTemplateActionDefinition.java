/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.action.scheduled;

import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;

import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.quartz.CronTrigger;
import org.quartz.Scheduler;
import org.quartz.Trigger;

/**
 * A scheduled action for which the trigger is defined in the standard cron format and the nodes to which the 
 * action should be run is defined from the nodes selected by query.
 * 
 * @author Andy Hind
 */
public class CronScheduledQueryBasedTemplateActionDefinition extends AbstractScheduledAction 
{
    /*
     * The search service.
     */
    private SearchService searchService;

    /*
     * The template service.
     */
    private TemplateService templateService;

    /*
     * The query language to use 
     */
    private String queryLanguage;

    /*
     * The stores against which the query should run  
     */
    private List<String> stores;

    /*
     * The template for the query
     */
    private String queryTemplate;

    /*
     * The cron expression
     */
    private String cronExpression;

    /*
     * The name of the job 
     */
    private String jobName;

    /*
     * The job group
     */
    private String jobGroup;

    /*
     * The name of the trigger
     */
    private String triggerName;

    /*
     * The name of the trigger group
     */
    private String triggerGroup;

    /*
     * The scheduler
     */
    private Scheduler scheduler;

    /*
     * The templateModelFactory 
     * 
     * This defines in which template language the query template is defined
     */
    private TemplateActionModelFactory templateActionModelFactory;

    public CronScheduledQueryBasedTemplateActionDefinition()
    {
        super();
    }

    //
    // IOC
    //

    public SearchService getSearchService()
    {
        return searchService;
    }

    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    public TemplateService getTemplateService()
    {
        return templateService;
    }

    public void setTemplateService(TemplateService templateService)
    {
        this.templateService = templateService;
    }

    public Scheduler getScheduler()
    {
        return scheduler;
    }

    public void setScheduler(Scheduler scheduler)
    {
        this.scheduler = scheduler;
    }

    public TemplateActionModelFactory getTemplateActionModelFactory()
    {
        return templateActionModelFactory;
    }

    public void setTemplateActionModelFactory(TemplateActionModelFactory templateActionModelFactory)
    {
        this.templateActionModelFactory = templateActionModelFactory;
    }

    //
    // End of IOC
    //

    @Override
    public Trigger getTrigger()
    {
        try
        {
            return new CronTrigger(getTriggerName(), getTriggerGroup(), getCronExpression());
        }
        catch (ParseException e)
        {
            throw new InvalidCronExpression("Invalid chron expression: n" + getCronExpression());
        }
    }

    @Override
    public List<NodeRef> getNodes()
    {
        LinkedList<NodeRef> nodeRefs = new LinkedList<NodeRef>();

        // Build the actual query string
        String queryTemplate = getQueryTemplate();
        String query = templateService.processTemplateString(getTemplateActionModelFactory().getTemplateEngine(),
                queryTemplate, getTemplateActionModelFactory().getModel());

        // Execute the query
        SearchParameters sp = new SearchParameters();
        sp.setLanguage(getQueryLanguage());
        sp.setQuery(query);
        for (String storeRef : getStores())
        {
            sp.addStore(new StoreRef(storeRef));
        }

        // Transform the reults into a node list
        ResultSet results = null;
        try
        {
            results = searchService.query(sp);
            for (ResultSetRow row : results)
            {
                nodeRefs.add(row.getNodeRef());
            }
        }
        finally
        {
            if (results != null)
            {
                results.close();
            }
        }
        return nodeRefs;
    }

    @Override
    public Action getAction(NodeRef nodeRef)
    {
        // Use the template to build its action
        return getTemplateActionDefinition().getAction(nodeRef);
    }

    //
    // IOC/Getters/Setters for instance variables
    //
    
    public void setQueryLanguage(String queryLanguage)
    {
        this.queryLanguage = queryLanguage;
    }

    public String getQueryLanguage()
    {
        return queryLanguage;
    }

    public void setStores(List<String> stores)
    {
        this.stores = stores;
    }

    public List<String> getStores()
    {
        return stores;
    }

    public void setQueryTemplate(String queryTemplate)
    {
        this.queryTemplate = queryTemplate;
    }

    public String getQueryTemplate()
    {
        return queryTemplate;
    }

    public void setCronExpression(String cronExpression)
    {
        this.cronExpression = cronExpression;
    }

    public String getCronExpression()
    {
        return cronExpression;
    }

    public void setJobName(String jobName)
    {
        this.jobName = jobName;
    }

    public String getJobName()
    {
        return jobName;
    }

    public void setJobGroup(String jobGroup)
    {
        this.jobGroup = jobGroup;
    }

    public String getJobGroup()
    {
        return jobGroup;
    }

    public void setTriggerName(String triggerName)
    {
        this.triggerName = triggerName;
    }

    public String getTriggerName()
    {
        return triggerName;
    }

    public void setTriggerGroup(String triggerGroup)
    {
        this.triggerGroup = triggerGroup;
    }

    public String getTriggerGroup()
    {
        return this.triggerGroup;
    }

    /**
     * Register with the scheduler.
     */
    public void afterPropertiesSet() throws Exception
    {
        register(getScheduler());
    }

}
