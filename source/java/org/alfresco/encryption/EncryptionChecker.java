package org.alfresco.encryption;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.transaction.TransactionService;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

/**
 * The EncryptionChecker checks the state of the repository's encryption system.
 * In particular it checks:
 * <ul>
 *    <li> that the keystore exists and, if not, creates one.
 *    <li> that the encryption keys have not been changed. If so, the bootstrap will be halted.
 * </ul>
 * 
 * @since 4.0
 *
 */
public class EncryptionChecker extends AbstractLifecycleBean
{
    private TransactionService transactionService;
    private KeyStoreChecker keyStoreChecker;

    public void setKeyStoreChecker(KeyStoreChecker keyStoreChecker)
    {
        this.keyStoreChecker = keyStoreChecker;
    }
    
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();
        txnHelper.setForceWritable(true);      // Force write in case server is read-only
        
        txnHelper.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                try
                {
                    keyStoreChecker.validateKeyStores();
                }
                catch(Throwable e)
                {
                    // Just throw as a runtime exception
                    throw new AlfrescoRuntimeException("Keystores are invalid", e);
                }

                return null;
            }
        });
    }

    @Override
    protected void onShutdown(ApplicationEvent event)
    {
        
    }
}
