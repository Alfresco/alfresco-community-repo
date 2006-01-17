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
package org.alfresco.repo.importer;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class ImporterJob implements Job
{
    public ImporterJob()
    {
        super();
    }

    public void execute(JobExecutionContext executionContext) throws JobExecutionException
    {
        ImporterJobSPI importerJob = (ImporterJobSPI) executionContext.getJobDetail().getJobDataMap()
                .get("bean");
        if (importerJob != null)
        {
            importerJob.doImport();
        }

    }
}
