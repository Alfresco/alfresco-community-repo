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
package org.alfresco.repo.security.sync;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * A scheduled job that regularly invokes a {@link UserRegistrySynchronizer}. Supports a
 * <code>synchronizeChangesOnly</code> string parameter. When <code>"false"</code> means that the
 * {@link UserRegistrySynchronizer#synchronize(boolean)} method will be called with a <code>true</code> forceUpdate
 * argument rather than the default <code>false</code>.
 * 
 * @author dward
 */
public class UserRegistrySynchronizerJob implements Job
{
    public void execute(JobExecutionContext executionContext) throws JobExecutionException
    {
        final UserRegistrySynchronizer userRegistrySynchronizer = (UserRegistrySynchronizer) executionContext
                .getJobDetail().getJobDataMap().get("userRegistrySynchronizer");
        final String synchronizeChangesOnly = (String) executionContext.getJobDetail().getJobDataMap().get("synchronizeChangesOnly");
        AuthenticationUtil.runAs(new RunAsWork<Object>()
        {
            public Object doWork() throws Exception
            {
                userRegistrySynchronizer.synchronize(synchronizeChangesOnly == null || !Boolean.parseBoolean(synchronizeChangesOnly), true);
                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
    }
}
