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
package org.alfresco.repo.action.scheduled;

import java.util.LinkedList;
import java.util.List;

import org.quartz.CronScheduleBuilder;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;

/**
 * A scheduled action for which the trigger is defined in the standard cron format and the nodes to which the action should be run is defined from the nodes selected by query.
 * 
 * @author Andy Hind
 */
public class CronScheduledQueryBasedTemplateActionDefinition extends AbstractScheduledAction
{
    /* The search service. */
    private SearchService searchService;

    /* The template service. */
    private TemplateService templateService;

    /* The query language to use */
    private String queryLanguage;

    /* The stores against which the query should run */
    private List<String> stores;

    /* The template for the query */
    private String queryTemplate;

    /* The cron expression */
    private String cronExpression;

    /* The name of the job */
    private String jobName;

    /* The job group */
    private String jobGroup;

    /* The name of the trigger */
    private String triggerName;

    /* The name of the trigger group */
    private String triggerGroup;

    /* The scheduler */
    private Scheduler scheduler;

    /* The templateModelFactory This defines in which template language the query template is defined */
    private TemplateActionModelFactory templateActionModelFactory;

    /**
     * Default constructore
     */
    public CronScheduledQueryBasedTemplateActionDefinition()
    {
        super();
    }

    //
    // IOC
    //

    /**
     * Get the search service.
     * 
     * @return - the serach service.
     */
    public SearchService getSearchService()
    {
        return searchService;
    }

    /**
     * Set the search service.
     * 
     * @param searchService
     *            SearchService
     */
    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    /**
     * Get the template service.
     * 
     * @return - the template service.
     */
    public TemplateService getTemplateService()
    {
        return templateService;
    }

    /**
     * Set the template service.
     * 
     * @param templateService
     *            TemplateService
     */
    public void setTemplateService(TemplateService templateService)
    {
        this.templateService = templateService;
    }

    /**
     * Get the scheduler.
     * 
     * @return - the scheduler.
     */
    public Scheduler getScheduler()
    {
        return scheduler;
    }

    /**
     * Set the scheduler.
     * 
     * @param scheduler
     *            Scheduler
     */
    public void setScheduler(Scheduler scheduler)
    {
        this.scheduler = scheduler;
    }

    /**
     * Get the template action model factory.
     * 
     * @return - the template action model factory.
     */
    public TemplateActionModelFactory getTemplateActionModelFactory()
    {
        return templateActionModelFactory;
    }

    /**
     * Set the template action model factory.
     * 
     * @param templateActionModelFactory
     *            TemplateActionModelFactory
     */
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
            return TriggerBuilder.newTrigger()
                    .withIdentity(getTriggerName(), getTriggerGroup())
                    .withSchedule(CronScheduleBuilder.cronSchedule(getCronExpression()))
                    .build();
        }
        // CronScheduleBuilder is throwing RuntimeException which is wrapping ParseException
        catch (final RuntimeException e)
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

        // MNT-11598 workaround: de-escape \$\{foo\} or \#\{foo\}
        if (queryTemplate.contains("\\$\\{") || queryTemplate.contains("\\#\\{"))
        {
            queryTemplate = queryTemplate.replace("\\$\\{", "${");
            queryTemplate = queryTemplate.replace("\\#\\{", "#{");

            if (queryTemplate.contains("\\}"))
            {
                queryTemplate = queryTemplate.replace("\\}", "}");
            }
        }

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

    /* (non-Javadoc)
     * 
     * @see org.alfresco.repo.action.scheduled.AbstractScheduledAction#getAction(org.alfresco.service.cmr.repository.NodeRef) */
    @Override
    public Action getAction(NodeRef nodeRef)
    {
        // Use the template to build its action
        return getTemplateActionDefinition().getAction(nodeRef);
    }

    //
    // IOC/Getters/Setters for instance variables
    //

    /**
     * Set the query language
     * 
     * @param queryLanguage
     *            String
     */
    public void setQueryLanguage(String queryLanguage)
    {
        this.queryLanguage = queryLanguage;
    }

    /**
     * Get the query language.
     * 
     * @return - the query language.
     */
    public String getQueryLanguage()
    {
        return queryLanguage;
    }

    /**
     * Set alist of stores to use.
     * 
     * @param stores
     *            - the list of stores.
     */
    public void setStores(List<String> stores)
    {
        this.stores = stores;
    }

    /**
     * Get the list of stores.
     * 
     * @return - the list of stores.
     */
    public List<String> getStores()
    {
        return stores;
    }

    /**
     * Set the template for the query.
     * 
     * @param queryTemplate
     *            String
     */
    public void setQueryTemplate(String queryTemplate)
    {
        this.queryTemplate = queryTemplate;
    }

    /**
     * Get the template from which to build the query.
     * 
     * @return - the template for the query.
     */
    public String getQueryTemplate()
    {
        return queryTemplate;
    }

    /**
     * Set the cron expression - see the wiki for examples.
     * 
     * @param cronExpression
     *            String
     */
    public void setCronExpression(String cronExpression)
    {
        this.cronExpression = cronExpression;
    }

    /**
     * Get the cron expression.
     * 
     * @return - the cron expression.
     */
    public String getCronExpression()
    {
        return cronExpression;
    }

    /**
     * Set the job name.
     * 
     * @param jobName
     *            String
     */
    public void setJobName(String jobName)
    {
        this.jobName = jobName;
    }

    /**
     * Get the job name
     * 
     * @return - the job name.
     * 
     */
    public String getJobName()
    {
        return jobName;
    }

    /**
     * Set the job group.
     * 
     * @param jobGroup
     *            String
     */
    public void setJobGroup(String jobGroup)
    {
        this.jobGroup = jobGroup;
    }

    /**
     * Get the job group.
     * 
     * @return - the job group.
     */
    public String getJobGroup()
    {
        return jobGroup;
    }

    /**
     * Set the trigger name.
     * 
     * @param triggerName
     *            String
     */
    public void setTriggerName(String triggerName)
    {
        this.triggerName = triggerName;
    }

    /**
     * Get the trigger name
     * 
     * @return - the trigger name.
     */
    public String getTriggerName()
    {
        return triggerName;
    }

    /**
     * Set the trigger group.
     * 
     * @param triggerGroup
     *            String
     */
    public void setTriggerGroup(String triggerGroup)
    {
        this.triggerGroup = triggerGroup;
    }

    /**
     * Get the name of the trigger group.
     * 
     * @return - the trigger group.
     * 
     */
    public String getTriggerGroup()
    {
        return this.triggerGroup;
    }

    /**
     * Register with the scheduler.
     * 
     * @throws Exception
     */
    public void afterPropertiesSet() throws Exception
    {
        register(getScheduler());
    }

}
