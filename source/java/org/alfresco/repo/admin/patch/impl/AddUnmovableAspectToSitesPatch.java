package org.alfresco.repo.admin.patch.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.patch.AsynchronousPatch;
import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
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
public class AddUnmovableAspectToSitesPatch extends AsynchronousPatch
{
    private static Log logger = LogFactory.getLog(AddUnmovableAspectToSitesPatch.class);
    private static final String MSG_SUCCESS = "patch.addUnmovableAspect.result";

    private final int NUM_THREADS = 4;
    private final int BATCH_SIZE = 200;

    private SiteService siteService; 
    private BehaviourFilter behaviourFilter;

    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }

    public void setBehaviourFilter(BehaviourFilter behaviourFilter)
    {
        this.behaviourFilter = behaviourFilter;
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

        final String authenticatedUser = AuthenticationUtil.getFullyAuthenticatedUser();
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

            public void process(final ChildAssociationRef child) throws Throwable
            {
                /*
                 * Fix for MNT-15064.
                 * Run as authenticated user to make sure the nodes are searched in the correct space store.
                 */
                RunAsWork<Void> work = new RunAsWork<Void>()
                {
                    @Override
                    public Void doWork() throws Exception
                    {
                        try
                        {
                            behaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
                            nodeService.addAspect(child.getChildRef(), ContentModel.ASPECT_UNMOVABLE, null);
                        }
                        finally
                        {
                            behaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
                        }
                        return null;
                    }
                };
                AuthenticationUtil.runAs(work, authenticatedUser);
            }
        };

        batchProcessor.process(worker, true);

        return I18NUtil.getMessage(MSG_SUCCESS);
    }
}
