/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.repo.admin.patch.impl;

import java.util.*;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * MNT-13577: Prevent sites to be moved
 * 
 * @author Sergey Scherbovich
 * @author Alex Mukha
 *
 */
public class AddUnmovableAspectToSitesPatch extends AbstractPatch
{
    private static Log logger = LogFactory.getLog(AddUnmovableAspectToSitesPatch.class);
    private static final String MSG_SUCCESS = "patch.addUnmovableAspect.result";

    private final int NUM_THREADS = 4;
    private final int BATCH_SIZE = 200;

    private SiteService siteService; 

    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }

    @Override
    protected String applyInternal() throws Exception
    {
        BatchProcessWorkProvider<ChildAssociationRef> workProvider = new BatchProcessWorkProvider<ChildAssociationRef>()
        {
            NodeRef sitesRoot = siteService.getSiteRoot();
            List<ChildAssociationRef> sites = nodeService.getChildAssocs(sitesRoot, Collections.singleton(SiteModel.TYPE_SITE));
            final Iterator<ChildAssociationRef> iterator = sites.listIterator();

            @Override
            public int getTotalEstimatedWorkSize()
            {
                return sites.size();
            }

            @Override
            public Collection<ChildAssociationRef> getNextWork()
            {
                List<ChildAssociationRef> sites = new ArrayList<ChildAssociationRef>(BATCH_SIZE);
                while (iterator.hasNext() && sites.size() <= BATCH_SIZE)
                {
                    sites.add(iterator.next());
                }
                return sites;
            }
        };

        RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();
        txnHelper.setForceWritable(true);

        BatchProcessor<ChildAssociationRef> batchProcessor = new BatchProcessor<ChildAssociationRef>(
                "AddUnmovableAspectToSitesPatch",
                txnHelper,
                workProvider,
                NUM_THREADS,
                BATCH_SIZE,
                applicationEventPublisher,
                logger,
                1000);

        BatchProcessor.BatchProcessWorker<ChildAssociationRef> worker = new BatchProcessor.BatchProcessWorker<ChildAssociationRef>()
        {
            public void afterProcess() throws Throwable
            {
            }

            public void beforeProcess() throws Throwable
            {
            }

            public String getIdentifier(ChildAssociationRef entry)
            {
                return entry.toString();
            }

            public void process(ChildAssociationRef child) throws Throwable
            {
                nodeService.addAspect(child.getChildRef(), ContentModel.ASPECT_UNMOVABLE, null);
            }
        };

        batchProcessor.process(worker, true);

        return I18NUtil.getMessage(MSG_SUCCESS);
    }
}
