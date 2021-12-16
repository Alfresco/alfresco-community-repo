/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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
package org.alfresco.repo.rendition2;

import static java.lang.Thread.sleep;
import static org.alfresco.model.ContentModel.PROP_CONTENT;
import static org.alfresco.model.RenditionModel.PROP_RENDITION_CONTENT_HASH_CODE;
import static org.alfresco.repo.content.MimetypeMap.EXTENSION_BINARY;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.Collections;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.metadata.AsynchronousExtractor;
import org.alfresco.repo.content.metadata.MetadataExtracter;
import org.alfresco.repo.content.transform.LocalTransformServiceRegistry;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.thumbnail.ThumbnailRegistry;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.rendition.RenditionService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.transform.client.registry.TransformServiceRegistry;
import org.alfresco.util.BaseSpringTest;
import org.alfresco.util.GUID;
import org.alfresco.util.PropertyMap;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.quartz.CronExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ResourceUtils;

import junit.framework.AssertionFailedError;

/**
 * Class unites common utility methods for {@link org.alfresco.repo.rendition2} package tests.
 */
public abstract class AbstractRenditionIntegrationTest extends BaseSpringTest
{
    @Autowired
    protected RenditionService2Impl renditionService2;

    @Autowired
    protected RenditionDefinitionRegistry2Impl renditionDefinitionRegistry2;

    @Autowired
    protected RenditionService renditionService;

    @Autowired
    protected ThumbnailRegistry thumbnailRegistry;

    @Autowired
    protected MimetypeMap mimetypeMap;

    @Autowired
    protected MimetypeService mimetypeService;

    @Autowired
    protected NodeService nodeService;

    @Autowired
    protected ContentService contentService;

    @Autowired
    protected TransactionService transactionService;

    @Autowired
    protected MutableAuthenticationService authenticationService;

    @Autowired
    protected PersonService personService;

    @Autowired
    protected PermissionService permissionService;

    @Autowired
    protected TransformServiceRegistry transformServiceRegistry;

    @Autowired
    protected LocalTransformServiceRegistry localTransformServiceRegistry;

    @Autowired
    protected TransformationOptionsConverter converter;

    @Autowired
    protected AsynchronousExtractor asynchronousExtractor;

    static String PASSWORD = "password";

    protected static final String ADMIN = "admin";
    protected static final String DOC_LIB = "doclib";

    private CronExpression origLocalTransCron;
    private CronExpression origRenditionCron;

    @BeforeClass
    public static void before()
    {
        // Use the docker images for transforms (local)
        System.setProperty("localTransform.core-aio.url", "http://localhost:8090/");
    }

    protected static void none()
    {
        System.setProperty("transform.service.enabled", "false");
        System.setProperty("local.transform.service.enabled", "false");
    }

    protected static void local()
    {
        System.setProperty("transform.service.enabled", "false");
        System.setProperty("local.transform.service.enabled", "true");

        // Strict MimetypeCheck
        System.setProperty("transformer.strict.mimetype.check", "true");
        //  Retry on DifferentMimetype
        System.setProperty("content.transformer.retryOn.different.mimetype", "true");
    }

    protected static void service()
    {
        System.setProperty("transform.service.enabled", "true");
        System.setProperty("local.transform.service.enabled", "false");
    }

    protected static void localService()
    {
        System.setProperty("transform.service.enabled", "true");
        System.setProperty("local.transform.service.enabled", "true");
    }

    @Before
    public void setUp() throws Exception
    {
        assertTrue("The RenditionService2 needs to be enabled", renditionService2.isEnabled());

        origLocalTransCron = localTransformServiceRegistry.getCronExpression();
        localTransformServiceRegistry.setCronExpression(null);
        boolean localTransformServiceEnabled = Boolean.parseBoolean(System.getProperty("local.transform.service.enabled"));
        localTransformServiceRegistry.setEnabled(localTransformServiceEnabled);
        localTransformServiceRegistry.afterPropertiesSet();

        if (transformServiceRegistry instanceof LocalTransformServiceRegistry)
        {
            ((LocalTransformServiceRegistry)transformServiceRegistry).setEnabled(localTransformServiceEnabled);
        }

        thumbnailRegistry.setTransformServiceRegistry(transformServiceRegistry);
        thumbnailRegistry.setLocalTransformServiceRegistry(localTransformServiceRegistry);
        thumbnailRegistry.setConverter(converter);

        origRenditionCron = renditionDefinitionRegistry2.getCronExpression();
        renditionDefinitionRegistry2.setCronExpression(null);
        renditionDefinitionRegistry2.setTransformServiceRegistry(transformServiceRegistry);
        renditionDefinitionRegistry2.setTransformServiceRegistry(localTransformServiceRegistry);
        renditionDefinitionRegistry2.afterPropertiesSet();
    }

