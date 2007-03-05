/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
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
 * A scheduled action for which the trigger is defined in the standard cron format and the nodes to which the action
 * should be run is defined from the nodes selected by query.
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
     * The templateModelFactory This defines in which template language the query template is defined
     */
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
     */
    public void setTemplateService(TemplateService templateService)
    {
        this.templateService = templateService;
    }

    /**
     * Get the scheduler.
     * @return - the scheduler.
     */
    public Scheduler getScheduler()
    {
        return scheduler;
    }

    /**
     * Set the scheduler.
     * @param scheduler
     */
    public void setScheduler(Scheduler scheduler)
    {
        this.scheduler = scheduler;
    }

    /**
     * Get the template action model factory.
     * @return - the template action model factory.
     */
    public TemplateActionModelFactory getTemplateActionModelFactory()
    {
        return templateActionModelFactory;
    }

    /**
     * Set the template action model factory.
     * @param templateActionModelFactory
     */
    public void setTemplateActionModelFactory(TemplateActionModelFactory templateActionModelFactory)
    {
        this.templateActionModelFactory = templateActionModelFactory;
    }

    //
    // End of IOC
    //
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.action.scheduled.AbstractScheduledAction#getTrigger()
     */
    @Override
    public Trigger getTrigger()
    {
        try
        {
            return new CronTrigger(getTriggerName(), getTriggerGroup(), getCronExpression());
        }
        catch (final ParseException e)
        {
            throw new InvalidCronExpression("Invalid chron expression: n" + getCronExpression());
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.action.scheduled.AbstractScheduledAction#getNodes()
     */
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

    /* (non-Javadoc)
     * @see org.alfresco.repo.action.scheduled.AbstractScheduledAction#getAction(org.alfresco.service.cmr.repository.NodeRef)
     */
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
     * @param queryLanguage 
     */
    public void setQueryLanguage(String queryLanguage)
    {
        this.queryLanguage = queryLanguage;
    }

    /**
     * Get the query language.
     * @return - the query language.
     */
    public String getQueryLanguage()
    {
        return queryLanguage;
    }

    /**
     * Set alist of stores to use.
     * @param stores - the list of stores.
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
     */
    public void setQueryTemplate(String queryTemplate)
    {
        this.queryTemplate = queryTemplate;
    }

    /**
     * Get the template from which to build the query.
     * @return - the template for the query.
     */
    public String getQueryTemplate()
    {
        return queryTemplate;
    }

    /**
     * Set the cron expression - see the wiki for examples.
     * @param cronExpression
     */
    public void setCronExpression(String cronExpression)
    {
        this.cronExpression = cronExpression;
    }


    /**
     * Get the cron expression.
     * @return - the cron expression.
     */
    public String getCronExpression()
    {
        return cronExpression;
    }

    /**
     * Set the job name.
     * @param jobName 
     */
    public void setJobName(String jobName)
    {
        this.jobName = jobName;
    }

    /**
     * Get the job name
     * @return - the job name.
     * 
     */
    public String getJobName()
    {
        return jobName;
    }

    /**
     * Set the job group.
     * @param jobGroup 
     */
    public void setJobGroup(String jobGroup)
    {
        this.jobGroup = jobGroup;
    }

    /**
     * Get the job group.
     * @return - the job group.
     */
    public String getJobGroup()
    {
        return jobGroup;
    }

    /** 
     * Set the trigger name.
     * @param triggerName 
     */
    public void setTriggerName(String triggerName)
    {
        this.triggerName = triggerName;
    }

    /**
     * Get the trigger name
     * @return - the trigger name.
     */
    public String getTriggerName()
    {
        return triggerName;
    }

    /**
     * Set the trigger group.
     * @param triggerGroup 
     */
    public void setTriggerGroup(String triggerGroup)
    {
        this.triggerGroup = triggerGroup;
    }

    /**
     * Get the name of the trigger group.
     * @return - the trigger group.
     * 
     */
    public String getTriggerGroup()
    {
        return this.triggerGroup;
    }

    /**
     * Register with the scheduler.
     * @throws Exception 
     */
    public void afterPropertiesSet() throws Exception
    {
        register(getScheduler());
    }

}
