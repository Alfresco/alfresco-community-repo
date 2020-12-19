/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
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
package org.alfresco.opencmis;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.TestCase;

import org.alfresco.sync.events.types.ContentEventImpl;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.filestore.FileContentWriter;
import org.alfresco.repo.domain.node.ContentDataWithId;
import org.alfresco.repo.events.EventPublisherForTestingOnly;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.FileFilterMode.Client;
import org.alfresco.util.GUID;
import org.alfresco.util.TempFileProvider;
import org.alfresco.util.testing.category.FrequentlyFailingTests;
import org.alfresco.util.testing.category.LuceneTests;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisStorageException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.chemistry.opencmis.commons.impl.server.AbstractServiceFactory;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.CmisService;
import org.apache.chemistry.opencmis.commons.server.TempStoreOutputStream;
import org.apache.chemistry.opencmis.server.shared.TempStoreOutputStreamFactory;
import org.junit.experimental.categories.Category;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.context.ApplicationContext;

/**
 * Tests basic local CMIS interaction
 * 
 * @author steveglover
 * @author Derek Hulley
 * @since 4.0
 */
@Category({OwnJVMTestsCategory.class, LuceneTests.class})
public class OpenCmisLocalTest extends TestCase
{
    public static final String[] CONFIG_LOCATIONS = new String[] { "classpath:alfresco/application-context.xml",
    	"classpath:opencmis/opencmistest-context.xml"
    																};
    private static ApplicationContext ctx;
    private static final String BEAN_NAME_AUTHENTICATION_COMPONENT = "authenticationComponent";
    private static final String MIME_PLAIN_TEXT = "text/plain";
    private TempStoreOutputStreamFactory streamFactory;
    private EventPublisherForTestingOnly eventPublisher;
    
    /**
     * Test class to provide the service factory
     * 
     * @author Derek Hulley
     * @since 4.0
     */
    public static class TestCmisServiceFactory extends AbstractServiceFactory
    {
        private static AlfrescoCmisServiceFactory serviceFactory;
        @Override
        public void init(Map<String, String> parameters)
        {
            serviceFactory = (AlfrescoCmisServiceFactory) ctx.getBean("CMISServiceFactory");
            serviceFactory.init(parameters);
        }

        @Override
        public void destroy()
        {
        }

        @Override
        public CmisService getService(CallContext context)
        {
            return serviceFactory.getService(context);
        }
        
    }

    private Repository getRepository(String user, String password)
    {
        // default factory implementation
        SessionFactory sessionFactory = SessionFactoryImpl.newInstance();
        Map<String, String> parameters = new HashMap<String, String>();

        // user credentials
        parameters.put(SessionParameter.USER, "admin");
        parameters.put(SessionParameter.PASSWORD, "admin");

        // connection settings
        parameters.put(SessionParameter.BINDING_TYPE, BindingType.LOCAL.value());
        parameters.put(SessionParameter.LOCAL_FACTORY, "org.alfresco.opencmis.OpenCmisLocalTest$TestCmisServiceFactory");

        // create session
        List<Repository> repositories = sessionFactory.getRepositories(parameters);
        return repositories.size() > 0 ? repositories.get(0) : null;
    }
    
    public void setUp() throws Exception
    {
        ctx = ApplicationContextHelper.getApplicationContext(CONFIG_LOCATIONS);
        File tempDir = new File(TempFileProvider.getTempDir(), GUID.generate());
        this.streamFactory = TempStoreOutputStreamFactory.newInstance(tempDir, 1024, 1024, false);
        this.eventPublisher = (EventPublisherForTestingOnly) ctx.getBean("eventPublisher");
    }
    
    public void testVoid()
    {
        
    }
    
    public void DISABLED_testSetUp() throws Exception
    {
        Repository repository = getRepository("admin", "admin");
        assertNotNull("No repository available for testing", repository);
    }
    
    public void DISABLED_testBasicFileOps()
    {
        Repository repository = getRepository("admin", "admin");
        Session session = repository.createSession();
        Folder rootFolder = session.getRootFolder();
        // create folder
        Map<String,String> folderProps = new HashMap<String, String>();
        {
            folderProps.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
            folderProps.put(PropertyIds.NAME, getName() + "-" + GUID.generate());
        }
        Folder folder = rootFolder.createFolder(folderProps, null, null, null, session.getDefaultContext());
        
        Map<String, String> fileProps = new HashMap<String, String>();
        {
            fileProps.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
            fileProps.put(PropertyIds.NAME, "mydoc-" + GUID.generate() + ".txt");
        }
        ContentStreamImpl fileContent = new ContentStreamImpl();
        {
            ContentWriter writer = new FileContentWriter(TempFileProvider.createTempFile(getName(), ".txt"));
            writer.putContent("Ipsum and so on");
            ContentReader reader = writer.getReader();
            fileContent.setMimeType(MimetypeMap.MIMETYPE_TEXT_PLAIN);
            fileContent.setStream(reader.getContentInputStream());
        }
        folder.createDocument(fileProps, fileContent, VersioningState.MAJOR);
    }
    
