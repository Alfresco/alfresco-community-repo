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