    @After
    public void cleanUp()
    {
        localTransformServiceRegistry.setCronExpression(origLocalTransCron);
        renditionDefinitionRegistry2.setCronExpression(origRenditionCron);

        AuthenticationUtil.clearCurrentSecurityContext();
    }

    @AfterClass
    public static void after()
    {
        System.clearProperty("localTransform.core-aio.url");
        System.clearProperty("transform.service.enabled");
        System.clearProperty("local.transform.service.enabled");
    }

    protected void checkRendition(String testFileName, String renditionName, boolean expectedToPass)
    {
        try
        {
            NodeRef sourceNodeRef = createSource(ADMIN, testFileName);
            render(ADMIN, sourceNodeRef, renditionName);
            waitForRendition(ADMIN, sourceNodeRef, renditionName, true);
            if (!expectedToPass)
            {
                fail("The " + renditionName + " rendition should NOT be supported for " + testFileName);
            }
        }
        catch (UnsupportedOperationException e)
        {
            if (expectedToPass)
            {
                fail("The " + renditionName + " rendition SHOULD be supported for " + testFileName);
            }
        }
    }

    protected void checkExtract(String testFileName, boolean expectedToPass)
    {
        try
        {
            NodeRef sourceNodeRef = createSource(ADMIN, testFileName);
            extract(ADMIN, sourceNodeRef);
            waitForExtract(ADMIN, sourceNodeRef, true);
            if (!expectedToPass)
            {
                fail("The extract of metadata should NOT be supported for " + testFileName);
            }
        }
        catch (AssertionFailedError e)
        {
            if (expectedToPass)
            {
                fail("The extract of metadata SHOULD be supported for " + testFileName);
            }
        }
    }

    // Creates a new source node as the given user in its own transaction.
    protected NodeRef createSource(String user, String testFileName)
    {
        return AuthenticationUtil.runAs(() ->
                transactionService.getRetryingTransactionHelper().doInTransaction(() ->
                        createSource(testFileName)), user);
    }

    // Creates a new source node as the current user in the current transaction.
    private NodeRef createSource(String testFileName) throws FileNotFoundException
    {
        return createContentNodeFromQuickFile(testFileName);
    }

    // Changes the content of a source node as the given user in its own transaction.
    protected void updateContent(String user, NodeRef sourceNodeRef, String testFileName)
    {
        AuthenticationUtil.runAs((AuthenticationUtil.RunAsWork<Void>) () ->
                transactionService.getRetryingTransactionHelper().doInTransaction(() ->
                {
                    updateContent(sourceNodeRef, testFileName);
                    return null;
                }), user);
    }

    // Changes the content of a source node as the current user in the current transaction.
    private NodeRef updateContent(NodeRef sourceNodeRef, String testFileName) throws FileNotFoundException
    {
        File file = ResourceUtils.getFile("classpath:quick/" + testFileName);
        nodeService.setProperty(sourceNodeRef, ContentModel.PROP_NAME, testFileName);

        ContentWriter contentWriter = contentService.getWriter(sourceNodeRef, ContentModel.PROP_CONTENT, true);
        contentWriter.setMimetype(mimetypeService.guessMimetype(testFileName));
        contentWriter.putContent(file);

        return sourceNodeRef;
    }

    // Clears the content of a source node as the given user in its own transaction.
    protected void clearContent(String user, NodeRef sourceNodeRef)
    {
        AuthenticationUtil.runAs((AuthenticationUtil.RunAsWork<Void>) () ->
                transactionService.getRetryingTransactionHelper().doInTransaction(() ->
                {
                    clearContent(sourceNodeRef);
                    return null;
                }), user);
    }

    // Clears the content of a source node as the current user in the current transaction.
    private void clearContent(NodeRef sourceNodeRef)
    {
        nodeService.removeProperty(sourceNodeRef, PROP_CONTENT);
    }

    // Requests a new rendition as the given user in its own transaction.
    protected void render(String user, NodeRef sourceNode, String renditionName)
    {
        AuthenticationUtil.runAs((AuthenticationUtil.RunAsWork<Void>) () ->
                transactionService.getRetryingTransactionHelper().doInTransaction(() ->
                {
                    render(sourceNode, renditionName);
                    return null;
                }), user);
    }