    public void testDownloadEvent() throws InterruptedException
    {
        Repository repository = getRepository("admin", "admin");
        Session session = repository.createSession();
        Folder rootFolder = session.getRootFolder();
        String docname = "mydoc-" + GUID.generate() + ".txt";
        Map<String, String> props = new HashMap<String, String>();
        {
            props.put(PropertyIds.OBJECT_TYPE_ID, "D:cmiscustom:document");
            props.put(PropertyIds.NAME, docname);
        }
        
        // content
        byte[] byteContent = "Hello from Download testing class".getBytes();
        InputStream stream = new ByteArrayInputStream(byteContent);
        ContentStream contentStream = new ContentStreamImpl(docname, BigInteger.valueOf(byteContent.length), "text/plain", stream);

        Document doc1 = rootFolder.createDocument(props, contentStream, VersioningState.MAJOR);
        NodeRef doc1NodeRef = cmisIdToNodeRef(doc1.getId());
        
        ContentStream content = doc1.getContentStream();
        assertNotNull(content);
        
        //range request
        content = doc1.getContentStream(BigInteger.valueOf(2),BigInteger.valueOf(4));
        assertNotNull(content);
    }

    private void commonAsserts(byte[] byteContent,ContentEventImpl cre)
    {
        assertEquals(Client.cmis, cre.getClient());
        assertEquals(byteContent.length, cre.getSize());
        assertEquals("text/plain", cre.getMimeType());
    }

    /**
     * Turns a CMIS id into a node ref
     * @param nodeId
     * @return
     */
    private NodeRef cmisIdToNodeRef(String nodeId)
    {
        int idx = nodeId.indexOf(";");
        if(idx != -1)
        {
            nodeId = nodeId.substring(0, idx);
        }
        NodeRef nodeRef = new NodeRef(nodeId);
        return nodeRef;
    }
    
    public void testALF10085() throws InterruptedException
    {
        Repository repository = getRepository("admin", "admin");
        Session session = repository.createSession();
        Folder rootFolder = session.getRootFolder();

        Map<String, String> props = new HashMap<String, String>();
        {
            props.put(PropertyIds.OBJECT_TYPE_ID, "D:cmiscustom:document");
            props.put(PropertyIds.NAME, "mydoc-" + GUID.generate() + ".txt");
        }
        Document doc1 = rootFolder.createDocument(props, null, null);
        
        props = new HashMap<String, String>();
        {
            props.put(PropertyIds.OBJECT_TYPE_ID, "D:cmiscustom:document");
            props.put(PropertyIds.NAME, "mydoc-" + GUID.generate() + ".txt");
        }
        Document doc2 = rootFolder.createDocument(props, null, null);
        
        Thread.sleep(6000); 
        
        session.getObject(doc1);

        doc1.refresh();
        Calendar doc1LastModifiedBefore = (Calendar)doc1.getProperty(PropertyIds.LAST_MODIFICATION_DATE).getFirstValue();
        assertNotNull(doc1LastModifiedBefore);

        doc2.refresh();
        Calendar doc2LastModifiedBefore = (Calendar)doc2.getProperty(PropertyIds.LAST_MODIFICATION_DATE).getFirstValue();
        assertNotNull(doc2LastModifiedBefore);

        // Add relationship A to B
        props = new HashMap<String, String>();
        {
            props.put(PropertyIds.OBJECT_TYPE_ID, "R:cmiscustom:assoc");
            props.put(PropertyIds.NAME, "A Relationship"); 
            props.put(PropertyIds.SOURCE_ID, doc1.getId()); 
            props.put(PropertyIds.TARGET_ID, doc2.getId()); 
        }
        session.createRelationship(props); 

        doc1.refresh();
        Calendar doc1LastModifiedAfter = (Calendar)doc1.getProperty(PropertyIds.LAST_MODIFICATION_DATE).getFirstValue();
        assertNotNull(doc1LastModifiedAfter);
        
        doc2.refresh();
        Calendar doc2LastModifiedAfter = (Calendar)doc2.getProperty(PropertyIds.LAST_MODIFICATION_DATE).getFirstValue();
        assertNotNull(doc2LastModifiedAfter);

        assertEquals(doc1LastModifiedBefore, doc1LastModifiedAfter);
        assertEquals(doc2LastModifiedBefore, doc2LastModifiedAfter);
    }
    
    // Test we don't get an exception with the interceptor
    public void testAlfrescoCmisStreamInterceptor() throws Exception
    {
        simulateCallWithAdvice(true);
    }

