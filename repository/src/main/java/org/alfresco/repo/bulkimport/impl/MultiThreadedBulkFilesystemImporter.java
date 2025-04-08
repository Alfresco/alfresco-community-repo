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
package org.alfresco.repo.bulkimport.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.bulkimport.BulkFilesystemImporter;
import org.alfresco.repo.bulkimport.BulkImportParameters;
import org.alfresco.repo.bulkimport.FilesystemTracker;
import org.alfresco.repo.bulkimport.ImportableItem;
import org.alfresco.repo.bulkimport.NodeImporter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Performs a multi-threaded filesystem import into the repository using the {@link BatchProcessor}.
 * 
 * @since 4.0
 *
 */
public abstract class MultiThreadedBulkFilesystemImporter extends AbstractBulkFilesystemImporter
{
    protected final static Log logger = LogFactory.getLog(BulkFilesystemImporter.class);

    protected int defaultBatchSize;
    protected int defaultNumThreads;
    protected int defaultLoggingInterval = 100;

    protected int getLoggingInterval(BulkImportParameters bulkImportParameters)
    {
        return bulkImportParameters.getLoggingInterval() != null ? bulkImportParameters.getLoggingInterval() : defaultLoggingInterval;
    }

    protected int getBatchSize(BulkImportParameters bulkImportParameters)
    {
        return bulkImportParameters.getBatchSize() != null ? bulkImportParameters.getBatchSize() : defaultBatchSize;
    }

    protected int getNumThreads(BulkImportParameters bulkImportParameters)
    {
        return bulkImportParameters.getNumThreads() != null ? bulkImportParameters.getNumThreads() : defaultNumThreads;
    }

    protected BatchProcessor.BatchProcessWorker<ImportableItem> getWorker(final BulkImportParameters bulkImportParameters, final String lockToken,
            final NodeImporter nodeImporter, final FilesystemTracker filesystemTracker)
    {
        final int batchSize = bulkImportParameters.getBatchSize() != null ? bulkImportParameters.getBatchSize() : defaultBatchSize;
        final boolean rulesEnabled = ruleService.isEnabled();
        final String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();
        final String currentDomain = TenantUtil.getCurrentDomain();

        BatchProcessor.BatchProcessWorker<ImportableItem> worker = new BatchProcessor.BatchProcessWorker<ImportableItem>() {
            public String getIdentifier(ImportableItem importableItem)
            {
                return importableItem.toString();
            }

            public void beforeProcess() throws Throwable
            {
                // Run as the correct user
                AuthenticationUtil.setRunAsUser(currentUser);

                refreshLock(lockToken, batchSize * 250L);
                if (bulkImportParameters.isDisableRulesService() && rulesEnabled)
                {
                    ruleService.disableRules();
                }
            }

            public void afterProcess() throws Throwable
            {
                if (bulkImportParameters.isDisableRulesService() && rulesEnabled)
                {
                    ruleService.enableRules();
                }

                importStatus.incrementNumberOfBatchesCompleted();

                AuthenticationUtil.clearCurrentSecurityContext();
            }

            public void process(final ImportableItem importableItem) throws Throwable
            {
                if (importStatus.getLastException() != null)
                {
                    // bail out early if an exception occurs
                    return;
                }

                TenantUtil.runAsUserTenant(new TenantRunAsWork<Void>() {
                    @Override
                    public Void doWork() throws Exception
                    {
                        try
                        {
                            behaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);

                            NodeRef nodeRef = nodeImporter.importImportableItem(importableItem, bulkImportParameters.getExistingFileMode());
                            filesystemTracker.itemImported(nodeRef, importableItem);
                        }
                        finally
                        {
                            behaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
                        }

                        return null;
                    }
                }, currentUser, currentDomain);
            }
        };

        return worker;
    }

    protected BatchProcessor<ImportableItem> getBatchProcessor(final BulkImportParameters bulkImportParameters,
            final BatchProcessWorkProvider<ImportableItem> workProvider, final int loggingInterval)
    {
        final int numThreads = getNumThreads(bulkImportParameters);
        final int batchSize = getBatchSize(bulkImportParameters);

        importStatus.setNumThreads(numThreads);
        importStatus.setBatchSize(batchSize);

        BatchProcessor<ImportableItem> batchProcessor = new BatchProcessor<ImportableItem>(
                "Bulk Filesystem Import",
                transactionHelper,
                workProvider,
                numThreads, batchSize,
                applicationContext,
                logger, loggingInterval);

        return batchProcessor;
    }

    public void setDefaultNumThreads(int defaultNumThreads)
    {
        this.defaultNumThreads = defaultNumThreads;
    }

    public void setDefaultBatchSize(int defaultBatchSize)
    {
        this.defaultBatchSize = defaultBatchSize;
    }

    public int getDefaultNumThreads()
    {
        return defaultNumThreads;
    }

    public int getDefaultBatchSize()
    {
        return defaultBatchSize;
    }

    /**
     * Method that does the work of importing a filesystem using the BatchProcessor.
     * 
     * @param bulkImportParameters
     *            The bulk import parameters to apply to this bulk import.
     * @param nodeImporter
     *            The node importer implementation that will import each node.
     * @param lockToken
     *            The lock token to use during the bulk import.
     */
    @Override
    protected void bulkImportImpl(final BulkImportParameters bulkImportParameters, final NodeImporter nodeImporter, final String lockToken)
    {
        final int batchSize = getBatchSize(bulkImportParameters);
        final int numThreads = getNumThreads(bulkImportParameters);

        importStatus.setNumThreads(numThreads);
        importStatus.setBatchSize(batchSize);
    }

}