    // Requests a new metadata extract as the given user in its own transaction.
    protected void extract(String user, NodeRef sourceNode)
    {
        AuthenticationUtil.runAs((AuthenticationUtil.RunAsWork<Void>) () ->
                transactionService.getRetryingTransactionHelper().doInTransaction(() ->
                {
                    extract(sourceNode);
                    return null;
                }), user);
    }

    // Requests a new rendition as the current user in the current transaction.
    private void render(NodeRef sourceNodeRef, String renditionName)
    {
        renditionService2.render(sourceNodeRef, renditionName);
    }

    // Requests a new metadata extract as the current user in the current transaction.
    private void extract(NodeRef sourceNodeRef)
    {
        ContentReader reader = contentService.getReader(sourceNodeRef, ContentModel.PROP_CONTENT);
        asynchronousExtractor.extract(sourceNodeRef, reader, MetadataExtracter.OverwritePolicy.PRAGMATIC,
                Collections.emptyMap(), Collections.emptyMap());
    }

    // As a given user waitForRendition for a rendition to appear. Creates new transactions to do this.
    protected NodeRef waitForRendition(String user, NodeRef sourceNodeRef, String renditionName, boolean shouldExist) throws AssertionFailedError
    {
        try
        {
            return AuthenticationUtil.runAs(() -> waitForRendition(sourceNodeRef, renditionName, shouldExist), user);
        }
        catch (RuntimeException e)
        {
            Throwable cause = e.getCause();
            if (cause instanceof AssertionFailedError)
            {
                throw (AssertionFailedError)cause;
            }
            throw e;
        }
    }

    // As a given user waitForExtract to appear. Creates new transactions to do this.
    protected void waitForExtract(String user, NodeRef sourceNodeRef, boolean nodePropsShouldChange) throws AssertionFailedError
    {
        try
        {
            AuthenticationUtil.runAs(() -> waitForExtract(sourceNodeRef, nodePropsShouldChange), user);
        }
        catch (RuntimeException e)
        {
            Throwable cause = e.getCause();
            if (cause instanceof AssertionFailedError)
            {
                throw (AssertionFailedError)cause;
            }
            throw e;
        }
    }

    // As the current user waitForRendition for a rendition to appear. Creates new transactions to do this.
    private NodeRef waitForRendition(NodeRef sourceNodeRef, String renditionName, boolean shouldExist) throws InterruptedException
    {
        long maxMillis = 10000;
        ChildAssociationRef assoc = null;
        for (int i = (int)(maxMillis / 1000); i >= 0; i--)
        {
            // Must create a new transaction in order to see changes that take place after this method started.
            assoc = transactionService.getRetryingTransactionHelper().doInTransaction(() ->
                    renditionService2.getRenditionByName(sourceNodeRef, renditionName), true, true);
            if (assoc != null)
            {
                break;
            }
            logger.debug("RenditionService2.getRenditionByName(...) sleep "+i);
            sleep(1000);
        }
        if (shouldExist)
        {
            assertNotNull("Rendition " + renditionName + " failed", assoc);
            return assoc.getChildRef();
        }
        else
        {
            assertNull("Rendition " + renditionName + " did not fail", assoc);
            return null;
        }
    }

    // As the current user waitForRendition for a rendition to appear. Creates new transactions to do this.
    private Object waitForExtract(NodeRef sourceNodeRef, boolean nodePropsShouldChange) throws InterruptedException
    {
        long maxMillis = 5000;
        boolean nodeModified = true;
        for (int i = (int)(maxMillis / 1000); i >= 0; i--)
        {
            // Must create a new transaction in order to see changes that take place after this method started.
            nodeModified = transactionService.getRetryingTransactionHelper().doInTransaction(() ->
            {
                Serializable created = nodeService.getProperty(sourceNodeRef, ContentModel.PROP_CREATED);
                Serializable modified = nodeService.getProperty(sourceNodeRef, ContentModel.PROP_MODIFIED);
                return !created.equals(modified);
            }, true, true);
            if (nodeModified)
            {
                break;
            }
            logger.debug("waitForExtract sleep "+i);
            sleep(1000);
        }
        if (nodePropsShouldChange)
        {
            assertTrue("Extract failed", nodeModified);
        }
        else
        {
            assertFalse("Extract did not fail", nodeModified);
        }
        return null;
    }

