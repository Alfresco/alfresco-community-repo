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
package org.alfresco.repo.content;

import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.search.SearchService;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Removes all content form the store that is not referenced by any content node.
 * <p>
 * The following parameters are required:
 * <ul>
 *   <li><b>contentStore</b>: The content store bean to clean up</li>
 *   <li><b>searcher</b>: The index searcher that searches for content in the store</li>
 *   <li><b>protectHours</b>: The number of hours to protect content that isn't referenced</li>
 * </ul>
 * 
 * @author Derek Hulley
 */
public class ContentStoreCleanupJob implements Job
{
    /**
     * Gets all content URLs from the store, checks if it is in use by any node
     * and deletes those that aren't.
     */
    public void execute(JobExecutionContext context) throws JobExecutionException
    {
        JobDataMap jobData = context.getJobDetail().getJobDataMap();
        // extract the content store to use
        Object contentStoreObj = jobData.get("contentStore");
        if (contentStoreObj == null || !(contentStoreObj instanceof ContentStore))
        {
            throw new AlfrescoRuntimeException(
                    "ContentStoreCleanupJob data must contain valid 'contentStore' reference");
        }
        ContentStore contentStore = (ContentStore) contentStoreObj;
        // extract the search to use
        Object searcherObj = jobData.get("searcher");
        if (searcherObj == null || !(searcherObj instanceof SearchService))
        {
            throw new AlfrescoRuntimeException(
                    "ContentStoreCleanupJob data must contain valid 'searcher' reference");
        }
        SearchService searcher = (SearchService) searcherObj;
        // get the number of hourse to protect content
        Object protectHoursObj = jobData.get("protectHours");
        if (protectHoursObj == null || !(protectHoursObj instanceof String))
        {
            throw new AlfrescoRuntimeException(
                    "ContentStoreCleanupJob data must contain valid 'protectHours' value");
        }
        long protectHours = 24L;
        try
        {
            protectHours = Long.parseLong((String) protectHoursObj);
        }
        catch (NumberFormatException e)
        {
            throw new AlfrescoRuntimeException(
                    "ContentStoreCleanupJob data 'protectHours' value is not a valid integer");
        }
        
        long protectMillis = protectHours * 3600L * 1000L;   // 3600s in an hour; 1000ms in a second
        long now = System.currentTimeMillis();
        long lastModifiedSafeTimeMs = (now - protectMillis);   // able to remove anything modified before this
        
        // get all URLs in the store
        Set<String> contentUrls = contentStore.getUrls();
        for (String contentUrl : contentUrls)
        {
            // TODO here we need to get hold of all the orphaned content in this store
            
            // not found - it is not in the repo, but check that it is old enough to delete
            ContentReader reader = contentStore.getReader(contentUrl);
            if (reader == null || !reader.exists())
            {
                // gone missing in the meantime
                continue;
            }
            long lastModified = reader.getLastModified();
            if (lastModified >= lastModifiedSafeTimeMs)
            {
                // not old enough
                continue;
            }
            
            // it is not in the repo and is old enough
            boolean result = contentStore.delete(contentUrl);
            System.out.println(contentUrl + ": " + Boolean.toString(result));
        }       
        
        // TODO for now throw this exception to ensure that this job does not get run until
        //      the orphaned content can be correctly retrieved
        throw new UnsupportedOperationException();
    }
}
