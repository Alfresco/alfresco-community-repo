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
package org.alfresco.repo.content.cleanup;

import org.alfresco.error.AlfrescoRuntimeException;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Triggers the deletion of unused content using a
 * {@link org.alfresco.repo.content.cleanup.ContentStoreCleaner}.
 * <p>
 * The following parameters are required:
 * <ul>
 *   <li><b>contentStoreCleaner</b>: The content store cleaner bean</li>
 * </ul>
 * 
 * @author Derek Hulley
 */
public class ContentStoreCleanupJob implements Job
{
    public ContentStoreCleanupJob()
    {
    }

    /**
     * Calls the cleaner to do its work
     */
    public void execute(JobExecutionContext context) throws JobExecutionException
    {
        JobDataMap jobData = context.getJobDetail().getJobDataMap();
        // extract the content cleaner to use
        Object contentStoreCleanerObj = jobData.get("contentStoreCleaner");
        if (contentStoreCleanerObj == null || !(contentStoreCleanerObj instanceof ContentStoreCleaner))
        {
            throw new AlfrescoRuntimeException(
                    "ContentStoreCleanupJob data must contain valid 'contentStoreCleaner' reference");
        }
        ContentStoreCleaner contentStoreCleaner = (ContentStoreCleaner) contentStoreCleanerObj;
        contentStoreCleaner.execute();
    }
}
