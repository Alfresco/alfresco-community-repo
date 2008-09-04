package org.alfresco.repo.cmis.ws;


import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.integrity.IntegrityChecker;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * Base class for all CMIS tests
 *
 * @author Dmitry Lazurkin
 *
 */
public class BaseServicePortTest extends AbstractDependencyInjectionSpringContextTests
{
    protected AuthenticationService authenticationService;
    protected TransactionService transactionService;
    protected NodeService nodeService;
    protected ServiceRegistry serviceRegistry;
    protected DictionaryService dictionaryService;

    protected AuthenticationComponent authenticationComponent;

    private UserTransaction txn;

    protected NodeRef rootNodeRef;

    @Override
    protected void onSetUp() throws Exception
    {
        super.onSetUp();

        serviceRegistry = (ServiceRegistry) applicationContext.getBean(ServiceRegistry.SERVICE_REGISTRY);

        authenticationService = serviceRegistry.getAuthenticationService();
        transactionService = serviceRegistry.getTransactionService();
        nodeService = serviceRegistry.getNodeService();
        dictionaryService = serviceRegistry.getDictionaryService();

        authenticationComponent = (AuthenticationComponent) applicationContext.getBean("authenticationComponent");

        authenticationComponent.setCurrentUser(authenticationComponent.getSystemUserName());

        // start the transaction
        txn = transactionService.getUserTransaction();
        txn.begin();

        IntegrityChecker.setWarnInTransaction();

        // authenticate
        authenticationComponent.setCurrentUser(authenticationComponent.getSystemUserName());

        // create a test store if need
        StoreRef storeRef = new StoreRef("workspace://CmisTestWorkspace");
        if (nodeService.exists(storeRef) == false)
        {
            storeRef = nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "CmisTestWorkspace");
        }
        NodeRef storeRootNodeRef = nodeService.getRootNode(storeRef);

        rootNodeRef = new NodeRef(storeRef, "alf:root_folder");
        if (nodeService.exists(rootNodeRef) == false)
        {
            rootNodeRef = nodeService.createNode(storeRootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName(NamespaceService.ALFRESCO_URI, "root_folder"),
                    ContentModel.TYPE_FOLDER).getChildRef();
        }

        authenticationComponent.clearCurrentSecurityContext();
    }

    @Override
    protected void onTearDown() throws Exception
    {
        try
        {
            txn.rollback();
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
    }

    @Override
    protected String[] getConfigLocations()
    {
        return new String[] { "classpath:alfresco/application-context.xml", "classpath:test-cmis-context.xml" };
    }

}
