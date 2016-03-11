package org.alfresco.module.org_alfresco_module_rm.job;

import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.notification.RecordsManagementNotificationHelper;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This job finds all Vital Records which are due for review, optionally
 * excluding those for which notification has already been issued.
 *
 * @author Neil McErlean
 */
public class NotifyOfRecordsDueForReviewJobExecuter extends RecordsManagementJobExecuter
{
    private static Log logger = LogFactory.getLog(NotifyOfRecordsDueForReviewJobExecuter.class);

    private RecordsManagementNotificationHelper recordsManagementNotificationHelper;

    private NodeService nodeService;

    private SearchService searchService;

    public void setRecordsManagementNotificationHelper(
            RecordsManagementNotificationHelper recordsManagementNotificationHelper)
    {
        this.recordsManagementNotificationHelper = recordsManagementNotificationHelper;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.job.RecordsManagementJobExecuter#execute()
     */
    public void executeImpl()
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Job " + this.getClass().getSimpleName() + " starting.");
        }

        AuthenticationUtil.runAs(new RunAsWork<Object>()
        {
            public Object doWork()
            {
                // Query is for all records that are due for review and for which
                // notification has not been sent.
                StringBuilder queryBuffer = new StringBuilder();
                queryBuffer.append("+ASPECT:\"rma:vitalRecord\" ");
                queryBuffer.append("+(@rma\\:reviewAsOf:[MIN TO NOW] ) ");
                queryBuffer.append("+( ");
                queryBuffer.append("@rma\\:notificationIssued:false ");
                queryBuffer.append("OR ISNULL:\"rma:notificationIssued\" ");
                queryBuffer.append(") ");
                String query = queryBuffer.toString();

                ResultSet results = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_FTS_ALFRESCO, query);
                final List<NodeRef> resultNodes = results.getNodeRefs();
                results.close();

                if (logger.isDebugEnabled())
                {
                    logger.debug("Found " + resultNodes.size() + " nodes due for review and without notification.");
                }

                //If we have something to do and a template to do it with
                if(resultNodes.size() != 0)
                {
                    //Send the email message - but we must not retry since email is not transactional
                    RetryingTransactionCallback<Void> txCallbackSendEmail = new RetryingTransactionCallback<Void>()
                    {
                        // Set the notification issued property.
                        public Void execute()
                        {
                            // Send notification
                            recordsManagementNotificationHelper.recordsDueForReviewEmailNotification(resultNodes);

                            return null;
                        }
                    };

                    RetryingTransactionCallback<Boolean> txUpdateNodesCallback = new RetryingTransactionCallback<Boolean>()
                    {
                        // Set the notification issued property.
                        public Boolean execute()
                        {
                            for (NodeRef node : resultNodes)
                            {
                                nodeService.setProperty(node, RecordsManagementModel.PROP_NOTIFICATION_ISSUED, "true");
                            }
                            return Boolean.TRUE;
                        }
                    };

                    /**
                     * Now do the work, one action in each transaction
                     */
                    // don't retry the send email
                    retryingTransactionHelper.setMaxRetries(0);
                    retryingTransactionHelper.doInTransaction(txCallbackSendEmail);
                    retryingTransactionHelper.setMaxRetries(10);
                    retryingTransactionHelper.doInTransaction(txUpdateNodesCallback);
                }
                return null;
            }

        }, AuthenticationUtil.getSystemUserName());

        if (logger.isDebugEnabled())
        {
            logger.debug("Job " + this.getClass().getSimpleName() + " finished");
        }
    }  // end of execute method

}


