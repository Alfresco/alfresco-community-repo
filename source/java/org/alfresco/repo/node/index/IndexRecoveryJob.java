/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.repo.node.index;

import org.springframework.extensions.surf.util.PropertyCheck;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Forces a index recovery using the {@link IndexRecovery recovery component} passed
 * in via the job detail.
 * <p>
 * Nothing is done if the cluster name property <b>alfresco.cluster.name</b> has not been set.
 * 
 * @author Derek Hulley
 */
public class IndexRecoveryJob implements Job
{
    public static final String KEY_INDEX_RECOVERY_COMPONENT = "indexRecoveryComponent";
    public static final String KEY_CLUSTER_NAME = "clusterName";
    
    /**
     * Forces a full index recovery using the {@link IndexRecovery recovery component} passed
     * in via the job detail.
     */
    public void execute(JobExecutionContext context) throws JobExecutionException
    {
        JobDataMap map = context.getJobDetail().getJobDataMap();
        IndexRecovery indexRecoveryComponent = (IndexRecovery) map.get(KEY_INDEX_RECOVERY_COMPONENT);
        if (indexRecoveryComponent == null)
        {
            throw new JobExecutionException("Missing job data: " + KEY_INDEX_RECOVERY_COMPONENT);
        }
        String clusterName = (String) map.get(KEY_CLUSTER_NAME);
        if (!PropertyCheck.isValidPropertyString(clusterName))
        {
            // No cluster name
            return;
        }
        // reindex
        indexRecoveryComponent.reindex();
    }
}