    private ContentStreamImpl makeContentStream(String filename, String mimetype, String content) throws IOException
    {
        TempStoreOutputStream tos = streamFactory.newOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(tos);
        writer.write(content);
        ContentStreamImpl contentStream = new ContentStreamImpl(filename, BigInteger.valueOf(tos.getLength()), MimetypeMap.MIMETYPE_TEXT_PLAIN, tos.getInputStream());
        return contentStream;
    }

    /**
     * Simulates the pattern of advice created by AlfrescoCmisServiceFactory.getService(CallContext),
     * optionally including a AlfrescoCmisStreamInterceptor which changes ContentStream parameter
     * values into ReusableContentStream parameters which unlike the original may be closed and then
     * opened again. This is important as retrying transaction advice is also added (simulated here
     * by the afterAdvice and the test target object. See MNT-285
     * @param includeStreamInterceptor
     */
    private void simulateCallWithAdvice(boolean includeStreamInterceptor) throws Exception
    {
        final int loops = 3;

        final AtomicInteger beforeAdviceCount = new AtomicInteger(0);
        final AtomicInteger afterAdviceCount = new AtomicInteger(0);
        final AtomicInteger targetCount = new AtomicInteger(0);
        
        MethodInterceptor beforeAdvice = new MethodInterceptor()
        {
            @Override
            public Object invoke(MethodInvocation mi) throws Throwable
            {
                beforeAdviceCount.incrementAndGet();
                return mi.proceed();
            }
        };
        
        AlfrescoCmisStreamInterceptor interceptor = new AlfrescoCmisStreamInterceptor();
        
        // Represents the retrying transaction
        MethodInterceptor afterAdvice = new MethodInterceptor()
        {
            @Override
            public Object invoke(MethodInvocation mi) throws Throwable
            {
                boolean exit = true;
                do
                {
                    try
                    {
                        afterAdviceCount.incrementAndGet();
                        return mi.proceed();
                    }
                    catch (RuntimeException e)
                    {
                        if ("Test".equals(e.getMessage()))
                        {
                            exit = false;
                        }
                        else
                        {
                            throw e;
                        }
                    }
                }
                while (!exit);
                return null;
            }
        };
        
        TestStreamTarget target = new TestStreamTarget()
        {
            @Override
            public void methodA(ContentStream csa, String str, ContentStream csb, ContentStream csc, int i) throws Exception
            {
                int count = targetCount.incrementAndGet();
                
                // Use input streams - normally only works once
                File a = null;
                File b = null;
                File c = null;
                try
                {
                    a = TempFileProvider.createTempFile(csa.getStream(), "csA", ".txt");
                    b = TempFileProvider.createTempFile(csb.getStream(), "csB", ".txt");
                    c = TempFileProvider.createTempFile(null,            "csC", ".txt");
                    
                    // Similar test to that in AlfrescoCmisServiceImpl.copyToTempFile(ContentStream)
                    if ((csa.getLength() > -1) && (a == null || csa.getLength() != a.length()))
                    {
                        throw new CmisStorageException("Expected " + csa.getLength() + " bytes but retrieved " +
                                (a == null ? -1 : a.length()) + " bytes!");
                    }
                }
                finally
                {
                    if (a != null)
                    {
                        a.delete();
                    }
                    if (b != null)
                    {
                        b.delete();
                    }
                    if (c != null)
                    {
                        c.delete();
                    }
                }
                
                // Force the input stream to be reused
                if (count < loops)
                {
                    throw new RuntimeException("Test");
                }
            }
        };
        
        ProxyFactory proxyFactory = new ProxyFactory(target);
        proxyFactory.addInterface(TestStreamTarget.class);
        proxyFactory.addAdvice(beforeAdvice);
        if (includeStreamInterceptor)
        {
            proxyFactory.addAdvice(interceptor);
        }
        proxyFactory.addAdvice(afterAdvice);
        TestStreamTarget proxy = (TestStreamTarget) proxyFactory.getProxy();

        File tempDir = new File(TempFileProvider.getTempDir(), GUID.generate());
        TempStoreOutputStreamFactory streamFactory = TempStoreOutputStreamFactory.newInstance(tempDir, 1024, 1024, false);
        TempStoreOutputStream tos = streamFactory.newOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(tos);
        writer.write("The cat sat on the mat");

        ContentStreamImpl csa = makeContentStream("file1", MimetypeMap.MIMETYPE_TEXT_PLAIN, "The cat sat on the mat");
        ContentStreamImpl csb = makeContentStream("file2", MimetypeMap.MIMETYPE_TEXT_PLAIN, "and the cow jumped over the moon.");
        proxy.methodA(csa, "ignored", csb, null, 10);

        assertEquals("beforeAdvice count", 1, beforeAdviceCount.intValue());
        assertEquals("afterAdvice count", 1, beforeAdviceCount.intValue());
        assertEquals("target count", loops, targetCount.intValue());
    }
    