    protected String getTestFileName(String sourceMimetype) throws FileNotFoundException
    {
        String extension = mimetypeMap.getExtension(sourceMimetype);
        String testFileName = extension.equals(EXTENSION_BINARY) ? null : "quick."+extension;
        if (testFileName != null)
        {
            try
            {
                ResourceUtils.getFile("classpath:quick/" + testFileName);
            }
            catch (FileNotFoundException e)
            {
                testFileName = null;
            }
        }
        return testFileName;
    }

    NodeRef createContentNodeFromQuickFile(String fileName) throws FileNotFoundException
    {
        NodeRef rootNodeRef = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        NodeRef folderNodeRef = nodeService.createNode(
                rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName(getName() + GUID.generate()),
                ContentModel.TYPE_FOLDER).getChildRef();

        File file = ResourceUtils.getFile("classpath:quick/" + fileName);
        NodeRef contentRef = nodeService.createNode(
                folderNodeRef,
                ContentModel.ASSOC_CONTAINS,
                ContentModel.ASSOC_CONTAINS,
                ContentModel.TYPE_CONTENT,
                Collections.singletonMap(ContentModel.PROP_NAME, fileName))
                .getChildRef();
        ContentWriter contentWriter = contentService.getWriter(contentRef, ContentModel.PROP_CONTENT, true);
        contentWriter.setMimetype(mimetypeService.guessMimetype(fileName));
        contentWriter.putContent(file);

        return contentRef;
    }

    static String generateNewUsernameString()
    {
        return "user-" + GUID.generate();
    }

    String createRandomUser()
    {
        return AuthenticationUtil.runAs(() ->
        {
            String username = generateNewUsernameString();
            createUser(username);
            return username;
        }, AuthenticationUtil.getAdminUserName());
    }

    void createUser(String username)
    {
        createUser(username, "firstName", "lastName", "jobTitle", 0);
    }

    void createUser(final String username,
                            final String firstName,
                            final String lastName,
                            final String jobTitle,
                            final long quota)
    {
        RetryingTransactionHelper.RetryingTransactionCallback<Void> createUserCallback = () ->
        {
            authenticationService.createAuthentication(username, PASSWORD.toCharArray());

            PropertyMap personProperties = new PropertyMap();
            personProperties.put(ContentModel.PROP_USERNAME, username);
            personProperties.put(ContentModel.PROP_AUTHORITY_DISPLAY_NAME, "title" + username);
            personProperties.put(ContentModel.PROP_FIRSTNAME, firstName);
            personProperties.put(ContentModel.PROP_LASTNAME, lastName);
            personProperties.put(ContentModel.PROP_EMAIL, username+"@example.com");
            personProperties.put(ContentModel.PROP_JOBTITLE, jobTitle);
            if (quota > 0)
            {
                personProperties.put(ContentModel.PROP_SIZE_QUOTA, quota);
            }
            personService.createPerson(personProperties);
            return null;
        };

        RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();
        txnHelper.doInTransaction(createUserCallback);
    }

    /**
     * Helper method to check if the supplied content hash code is valid or not
     *
     * @param contentHashCode
     *            the hash code to verify
     *
     * @return true in case it is an actual hash code, false otherwise
     */
    protected boolean isValidRenditionContentHashCode(int contentHashCode)
    {
        return contentHashCode != RenditionService2Impl.RENDITION2_DOES_NOT_EXIST
                && contentHashCode != RenditionService2Impl.SOURCE_HAS_NO_CONTENT;
    }

    /**
     * Helper method which gets the content hash code from the supplied rendition node without specific validations (the
     * equivalent method from {@link RenditionService2Impl} is not exposed)
     *
     * @param renditionNodeRef
     *            the rendition node
     *
     * @return -1 in case of there is no content, -2 in case rendition doesn't exist, the actual content hash code
     *         otherwise
     */
    protected int getRenditionContentHashCode(NodeRef renditionNodeRef)
    {
        int renditionContentHashCode = RenditionService2Impl.RENDITION2_DOES_NOT_EXIST;

        if (renditionNodeRef != null)
        {
            Serializable hashCode = nodeService.getProperty(renditionNodeRef, PROP_RENDITION_CONTENT_HASH_CODE);
            renditionContentHashCode = hashCode == null ? RenditionService2Impl.SOURCE_HAS_NO_CONTENT : (int) hashCode;
        }

        return renditionContentHashCode;
    }
}
