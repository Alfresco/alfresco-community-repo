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
package org.alfresco.repo.action;

import java.util.Comparator;

/**
 * This class is the base filter class for asynchronous actions. These filters are used in identifying
 * 'equivalent' actions in the asynchronous action execution service. By registering
 * a subclass of this type, all actions of a given action-definition-name that are still pending
 * (i.e. currently executing or in the queue awaiting execution) will be compared to any new action
 * and if they are equal (as determined by the compare implementation defined herein) the newly
 * submitted action will not be added to the queue and will be dropped.
 * 
 * Concrete subclasses can be implemented and then dependency-injected using the spring-bean
 * baseActionFilter as their parent.
 * 
 * @author Neil McErlean
 */
public abstract class AbstractAsynchronousActionFilter implements Comparator<OngoingAsyncAction>
{
    private String name;
    private String actionDefinitionName;
    private AsynchronousActionExecutionQueueImpl asynchronousActionExecutionQueue;

    /**
     * Gets the name of this comparator.
     * @return String
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * Sets the name of this comparator.
     * @param name String
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Gets the action definition name against which this comparator is registered.
     * @return String
     */
    public String getActionDefinitionName()
    {
        return this.actionDefinitionName;
    }

    public void setActionDefinitionName(String actionDefinitionName)
    {
        this.actionDefinitionName = actionDefinitionName;
    }

    public void setAsynchronousActionExecutionQueue(
            AsynchronousActionExecutionQueueImpl asynchronousActionExecutionQueue)
    {
        this.asynchronousActionExecutionQueue = asynchronousActionExecutionQueue;
    }
    
    public void init()
    {
        this.asynchronousActionExecutionQueue.registerActionFilter(this);
    }
}