    private interface TestStreamTarget
    {
        void methodA(ContentStream csa, String str, ContentStream csb, ContentStream csc, int i) throws Exception;
    }

    /**
     * MNT-14687 - Creating a document as checkedout and then cancelling the
     * checkout should delete the document.
     * 
     * This test would have fit better within CheckOutCheckInServiceImplTest but
     * was added here to make use of existing methods
     */
    @Category(FrequentlyFailingTests.class) // ACS-962
    public void testCancelCheckoutWhileInCheckedOutState()
    {
        ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        CheckOutCheckInService cociService = serviceRegistry.getCheckOutCheckInService();

        // Authenticate as system
        AuthenticationComponent authenticationComponent = (AuthenticationComponent) ctx.getBean(BEAN_NAME_AUTHENTICATION_COMPONENT);
        authenticationComponent.setSystemUserAsCurrentUser();

        /* Create the document using openCmis services */
        Repository repository = getRepository("admin", "admin");
        Session session = repository.createSession();
        Folder rootFolder = session.getRootFolder();

        // Set file properties
        String docname = "myDoc-" + GUID.generate() + ".txt";
        Map<String, String> props = new HashMap<String, String>();
        {
            props.put(PropertyIds.OBJECT_TYPE_ID, BaseTypeId.CMIS_DOCUMENT.value());
            props.put(PropertyIds.NAME, docname);
        }

        // Create some content
        byte[] byteContent = "Some content".getBytes();
        InputStream stream = new ByteArrayInputStream(byteContent);
        ContentStream contentStream = new ContentStreamImpl(docname, BigInteger.valueOf(byteContent.length), MIME_PLAIN_TEXT, stream);

        // Create the document
        Document doc1 = rootFolder.createDocument(props, contentStream, VersioningState.CHECKEDOUT);
        NodeRef doc1NodeRef = cmisIdToNodeRef(doc1.getId());
        NodeRef doc1WorkingCopy = cociService.getWorkingCopy(doc1NodeRef);

        /* Cancel Checkout */
        cociService.cancelCheckout(doc1WorkingCopy);

        /* Check if both the working copy and the document were deleted */
        NodeService nodeService = serviceRegistry.getNodeService();
        assertFalse(nodeService.exists(doc1NodeRef));
        assertFalse(nodeService.exists(doc1WorkingCopy));
    }

    public void testEncodingForCreateContentStream()
    {
        ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        FileFolderService ffs = serviceRegistry.getFileFolderService();
        // Authenticate as system
        AuthenticationComponent authenticationComponent = (AuthenticationComponent) ctx
                .getBean(BEAN_NAME_AUTHENTICATION_COMPONENT);
        authenticationComponent.setSystemUserAsCurrentUser();
        try
        {
            /* Create the document using openCmis services */
            Repository repository = getRepository("admin", "admin");
            Session session = repository.createSession();
            Folder rootFolder = session.getRootFolder();
            Document document = createDocument(rootFolder, "test_file_" + GUID.generate() + ".txt", session);

            ContentStream content = document.getContentStream();
            assertNotNull(content);

            content = document.getContentStream(BigInteger.valueOf(2), BigInteger.valueOf(4));
            assertNotNull(content);

            NodeRef doc1NodeRef = cmisIdToNodeRef(document.getId());
            FileInfo fileInfo = ffs.getFileInfo(doc1NodeRef);
            Map<QName, Serializable> properties = fileInfo.getProperties();
            ContentDataWithId contentData = (ContentDataWithId) properties
                    .get(QName.createQName("{http://www.alfresco.org/model/content/1.0}content"));
            String encoding = contentData.getEncoding();

            assertEquals("ISO-8859-1", encoding);
        }
        finally
        {
            authenticationComponent.clearCurrentSecurityContext();
        }
    }

    private static Document createDocument(Folder target, String newDocName, Session session)
    {
        Map<String, String> props = new HashMap<String, String>();
        props.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
        props.put(PropertyIds.NAME, newDocName);
        String content = "aegif Mind Share Leader Generating New Paradigms by aegif corporation.";
        byte[] buf = null;
        try
        {
            buf = content.getBytes("ISO-8859-1"); // set the encoding here for the content stream
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }

        ByteArrayInputStream input = new ByteArrayInputStream(buf);

        ContentStream contentStream = session.getObjectFactory().createContentStream(newDocName, buf.length,
                "text/plain; charset=UTF-8", input); // additionally set the charset here
        // NOTE that we intentionally specified the wrong charset here (as UTF-8)
        // because Alfresco does automatic charset detection, so we will ignore this explicit request
        return target.createDocument(props, contentStream, VersioningState.MAJOR);
    }
}
