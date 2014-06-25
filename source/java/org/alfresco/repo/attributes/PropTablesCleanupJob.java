/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.repo.attributes;

import org.alfresco.repo.domain.propval.PropertyValueDAO;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Cleanup job to initiate cleaning of unused values from the alf_prop_xxx tables.
 *  
 * @author Matt Ward
 */
public class PropTablesCleanupJob implements Job
{
    protected static final Object PROPERTY_VALUE_DAO_KEY = "propertyValueDAO";

    @Override
    public void execute(JobExecutionContext jobCtx) throws JobExecutionException
    {
        JobDataMap jobData = jobCtx.getJobDetail().getJobDataMap();
        
        PropertyValueDAO propertyValueDAO = (PropertyValueDAO) jobData.get(PROPERTY_VALUE_DAO_KEY);
        if (propertyValueDAO == null)
        {
            throw new IllegalArgumentException(PROPERTY_VALUE_DAO_KEY + " in job data map was null");
        }
        
        propertyValueDAO.cleanupUnusedValues();
    }
}
