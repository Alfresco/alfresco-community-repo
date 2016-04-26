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
package org.alfresco.repo.scheduler;

import org.quartz.Scheduler;

/**
 * An interface used by the {@link AlfrescoSchedulerFactory} in order to initialise a Quartz job store with a reference
 * to its scheduler. This is primarily to allow the monitoring interface on the job store to allow functions such as
 * 'execute now'.
 * 
 * @author dward
 */
public interface SchedulerAware
{

    /**
     * Provides a reference to the scheduler.
     * 
     * @param scheduler
     *            the scheduler
     */
    public void setScheduler(Scheduler scheduler);
}
