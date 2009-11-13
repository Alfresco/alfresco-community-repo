/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
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
    /*
     * (non-Javadoc)
     * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
     */
    public void execute(JobExecutionContext executionContext) throws JobExecutionException
    {
        final UserRegistrySynchronizer userRegistrySynchronizer = (UserRegistrySynchronizer) executionContext
                .getJobDetail().getJobDataMap().get("userRegistrySynchronizer");
        final String synchronizeChangesOnly = (String) executionContext.getJobDetail().getJobDataMap().get(
                "synchronizeChangesOnly");
        AuthenticationUtil.runAs(new RunAsWork<Object>()
        {
            public Object doWork() throws Exception
            {
                userRegistrySynchronizer.synchronize(synchronizeChangesOnly == null
                        || !Boolean.parseBoolean(synchronizeChangesOnly), true, true);
                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
    }

}
