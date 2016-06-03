package org.alfresco.repo.content;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;

/**
 * Tests for guess mimetype for file
 * 
 * This includes a test for apple specific hidden files
 * 
 * @author rneamtu
 */

public class GuessMimetypeTest extends TestCase
{
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();

    private NodeService nodeService;
    private ContentService contentService;
    private RetryingTransactionHelper retryingTransactionHelper;
    private StoreRef storeRef;
    private NodeRef rootNodeRef;
    private NodeRef nodeRef;

    @Override
    public void setUp() throws Exception
    {
        this.nodeService = (NodeService) ctx.getBean("nodeService");
        this.contentService = (ContentService) ctx.getBean("ContentService");

        this.retryingTransactionHelper = (RetryingTransactionHelper) ctx.getBean("retryingTransactionHelper");

        retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Object>()
        {
            @Override
            public Object execute() throws Throwable
            {
                // As system user
                AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());

                storeRef = StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;

                rootNodeRef = nodeService.getRootNode(storeRef);

                return null;
            }
        });
    }

    public void testAppleMimetype() throws Exception
    {
        String content = "This is some content";
        String fileName = "._myfile.pdf";
        retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Object>()
        {
            @Override
            public Object execute() throws Throwable
            {
                AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());

                Map<QName, Serializable> properties = new HashMap<QName, Serializable>(13);
                properties.put(ContentModel.PROP_NAME, (Serializable) "test.txt");

                nodeRef = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{test}testnode"),
                        ContentModel.TYPE_CONTENT).getChildRef();
                return null;
            }
        });

        ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);

        writer.putContent(content);
        writer.guessMimetype(fileName);

        assertEquals(MimetypeMap.MIMETYPE_APPLEFILE, writer.getMimetype());

        fileName = "myfile.pdf";
        retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Object>()
        {
            @Override
            public Object execute() throws Throwable
            {
                AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());

                Map<QName, Serializable> properties = new HashMap<QName, Serializable>(13);
                properties.put(ContentModel.PROP_NAME, (Serializable) "test.txt");

                nodeRef = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{test}testnode"),
                        ContentModel.TYPE_CONTENT).getChildRef();
                return null;
            }
        });

        ContentWriter writer2 = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
        content = "This is other content";
        writer2.putContent(content);
        writer2.guessMimetype(fileName);

        assertNotSame(MimetypeMap.MIMETYPE_APPLEFILE, writer2.getMimetype());

    }
}
