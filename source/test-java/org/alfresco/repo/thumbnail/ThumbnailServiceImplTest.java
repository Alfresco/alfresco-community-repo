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

package org.alfresco.repo.thumbnail;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.model.RenditionModel;
import org.alfresco.repo.action.evaluator.NoConditionEvaluator;
import org.alfresco.repo.action.executer.AddFeaturesActionExecuter;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.transform.AbstractContentTransformer2;
import org.alfresco.repo.content.transform.AbstractContentTransformerTest;
import org.alfresco.repo.content.transform.ContentTransformer;
import org.alfresco.repo.content.transform.magick.ImageResizeOptions;
import org.alfresco.repo.content.transform.magick.ImageTransformationOptions;
import org.alfresco.repo.domain.hibernate.dialect.AlfrescoOracle9Dialect;
import org.alfresco.repo.domain.hibernate.dialect.AlfrescoSQLServerDialect;
import org.alfresco.repo.jscript.ClasspathScriptLocation;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.thumbnail.script.ScriptThumbnailService;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.rendition.RenditionService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentServiceTransientException;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.PagedSourceOptions;
import org.alfresco.service.cmr.repository.ScriptLocation;
import org.alfresco.service.cmr.repository.ScriptService;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.cmr.rule.RuleType;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.thumbnail.FailedThumbnailInfo;
import org.alfresco.service.cmr.thumbnail.ThumbnailParentAssociationDetails;
import org.alfresco.service.cmr.thumbnail.ThumbnailService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.BaseAlfrescoSpringTest;
import org.alfresco.util.GUID;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.dialect.DB2Dialect;
import org.hibernate.dialect.Dialect;
import org.junit.experimental.categories.Category;

/**
 * Thumbnail service implementation unit test
 * 
 * @author Roy Wetherall
 * @author Neil McErlean
 */
@Category(OwnJVMTestsCategory.class)
public class ThumbnailServiceImplTest extends BaseAlfrescoSpringTest
{
    private static Log logger = LogFactory.getLog(ThumbnailServiceImplTest.class);

    private NodeService secureNodeService;
    private RenditionService renditionService;
    private ThumbnailService thumbnailService;
    private ScriptThumbnailService scriptThumbnailService;
    private ScriptService scriptService;
    private MimetypeMap mimetypeMap;
    private TransactionService transactionService;
    private ServiceRegistry services;
    private FailureHandlingOptions failureHandlingOptions;
    private Repository repositoryHelper;
    private PermissionService permissionService;
    private LockService lockService;

    private NodeRef folder;
    private static final String TEST_FAILING_MIME_TYPE = "application/vnd.alfresco.test.transientfailure";
    private static final String TEST_LONG_RUNNING_MIME_TYPE = "application/vnd.alfresco.test.longrunning";
    private static final long TEST_LONG_RUNNING_TRANSFORM_TIME = 5000;
    private static final String TEST_LONG_RUNNING_PROPERTY_VALUE = "NewValue";

    /**
     * Called during the transaction setup
     */
    @SuppressWarnings("deprecation")
    @Override
    protected void onSetUpInTransaction() throws Exception
    {
        super.onSetUpInTransaction();

        // Get the required services
        this.secureNodeService = (NodeService) this.applicationContext.getBean("NodeService");
        this.renditionService = (RenditionService) this.applicationContext.getBean("RenditionService");
        this.thumbnailService = (ThumbnailService) this.applicationContext.getBean("ThumbnailService");
        this.scriptThumbnailService = (ScriptThumbnailService) this.applicationContext.getBean("thumbnailServiceScript");
        this.mimetypeMap = (MimetypeMap) this.applicationContext.getBean("mimetypeService");
        this.scriptService = (ScriptService) this.applicationContext.getBean("ScriptService");
        this.services = (ServiceRegistry) this.applicationContext.getBean("ServiceRegistry");
        this.transactionService = (TransactionService) this.applicationContext.getBean("transactionService");
        this.failureHandlingOptions = (FailureHandlingOptions) this.applicationContext.getBean("standardFailureOptions");
        this.repositoryHelper = (Repository) this.applicationContext.getBean("repositoryHelper");
        this.permissionService = (PermissionService) applicationContext.getBean("PermissionService");
        this.lockService = (LockService) applicationContext.getBean("lockService");

        // Create a folder and some content
        Map<QName, Serializable> folderProps = new HashMap<QName, Serializable>(1);
        folderProps.put(ContentModel.PROP_NAME, "testFolder");
        this.folder = this.secureNodeService.createNode(this.rootNodeRef, ContentModel.ASSOC_CHILDREN,
                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "testFolder"), ContentModel.TYPE_FOLDER)
                    .getChildRef();
    }
    
    @Override protected String[] getConfigLocations()
    {
        List<String> configLocations = new ArrayList<String>();
        for (String config : ApplicationContextHelper.CONFIG_LOCATIONS)
        {
            configLocations.add(config);
        }
        configLocations.add("classpath:org/alfresco/repo/thumbnail/test-thumbnail-context.xml");
        
        return configLocations.toArray(new String[0]);
    }
    
    private void checkTransformer()
    {
        ContentTransformer transformer = this.contentService.getImageTransformer();
        assertNotNull("No transformer returned for 'getImageTransformer'", transformer);

        // Check that it is working
        ImageTransformationOptions imageTransformationOptions = new ImageTransformationOptions();
        if (!transformer.isTransformable(MimetypeMap.MIMETYPE_IMAGE_JPEG, -1, MimetypeMap.MIMETYPE_IMAGE_JPEG,
                    imageTransformationOptions))
        {
            fail("Image transformer is not working.  Please check your image conversion command setup.");
        }
    }

    public void testCreateRenditionThumbnailFromImage() throws Exception
    {
        QName qname = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "doclib");

        ThumbnailDefinition details = thumbnailService.getThumbnailRegistry().getThumbnailDefinition(
                    qname.getLocalName());
        assertEquals("doclib", details.getName());
        assertEquals("image/png", details.getMimetype());
        assertEquals("alfresco/thumbnail/thumbnail_placeholder_doclib.png", details.getPlaceHolderResourcePath());

        checkTransformer();

        NodeRef jpgOrig = createOriginalContent(this.folder, MimetypeMap.MIMETYPE_IMAGE_JPEG);

        NodeRef thumbnail0 = this.thumbnailService.createThumbnail(jpgOrig, ContentModel.PROP_CONTENT,
                    MimetypeMap.MIMETYPE_IMAGE_JPEG, details.getTransformationOptions(), "doclib");
        assertNotNull(thumbnail0);
        checkRenditioned(jpgOrig, Collections.singletonList(new ExpectedAssoc(RegexQNamePattern.MATCH_ALL, "doclib", 1)));
        checkRendition("doclib", thumbnail0);
        outputThumbnailTempContentLocation(thumbnail0, "jpg", "doclib test");
    }
    
    public void testCreateRenditionThumbnailFromPdf() throws Exception
    {
        QName qname = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "doclib");

        ThumbnailDefinition details = thumbnailService.getThumbnailRegistry().getThumbnailDefinition(
                    qname.getLocalName());
        assertEquals("doclib", details.getName());
        assertEquals("image/png", details.getMimetype());
        assertEquals("alfresco/thumbnail/thumbnail_placeholder_doclib.png", details.getPlaceHolderResourcePath());

        checkTransformer();

        NodeRef pdfOrig = createOriginalContent(this.folder, MimetypeMap.MIMETYPE_PDF);

        NodeRef thumbnail0 = this.thumbnailService.createThumbnail(pdfOrig, ContentModel.PROP_CONTENT,
                    MimetypeMap.MIMETYPE_IMAGE_JPEG, details.getTransformationOptions(), "doclib");
        assertNotNull(thumbnail0);
        checkRenditioned(pdfOrig, Collections.singletonList(new ExpectedAssoc(RegexQNamePattern.MATCH_ALL, "doclib", 1)));
        checkRendition("doclib", thumbnail0);
        outputThumbnailTempContentLocation(thumbnail0, "jpg", "doclib test");
    }
    
    public void testCreateRenditionThumbnailFromPdfPage2() throws Exception
    {
        ImageTransformationOptions options = new ImageTransformationOptions();
        PagedSourceOptions pagedSourceOptions = new PagedSourceOptions();
        pagedSourceOptions.setStartPageNumber(new Integer(2));
        pagedSourceOptions.setEndPageNumber(new Integer(2));
        options.addSourceOptions(pagedSourceOptions);
        
        ThumbnailDefinition thumbnailDefinition = new ThumbnailDefinition(MimetypeMap.MIMETYPE_PDF, options, "doclib_2");
        thumbnailService.getThumbnailRegistry().addThumbnailDefinition(thumbnailDefinition);

        checkTransformer();

        NodeRef pdfOrig = createOriginalContent(this.folder, MimetypeMap.MIMETYPE_PDF);

        NodeRef thumbnail0 = this.thumbnailService.createThumbnail(pdfOrig, ContentModel.PROP_CONTENT,
                    MimetypeMap.MIMETYPE_IMAGE_JPEG, thumbnailDefinition.getTransformationOptions(), "doclib_2");
        assertNotNull(thumbnail0);
        checkRenditioned(pdfOrig, Collections.singletonList(new ExpectedAssoc(RegexQNamePattern.MATCH_ALL, "doclib_2", 1)));
        checkRendition("doclib_2", thumbnail0);
        
        // Check the length
        File tempFile = TempFileProvider.createTempFile("thumbnailServiceImplTest", ".jpg");
        ContentReader reader = this.contentService.getReader(thumbnail0, ContentModel.PROP_CONTENT);
        
        long size = reader.getSize();
        System.out.println("size=" + size);
        assertTrue("Page 2 should be blank and less than 4500 bytes", size < 4500);
        
        reader.getContent(tempFile);
        System.out.println("doclib_2 test: " + tempFile.getPath());
    }

    public void testCreateThumbnailFromImage() throws Exception
    {
        checkTransformer();

        NodeRef jpgOrig = createOriginalContent(this.folder, MimetypeMap.MIMETYPE_IMAGE_JPEG);
        NodeRef gifOrig = createOriginalContent(this.folder, MimetypeMap.MIMETYPE_IMAGE_GIF);

        // ===== small: 64x64, marked as thumbnail ====

        ImageResizeOptions imageResizeOptions = new ImageResizeOptions();
        imageResizeOptions.setWidth(64);
        imageResizeOptions.setHeight(64);
        imageResizeOptions.setResizeToThumbnail(true);
        ImageTransformationOptions imageTransformationOptions = new ImageTransformationOptions();
        imageTransformationOptions.setResizeOptions(imageResizeOptions);
        // ThumbnailDetails createOptions = new ThumbnailDetails();

        NodeRef thumbnail1 = this.thumbnailService.createThumbnail(jpgOrig, ContentModel.PROP_CONTENT,
                    MimetypeMap.MIMETYPE_IMAGE_JPEG, imageTransformationOptions, "small");
        assertNotNull(thumbnail1);
        checkRenditioned(jpgOrig, Collections.singletonList(new ExpectedAssoc(RegexQNamePattern.MATCH_ALL, "small", 1)));
        checkRendition("small", thumbnail1);
        outputThumbnailTempContentLocation(thumbnail1, "jpg", "small - 64x64, marked as thumbnail");

        // ===== small2: 64x64, aspect not maintained ====

        ImageResizeOptions imageResizeOptions2 = new ImageResizeOptions();
        imageResizeOptions2.setWidth(64);
        imageResizeOptions2.setHeight(64);
        imageResizeOptions2.setMaintainAspectRatio(false);
        ImageTransformationOptions imageTransformationOptions2 = new ImageTransformationOptions();
        imageTransformationOptions2.setResizeOptions(imageResizeOptions2);
        // ThumbnailDetails createOptions2 = new ThumbnailDetails();
        NodeRef thumbnail2 = this.thumbnailService.createThumbnail(jpgOrig, ContentModel.PROP_CONTENT,
                    MimetypeMap.MIMETYPE_IMAGE_JPEG, imageTransformationOptions2, "small2");
        checkRenditioned(jpgOrig, Collections.singletonList(new ExpectedAssoc(RegexQNamePattern.MATCH_ALL, "small2", 1)));
        checkRendition("small2", thumbnail2);
        outputThumbnailTempContentLocation(thumbnail2, "jpg", "small2 - 64x64, aspect not maintained");

        // ===== half: 50%x50 =====

        ImageResizeOptions imageResizeOptions3 = new ImageResizeOptions();
        imageResizeOptions3.setWidth(50);
        imageResizeOptions3.setHeight(50);
        imageResizeOptions3.setPercentResize(true);
        ImageTransformationOptions imageTransformationOptions3 = new ImageTransformationOptions();
        imageTransformationOptions3.setResizeOptions(imageResizeOptions3);
        // ThumbnailDetails createOptions3 = new ThumbnailDetails();
        NodeRef thumbnail3 = this.thumbnailService.createThumbnail(jpgOrig, ContentModel.PROP_CONTENT,
                    MimetypeMap.MIMETYPE_IMAGE_JPEG, imageTransformationOptions3, "half");
        checkRenditioned(jpgOrig, 
        		Collections.singletonList(new ExpectedAssoc(RegexQNamePattern.MATCH_ALL, "half", 1)));
        checkRendition("half", thumbnail3);
        outputThumbnailTempContentLocation(thumbnail3, "jpg", "half - 50%x50%");

        // ===== half2: 50%x50 from gif =====

        ImageResizeOptions imageResizeOptions4 = new ImageResizeOptions();
        imageResizeOptions4.setWidth(50);
        imageResizeOptions4.setHeight(50);
        imageResizeOptions4.setPercentResize(true);
        ImageTransformationOptions imageTransformationOptions4 = new ImageTransformationOptions();
        imageTransformationOptions4.setResizeOptions(imageResizeOptions4);
        // ThumbnailDetails createOptions4 = new ThumbnailDetails();
        NodeRef thumbnail4 = this.thumbnailService.createThumbnail(gifOrig, ContentModel.PROP_CONTENT,
                    MimetypeMap.MIMETYPE_IMAGE_JPEG, imageTransformationOptions4, "half2");
        checkRenditioned(gifOrig, Collections.singletonList(new ExpectedAssoc(RegexQNamePattern.MATCH_ALL, "half2", 1)));
        checkRendition("half2", thumbnail4);
        outputThumbnailTempContentLocation(thumbnail4, "jpg", "half2 - 50%x50%, from gif");
    }

    public void testDuplicationNames() throws Exception
    {
        checkTransformer();

        NodeRef jpgOrig = createOriginalContent(this.folder, MimetypeMap.MIMETYPE_IMAGE_JPEG);
        ImageResizeOptions imageResizeOptions = new ImageResizeOptions();
        imageResizeOptions.setWidth(64);
        imageResizeOptions.setHeight(64);
        imageResizeOptions.setResizeToThumbnail(true);
        ImageTransformationOptions imageTransformationOptions = new ImageTransformationOptions();
        imageTransformationOptions.setResizeOptions(imageResizeOptions);
        // ThumbnailDetails createOptions = new ThumbnailDetails();
        NodeRef thumbnail1 = this.thumbnailService.createThumbnail(jpgOrig, ContentModel.PROP_CONTENT,
                    MimetypeMap.MIMETYPE_IMAGE_JPEG, imageTransformationOptions, "small");
        assertNotNull(thumbnail1);
        checkRenditioned(jpgOrig, 
        		Collections.singletonList(new ExpectedAssoc(RegexQNamePattern.MATCH_ALL, "small", 1)));
        checkRendition("small", thumbnail1);

        // the origional thumbnail is returned if we are attempting to create a duplicate
        NodeRef duplicate = this.thumbnailService.createThumbnail(jpgOrig, ContentModel.PROP_CONTENT, MimetypeMap.MIMETYPE_IMAGE_JPEG,
                        imageTransformationOptions, "small");
        assertNotNull(duplicate);
        assertEquals(duplicate, thumbnail1);
    }

    /**
     * @since 3.5.0
     */
    public void testCreateFailingThumbnail() throws Exception
    {
        //see REPO-1528
        if(shouldTestBeSkippedForCurrentDB())
        {
            return;
        }
        logger.debug("Starting testCreateFailingThumbnail");

        final NodeRef corruptNode = this.createCorruptedContent(folder);
        logger.debug("Running failing thumbnail on " + corruptNode);
        
        // Make sure the source node is correctly set up before we start
        // It should not be renditioned and should not be marked as having any failed thumbnails.
        assertFalse(secureNodeService.hasAspect(corruptNode, RenditionModel.ASPECT_RENDITIONED));
        assertFalse(secureNodeService.hasAspect(corruptNode, ContentModel.ASPECT_FAILED_THUMBNAIL_SOURCE));
        
        setComplete();
        endTransaction();

        // Attempt to perform a thumbnail that we know will fail.
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
            {
                public Void execute() throws Throwable
                {
                    ThumbnailDefinition thumbnailDef = thumbnailService.getThumbnailRegistry().getThumbnailDefinition("doclib");
                    
                    Action createThumbnailAction = ThumbnailHelper.createCreateThumbnailAction(thumbnailDef, services);
                    actionService.executeAction(createThumbnailAction, corruptNode, true, true);
                    return null;
                }
            });
        // The thumbnail attempt has now failed. But a compensating action should have been scheduled that will mark the
        // source node with a failure aspect. As that is an asynchronous action, we need to wait for that to complete.
        
        Thread.sleep(3000); // This should be long enough for the compensating action to run.

        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                assertFalse("corrupt node should not have renditioned aspect", secureNodeService.hasAspect(corruptNode, RenditionModel.ASPECT_RENDITIONED));
                assertTrue("corrupt node should have failed thumbnails aspect", secureNodeService.hasAspect(corruptNode, ContentModel.ASPECT_FAILED_THUMBNAIL_SOURCE));

                Map<String, FailedThumbnailInfo> failedThumbnails = thumbnailService.getFailedThumbnails(corruptNode);
                assertEquals("Wrong number of failed thumbnails", 1, failedThumbnails.size());
              
                assertTrue("Missing QName for failed thumbnail", failedThumbnails.containsKey("doclib"));
                final FailedThumbnailInfo doclibFailureInfo = failedThumbnails.get("doclib");
                assertNotNull("Failure info was null", doclibFailureInfo);
                assertEquals("Failure count was wrong.", 1, doclibFailureInfo.getFailureCount());
                assertEquals("thumbnail name was wrong.", "doclib", doclibFailureInfo.getThumbnailDefinitionName());

                return null;
            }
        });
        
        // If you uncomment this line and set the timeout to a value greater than ${system.thumbnail.minimum.retry.period} * 1000.
        // Then the retry period will have passed, the below re-thumbnail attempt will be made and the test will fail with a
        // failureCount == 2.
        //
        // Thread.sleep(150 * 1000);

        // Run the thumbnail again. It should not run because the action condition should prevent it.
        // We can check that it does not run by ensuring the failureCount does not change.
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
                {
                    public Void execute() throws Throwable
                    {
                        ThumbnailDefinition thumbnailDef = thumbnailService.getThumbnailRegistry().getThumbnailDefinition("doclib");
                        
                        Action createThumbnailAction = ThumbnailHelper.createCreateThumbnailAction(thumbnailDef, services);
                        actionService.executeAction(createThumbnailAction, corruptNode, true, true);
                        return null;
                    }
                });
        // Pause to let the async action be considered for running (but not run).
        Thread.sleep(3000);

        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
                {
                    public Void execute() throws Throwable
                    {
                        Map<String, FailedThumbnailInfo> failedThumbnails = thumbnailService.getFailedThumbnails(corruptNode);
                        assertEquals("Wrong number of failed thumbnails", 1, failedThumbnails.size());
                      
                        assertTrue("Missing QName for failed thumbnail", failedThumbnails.containsKey("doclib"));
                        final FailedThumbnailInfo doclibFailureInfo = failedThumbnails.get("doclib");
                        assertNotNull("Failure info was null", doclibFailureInfo);
                        assertEquals("Failure count was wrong.", 1, doclibFailureInfo.getFailureCount());
                        assertEquals("thumbnail name was wrong.", "doclib", doclibFailureInfo.getThumbnailDefinitionName());

                        return null;
                    }
                });
    }
    
    /**
     * Inbound rule must not be applied on failed thumbnail
     * 
     * @see MNT-10914
     */
    public void testRuleExecutionOnFailedThumbnailChild() throws Exception
    {
        // create inbound rule on folder
        Map<String, Serializable> params = new HashMap<String, Serializable>(1);
        params.put("aspect-name", ContentModel.ASPECT_GEN_CLASSIFIABLE);
        Rule rule = new Rule();
        rule.setRuleType(RuleType.INBOUND);
        Action action = this.actionService.createAction(AddFeaturesActionExecuter.NAME, params);
        ActionCondition condition = this.actionService.createActionCondition(NoConditionEvaluator.NAME, null);
        action.addActionCondition(condition);
        rule.setAction(action);
        rule.applyToChildren(true);
        services.getRuleService().saveRule(folder, rule);

        setComplete();
        endTransaction();

        final NodeRef corruptNode = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                return createCorruptedContent(folder);
            }
        });
        // Make sure the source node is correctly set up before we start
        // It should not be renditioned and should not be marked as having any failed thumbnails.
        assertFalse(secureNodeService.hasAspect(corruptNode, ContentModel.ASPECT_FAILED_THUMBNAIL_SOURCE));

        // Attempt to perform a thumbnail that we know will fail.
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                ThumbnailDefinition thumbnailDef = thumbnailService.getThumbnailRegistry().getThumbnailDefinition("doclib");
                Action createThumbnailAction = ThumbnailHelper.createCreateThumbnailAction(thumbnailDef, services);
                actionService.executeAction(createThumbnailAction, corruptNode, true, true);
                return null;
            }
        });
        // The thumbnail attempt has now failed. But a compensating action should have been scheduled that will mark the
        // source node with a failure aspect. As that is an asynchronous action, we need to wait for that to complete.

        Thread.sleep(3000); // This should be long enough for the compensating action to run.

        final NodeRef failedThumbnailNode = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                assertTrue("corrupt node should have failed thumbnails aspect", secureNodeService.hasAspect(corruptNode, ContentModel.ASPECT_FAILED_THUMBNAIL_SOURCE));

                Map<String, FailedThumbnailInfo> failedThumbnails = thumbnailService.getFailedThumbnails(corruptNode);
                assertEquals("Wrong number of failed thumbnails", 1, failedThumbnails.size());

                assertTrue("Missing QName for failed thumbnail", failedThumbnails.containsKey("doclib"));
                final FailedThumbnailInfo doclibFailureInfo = failedThumbnails.get("doclib");
                assertNotNull("Failure info was null", doclibFailureInfo);

                return doclibFailureInfo.getFailedThumbnailNode();
            }
        });

        assertTrue("Rule must not be executed on document", secureNodeService.hasAspect(corruptNode, ContentModel.ASPECT_GEN_CLASSIFIABLE));
        assertFalse("Rule must not be executed on failed thumbnail", secureNodeService.hasAspect(failedThumbnailNode, ContentModel.ASPECT_GEN_CLASSIFIABLE));
    }

    /**
     * From 4.0.1 we support 'transient' thumbnail failure. This occurs when the {@link ContentTransformer}
     * cannot attempt to perform the transformation for some reason (e.g. process/service unavailable) and wishes
     * to decline the request. Such 'failures' should not lead to the addition of the {@link ContentModel#ASPECT_FAILED_THUMBNAIL_SOURCE}
     * aspect.
     * 
     * @since 4.0.1
     */
    public void testCreateTransientlyFailingThumbnail() throws Exception
    {
        Map<QName, Serializable> props = new HashMap<QName, Serializable>();
        props.put(ContentModel.PROP_NAME, "transientThumbnail.transientThumbnail");
        final NodeRef testNode = this.secureNodeService.createNode(folder, ContentModel.ASSOC_CONTAINS,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "transientThumbnail.transientThumbnail"),
                ContentModel.TYPE_CONTENT, props).getChildRef();
        
        secureNodeService.setProperty(testNode, ContentModel.PROP_CONTENT,
                new ContentData(null, TEST_FAILING_MIME_TYPE, 0L, null));
        // We don't need to write any content into this node, as our test transformer will fail immediately.

        logger.debug("Running failing thumbnail on " + testNode);
        
        // Make sure the source node is correctly set up before we start
        // It should not be renditioned and should not be marked as having any failed thumbnails.
        assertFalse(secureNodeService.hasAspect(testNode, RenditionModel.ASPECT_RENDITIONED));
        assertFalse(secureNodeService.hasAspect(testNode, ContentModel.ASPECT_FAILED_THUMBNAIL_SOURCE));
        
        setComplete();
        endTransaction();

        // Attempt to perform a thumbnail that we know will fail.
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                ThumbnailDefinition thumbnailDef = thumbnailService.getThumbnailRegistry().getThumbnailDefinition("doclib");
                
                Action createThumbnailAction = ThumbnailHelper.createCreateThumbnailAction(thumbnailDef, services);
                actionService.executeAction(createThumbnailAction, testNode, true, true);
                return null;
            }
        });
        // The thumbnail attempt has now failed. But in this case the compensating action should NOT have been scheduled.
        // We'll wait briefly in case it has erroneously been scheduled.
        
        Thread.sleep(3000); // This should be long enough for the compensating action to run - if it has been scheduled, which it shouldn't.

        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                assertFalse("Node should not have renditioned aspect", secureNodeService.hasAspect(testNode, RenditionModel.ASPECT_RENDITIONED));
                assertFalse("Node should not have failed thumbnails aspect", secureNodeService.hasAspect(testNode, ContentModel.ASPECT_FAILED_THUMBNAIL_SOURCE));
              
                return null;
            }
        });
    }
    
    public void testThumbnailUpdate() throws Exception
    {
        checkTransformer();

        // First create a thumbnail
        NodeRef jpgOrig = createOriginalContent(this.folder, MimetypeMap.MIMETYPE_IMAGE_JPEG);
        ImageResizeOptions imageResizeOptions = new ImageResizeOptions();
        imageResizeOptions.setWidth(64);
        imageResizeOptions.setHeight(64);
        imageResizeOptions.setResizeToThumbnail(true);
        ImageTransformationOptions imageTransformationOptions = new ImageTransformationOptions();
        imageTransformationOptions.setResizeOptions(imageResizeOptions);
        NodeRef thumbnail1 = this.thumbnailService.createThumbnail(jpgOrig, ContentModel.PROP_CONTENT,
                    MimetypeMap.MIMETYPE_IMAGE_JPEG, imageTransformationOptions, "small");

        // Thumbnails should always be of type cm:thumbnail.
        assertEquals(ContentModel.TYPE_THUMBNAIL, secureNodeService.getType(thumbnail1));

        // Update the thumbnail
        this.thumbnailService.updateThumbnail(thumbnail1, imageTransformationOptions);
        
        // ALF-2047. Thumbnails were changing to type cm:content after update.
        assertEquals(ContentModel.TYPE_THUMBNAIL, secureNodeService.getType(thumbnail1));
    }

    public void testGetThumbnailByName() throws Exception
    {
        checkTransformer();

        NodeRef jpgOrig = createOriginalContent(this.folder, MimetypeMap.MIMETYPE_IMAGE_JPEG);

        // Check for missing thumbnail
        NodeRef result1 = this.thumbnailService.getThumbnailByName(jpgOrig, ContentModel.PROP_CONTENT, "small");
        assertNull("The thumbnail 'small' should have been missing", result1);

        // Create the thumbnail
        ImageResizeOptions imageResizeOptions = new ImageResizeOptions();
        imageResizeOptions.setWidth(64);
        imageResizeOptions.setHeight(64);
        imageResizeOptions.setResizeToThumbnail(true);
        ImageTransformationOptions imageTransformationOptions = new ImageTransformationOptions();
        imageTransformationOptions.setResizeOptions(imageResizeOptions);
        this.thumbnailService.createThumbnail(jpgOrig, ContentModel.PROP_CONTENT, MimetypeMap.MIMETYPE_IMAGE_JPEG,
                    imageTransformationOptions, "small");

        // Try and retrieve the thumbnail
        NodeRef result2 = this.thumbnailService.getThumbnailByName(jpgOrig, ContentModel.PROP_CONTENT, "small");
        assertNotNull(result2);
        checkRendition("small", result2);

        // Check for an other thumbnail that doesn't exist
        NodeRef result3 = this.thumbnailService.getThumbnailByName(jpgOrig, ContentModel.PROP_CONTENT, "anotherone");
        assertNull("The thumbnail 'anotherone' should have been missing", result3);
    }

    /**
     * See REPO-1580, MNT-17113, REPO-1644 (and related)
     */
    public void testLastThumbnailModificationDataContentUpdates() throws Exception
    {
        NodeRef pdfOrig = createOriginalContent(this.folder, MimetypeMap.MIMETYPE_PDF);
        QName qname = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "doclib");

        ThumbnailDefinition details = thumbnailService.getThumbnailRegistry().getThumbnailDefinition(qname.getLocalName());
        NodeRef thumbnail = this.thumbnailService.createThumbnail(pdfOrig, ContentModel.PROP_CONTENT, MimetypeMap.MIMETYPE_IMAGE_JPEG,
                details.getTransformationOptions(), "doclib");
        assertNotNull(thumbnail);

        setComplete();
        endTransaction();
        
        Thread.sleep(1000);

        // Get initial value of property "Last thumbnail modification data"
        String lastThumbnailDataV1 = ((List<String>) this.secureNodeService.getProperty(pdfOrig, ContentModel.PROP_LAST_THUMBNAIL_MODIFICATION_DATA)).get(0);
        
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                // Update file content
                setNewContent(pdfOrig, "quick-size-limit.", MimetypeMap.MIMETYPE_PDF);
                return null;
            }
        }, false, true);
        
        Thread.sleep(1000);

        // Get modified value of property "Last thumbnail modification data"
        String lastThumbnailDataV2 = ((List<String>) this.secureNodeService.getProperty(pdfOrig, ContentModel.PROP_LAST_THUMBNAIL_MODIFICATION_DATA)).get(0);

        // Check if property "Last thumbnail modification data" has changed
        assertFalse("Property 'Last thumbnail modification data' has not changed", lastThumbnailDataV1.equals(lastThumbnailDataV2));
    }

    /**
     * See REPO-1580, MNT-17113 (and related)
     */
    public void testLockedContent() throws Exception
    {
        NodeRef sharedHomeNodeRef = repositoryHelper.getSharedHome();
        
        String user1 = "bob" + GUID.generate();
        createUser(user1);

        String user2 = "fred" + GUID.generate();
        createUser(user2);
        
        authenticationComponent.setCurrentUser(user1);
        
        NodeRef pdfOrig = createOriginalContent(sharedHomeNodeRef, "testLockedContent-"+GUID.generate(), MimetypeMap.MIMETYPE_PDF);
        
        lockService.lock(pdfOrig, LockType.READ_ONLY_LOCK);

        setComplete();
        endTransaction();
        
        startNewTransaction();

        authenticationComponent.setCurrentUser(user2);
        
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(pdfOrig, PermissionService.READ));
        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(pdfOrig, PermissionService.WRITE));
        
        QName qname = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "doclib");
        
        assertNull(thumbnailService.getThumbnailByName(pdfOrig, ContentModel.PROP_CONTENT, qname.getLocalName()));
        assertNull(secureNodeService.getProperty(pdfOrig, ContentModel.PROP_LAST_THUMBNAIL_MODIFICATION_DATA));
                
        ThumbnailDefinition details = thumbnailService.getThumbnailRegistry().getThumbnailDefinition(qname.getLocalName());
        NodeRef thumbnail = thumbnailService.createThumbnail(pdfOrig, ContentModel.PROP_CONTENT, MimetypeMap.MIMETYPE_IMAGE_JPEG,
                details.getTransformationOptions(), "doclib");
        assertNotNull(thumbnail);

        setComplete();
        endTransaction();

        Thread.sleep(1000);

        startNewTransaction();

        authenticationComponent.setCurrentUser(user1);

        assertNotNull(thumbnailService.getThumbnailByName(pdfOrig, ContentModel.PROP_CONTENT, qname.getLocalName()));
        assertNotNull(secureNodeService.getProperty(pdfOrig, ContentModel.PROP_LAST_THUMBNAIL_MODIFICATION_DATA));
        
        // cleanup

        lockService.unlock(pdfOrig, false);
        secureNodeService.deleteNode(pdfOrig);

        setComplete();
        endTransaction();

        startNewTransaction();
    }

    private void setNewContent(NodeRef noderef, String quickFileName, String mimetype) throws IOException
    {
        String ext = this.mimetypeMap.getExtension(mimetype);
        File origFile = AbstractContentTransformerTest.loadNamedQuickTestFile(quickFileName + ext);

        ContentWriter writer = this.contentService.getWriter(noderef, ContentModel.PROP_CONTENT, true);
        writer.setMimetype(mimetype);
        writer.setEncoding("UTF-8");
        writer.putContent(origFile);
    }
    
    private static class ExpectedAssoc
    {
        private QNamePattern assocTypeQName;
        private String assocName;
        private int count;

        public ExpectedAssoc(QNamePattern assocTypeQName, String assocName, int count)
        {
            super();
            this.assocTypeQName = assocTypeQName;
            this.assocName = assocName;
            this.count = count;
        }

        public QNamePattern getAssocTypeQName()
        {
            return assocTypeQName;
        }

        public String getAssocName()
        {
            return assocName;
        }

        public int getCount()
        {
            return count;
        }

        @Override
        public String toString()
        {
            return "ExpectedAssoc [assocTypeQName=" + assocTypeQName + ", assocName=" + assocName + ", count=" + count
                    + "]";
        }
    }

    private static class ExpectedThumbnail
    {
        private String thumbnailName;

        public static ExpectedThumbnail ignoredName()
        {
            return new ExpectedThumbnail();
        }

        public static ExpectedThumbnail withName(String thumbnailName)
        {
            return new ExpectedThumbnail(thumbnailName);
        }

        public ExpectedThumbnail()
        {
            super();
        }

        public ExpectedThumbnail(String thumbnailName)
        {
            super();
            this.thumbnailName = thumbnailName;
        }

        public String getThumbnailName()
        {
            return thumbnailName;
        }

        @Override
        public String toString()
        {
            return "ExpectedThumbnail [thumbnailName=" + thumbnailName + "]";
        }
    }

    private void checkRenditioned(NodeRef contentNodeRef, List<ExpectedAssoc> expectedAssocs) {
        assertTrue("Renditioned aspect should have been applied",
                this.secureNodeService.hasAspect(contentNodeRef, RenditionModel.ASPECT_RENDITIONED));

        for (ExpectedAssoc expectedAssoc : expectedAssocs) {
            QNamePattern qNamePattern = expectedAssoc.getAssocName() != null
                    ? QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, expectedAssoc.getAssocName()) : null;
            List<ChildAssociationRef> assocs = this.secureNodeService.getChildAssocs(contentNodeRef,
                    expectedAssoc.getAssocTypeQName(), qNamePattern);
            assertNotNull(assocs);
            assertEquals(expectedAssoc + " association count mismatch", expectedAssoc.getCount(), assocs.size());
        }
    }

    private void checkRendition(String thumbnailName, NodeRef thumbnail)
    {
        // Check the thumbnail is of the correct type
        assertTrue("Thumbnail should have been a rendition",
                renditionService.isRendition(thumbnail));

        // Check the name
        if (thumbnailName != null)
        {
            assertEquals(thumbnailName, this.secureNodeService.getProperty(thumbnail, ContentModel.PROP_NAME));
        }

        // Check the content property value
        assertEquals(ContentModel.PROP_CONTENT, this.secureNodeService.getProperty(thumbnail,
                    ContentModel.PROP_CONTENT_PROPERTY_NAME));
        
        // Check the thumbnail is of type cm:thumbnail.
        assertEquals("The thumbnail node should be of type cm:thumbnail!",
                    ContentModel.TYPE_THUMBNAIL, secureNodeService.getType(thumbnail));
        
        // Check the thumbnail name property is correctly set on thumbnail.
        assertEquals( thumbnailName, secureNodeService.getProperty(thumbnail, ContentModel.PROP_THUMBNAIL_NAME));
        
        ContentData thumbnailData = (ContentData) secureNodeService.getProperty(thumbnail, ContentModel.PROP_CONTENT);
        assertNotNull("Thumbnail data was null", thumbnailData);
        assertTrue("Thumbnail data was empty", thumbnailData.getSize() > 0);
    }

    private void outputThumbnailTempContentLocation(NodeRef thumbnail, String ext, String message) throws IOException
    {
        File tempFile = TempFileProvider.createTempFile("thumbnailServiceImplTest", "." + ext);
        ContentReader reader = this.contentService.getReader(thumbnail, ContentModel.PROP_CONTENT);
        reader.getContent(tempFile);
        System.out.println(message + ": " + tempFile.getPath());
    }

    /**
     * This method creates a node under the specified folder whose content is
     * taken from the quick file corresponding to the specified MIME type.
     *
     * @param parentFolder
     * @param mimetype
     * @return
     * @throws IOException
     */
    private NodeRef createOriginalContent(NodeRef parentFolder, String mimetype) throws IOException
    {
        return createOriginalContent(parentFolder, "original", mimetype);
    }

    private NodeRef createOriginalContent(NodeRef parentFolder, String baseName, String mimetype) throws IOException
    {
        String ext = this.mimetypeMap.getExtension(mimetype);
        File origFile = AbstractContentTransformerTest.loadQuickTestFile(ext);

        Map<QName, Serializable> props = new HashMap<QName, Serializable>(1);
        props.put(ContentModel.PROP_NAME, baseName + "." + ext);
        NodeRef node = this.secureNodeService.createNode(parentFolder, ContentModel.ASSOC_CONTAINS,
                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "original." + ext),
                    ContentModel.TYPE_CONTENT, props).getChildRef();

        ContentWriter writer = this.contentService.getWriter(node, ContentModel.PROP_CONTENT, true);
        writer.setMimetype(mimetype);
        writer.setEncoding("UTF-8");
        writer.putContent(origFile);

        return node;
    }
    
    private NodeRef createCorruptedContent(NodeRef parentFolder) throws IOException
    {
        // The below pdf file has been truncated such that it is identifiable as a PDF but otherwise corrupt.
        File corruptPdfFile = AbstractContentTransformerTest.loadNamedQuickTestFile("quickCorrupt.pdf");
        assertNotNull("Failed to load required test file.", corruptPdfFile);

        Map<QName, Serializable> props = new HashMap<QName, Serializable>();
        props.put(ContentModel.PROP_NAME, "corrupt.pdf");
        NodeRef node = this.secureNodeService.createNode(parentFolder, ContentModel.ASSOC_CONTAINS,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "quickCorrupt.pdf"),
                ContentModel.TYPE_CONTENT, props).getChildRef();

        secureNodeService.setProperty(node, ContentModel.PROP_CONTENT, new ContentData(null,
                    MimetypeMap.MIMETYPE_PDF, 0L, null));
        ContentWriter writer = contentService.getWriter(node, ContentModel.PROP_CONTENT, true);
        writer.setMimetype(MimetypeMap.MIMETYPE_PDF);
        writer.setEncoding("UTF-8");
        writer.putContent(corruptPdfFile);
        
        return node;
    }
    
    @SuppressWarnings("deprecation")
    public void testAutoUpdate() throws Exception
    {
        checkTransformer();

        final NodeRef jpgOrig = createOriginalContent(this.folder, MimetypeMap.MIMETYPE_IMAGE_JPEG);

        ThumbnailDefinition details = this.thumbnailService.getThumbnailRegistry().getThumbnailDefinition("medium");
        @SuppressWarnings("unused")
        final NodeRef thumbnail = this.thumbnailService.createThumbnail(jpgOrig, ContentModel.PROP_CONTENT, details
                    .getMimetype(), details.getTransformationOptions(), details.getName());

        setComplete();
        endTransaction();

        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Exception
            {
                String ext = ThumbnailServiceImplTest.this.mimetypeMap.getExtension(MimetypeMap.MIMETYPE_IMAGE_JPEG);
                File origFile = AbstractContentTransformerTest.loadQuickTestFile(ext);

                ContentWriter writer = ThumbnailServiceImplTest.this.contentService.getWriter(jpgOrig,
                            ContentModel.PROP_CONTENT, true);
                writer.putContent(origFile);

                return null;
            }
        });

        // TODO
        // this test should wait for the async action to run .. will need to
        // commit transaction for that thou!

        // Thread.sleep(1000);
    }

    public void testHTMLToImageAndSWF() throws Exception
    {
        NodeRef nodeRef = createOriginalContent(this.folder, MimetypeMap.MIMETYPE_HTML);
        ThumbnailDefinition def = this.thumbnailService.getThumbnailRegistry().getThumbnailDefinition("medium");

        ContentTransformer transformer = this.contentService.getTransformer(null, MimetypeMap.MIMETYPE_HTML, -1, def
                                .getMimetype(), def.getTransformationOptions());
        if (transformer != null)
        {
            NodeRef thumb = this.thumbnailService.createThumbnail(nodeRef, ContentModel.PROP_CONTENT,
                        def.getMimetype(), def.getTransformationOptions(), def.getName());
            assertNotNull(thumb);
            ContentReader reader = this.contentService.getReader(thumb, ContentModel.PROP_CONTENT);
            assertNotNull(reader);
            assertEquals(def.getMimetype(), reader.getMimetype());
            assertTrue(reader.getSize() != 0);
        }

        def = this.thumbnailService.getThumbnailRegistry().getThumbnailDefinition("webpreview");
        if (transformer != null)
        {
            NodeRef thumb = this.thumbnailService.createThumbnail(nodeRef, ContentModel.PROP_CONTENT,
                        def.getMimetype(), def.getTransformationOptions(), def.getName());
            assertNotNull(thumb);
            ContentReader reader = this.contentService.getReader(thumb, ContentModel.PROP_CONTENT);
            assertNotNull(reader);
            assertEquals(def.getMimetype(), reader.getMimetype());
            assertTrue(reader.getSize() != 0);
        }
    }
    
    public void testThumbnailServiceCreateApi() throws Exception
    {
        // Create a second folder
        Map<QName, Serializable> folderProps = new HashMap<QName, Serializable>();
        folderProps.put(ContentModel.PROP_NAME, "otherTestFolder");
        NodeRef otherFolder = this.secureNodeService.createNode(this.rootNodeRef, ContentModel.ASSOC_CHILDREN,
                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "otherTestFolder"), ContentModel.TYPE_FOLDER)
                    .getChildRef();

        checkTransformer();
        NodeRef jpgOrig = createOriginalContent(this.folder, MimetypeMap.MIMETYPE_IMAGE_JPEG);
        
        ImageResizeOptions imageResizeOptions = new ImageResizeOptions();
        imageResizeOptions.setWidth(64);
        imageResizeOptions.setHeight(64);
        imageResizeOptions.setResizeToThumbnail(true);
        ImageTransformationOptions imageTransformationOptions = new ImageTransformationOptions();
        imageTransformationOptions.setResizeOptions(imageResizeOptions);
        
        // Create thumbnail - same MIME type
        NodeRef thumbnail1 = this.thumbnailService.createThumbnail(jpgOrig, ContentModel.PROP_CONTENT,
                MimetypeMap.MIMETYPE_IMAGE_JPEG, imageTransformationOptions, "smallJpeg");
        assertNotNull(thumbnail1);
        checkRenditioned(jpgOrig, 
        		Collections.singletonList(new ExpectedAssoc(RegexQNamePattern.MATCH_ALL, "smallJpeg", 1)));
        checkRendition("smallJpeg", thumbnail1);
        outputThumbnailTempContentLocation(thumbnail1, "jpg", "smallJpeg - 64x64, marked as thumbnail");

        // Create thumbnail - different MIME type
        thumbnail1 = this.thumbnailService.createThumbnail(jpgOrig, ContentModel.PROP_CONTENT,
                MimetypeMap.MIMETYPE_IMAGE_PNG, imageTransformationOptions, "smallPng");
        assertNotNull(thumbnail1);
        checkRenditioned(jpgOrig,
        		Collections.singletonList(new ExpectedAssoc(RegexQNamePattern.MATCH_ALL, "smallPng", 1)));
        checkRendition("smallPng", thumbnail1);
        outputThumbnailTempContentLocation(thumbnail1, "png", "smallPng - 64x64, marked as thumbnail");
        
        // Create thumbnail - different content property
        // TODO
        
        // Create thumbnail - different command options
        // We'll pass illegal command options to ImageMagick in order to trigger an exception
        Exception x = null;
        try
        {
            imageTransformationOptions.setCommandOptions("-noSuchOption");
            thumbnail1 = this.thumbnailService.createThumbnail(jpgOrig, ContentModel.PROP_CONTENT,
                    MimetypeMap.MIMETYPE_IMAGE_PNG, imageTransformationOptions, "smallCO");
        } catch (ContentIOException ciox)
        {
            x = ciox;
            ciox.printStackTrace();
        }
        assertNotNull("Expected exception from ImageMagick due to invalid option", x);
        // Reset the command options
        imageTransformationOptions.setCommandOptions("");
        
        
        // Create thumbnail - different target assoc details
        ThumbnailParentAssociationDetails tpad
            = new ThumbnailParentAssociationDetails(otherFolder,
                    QName.createQName(NamespaceService.RENDITION_MODEL_1_0_URI, "foo"),
                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "bar"));
        thumbnail1 = this.thumbnailService.createThumbnail(jpgOrig, ContentModel.PROP_CONTENT,
                MimetypeMap.MIMETYPE_IMAGE_PNG, imageTransformationOptions, "targetDetails", tpad);
        assertNotNull(thumbnail1);
        checkRenditioned(jpgOrig, 
        		Collections.singletonList(new ExpectedAssoc(RegexQNamePattern.MATCH_ALL, "targetDetails", 1)));
        checkRendition("targetDetails", thumbnail1);
        outputThumbnailTempContentLocation(thumbnail1, "png", "targetDetails - 64x64, marked as thumbnail");

        
        
        // Create thumbnail - null thumbnail name
        thumbnail1 = this.thumbnailService.createThumbnail(jpgOrig, ContentModel.PROP_CONTENT,
                MimetypeMap.MIMETYPE_IMAGE_PNG, imageTransformationOptions, null);
        assertNotNull(thumbnail1);
        checkRenditioned(jpgOrig, 
        		Collections.singletonList(new ExpectedAssoc(RegexQNamePattern.MATCH_ALL, null, 5)));
        checkRendition(null, thumbnail1);
        outputThumbnailTempContentLocation(thumbnail1, "png", "'null' - 64x64, marked as thumbnail");
    }

    public void testRegistry()
    {
        ThumbnailRegistry thumbnailRegistry = this.thumbnailService.getThumbnailRegistry();
        List<ThumbnailDefinition> defs = thumbnailRegistry.getThumbnailDefinitions(MimetypeMap.MIMETYPE_HTML, -1);
        System.out.println("Definitions ...");
        for (ThumbnailDefinition def : defs)
        {
            System.out.println("Thumbnail Available: " + def.getName());
        }
    }

    // == Test the JavaScript API ==

    public void testJSAPI() throws Exception
    {
        NodeRef jpgOrig = createOriginalContent(this.folder, MimetypeMap.MIMETYPE_IMAGE_JPEG);
        NodeRef gifOrig = createOriginalContent(this.folder, MimetypeMap.MIMETYPE_IMAGE_GIF);
        NodeRef pdfOrig = createOriginalContent(this.folder, MimetypeMap.MIMETYPE_PDF);
        NodeRef docOrig = createOriginalContent(this.folder, MimetypeMap.MIMETYPE_WORD);

        Map<String, Object> model = new HashMap<String, Object>(2);
        model.put("jpgOrig", jpgOrig);
        model.put("gifOrig", gifOrig);
        model.put("pdfOrig", pdfOrig);
        model.put("docOrig", docOrig);

        ScriptLocation location = new ClasspathScriptLocation("org/alfresco/repo/thumbnail/script/test_thumbnailAPI.js");
        this.scriptService.executeScript(location, model);
    }

    /**
     * This test method tests the thumbnail placeholders which are handled in the {@link ScriptThumbnailService}.
     * See ALF-6566.
     */
    public void testPlaceHoldersByMimeType() throws Exception
    {
        // Retrieve the classpath paths for all the standard icon resources for doclib.
        
        final String standardDoclibIcon = "alfresco/thumbnail/thumbnail_placeholder_doclib.png";
        
        // This used to be the cogs, but as of ALF-6566 is a generic document icon.
        String doclibIcon = scriptThumbnailService.getPlaceHolderResourcePath("doclib");
        assertEquals(standardDoclibIcon, doclibIcon);
        
        // The same but with explicit null mimetype.
        doclibIcon = scriptThumbnailService.getMimeAwarePlaceHolderResourcePath("doclib", null);
        assertEquals(standardDoclibIcon, doclibIcon);

        // The icon for a .doc mime type - a sample, recognised mime type.
        String docxDoclibIcon = scriptThumbnailService.getMimeAwarePlaceHolderResourcePath("doclib", MimetypeMap.MIMETYPE_WORD);
        assertEquals("alfresco/thumbnail/thumbnail_placeholder_doclib_doc.png", docxDoclibIcon);

        // The icon for an unrecognised mime type.
        String fallbackDoclibIcon = scriptThumbnailService.getMimeAwarePlaceHolderResourcePath("doclib", "application/wibble");
        assertEquals(standardDoclibIcon, fallbackDoclibIcon);
        
        // And one from the 'medium' set.
        String mediumIcon = scriptThumbnailService.getPlaceHolderResourcePath("medium");
        final String standardMediumIcon = "alfresco/thumbnail/thumbnail_placeholder_medium.jpg"; // This one jpg, not png
        assertEquals(standardMediumIcon, mediumIcon);
    }
    
    protected void performLongRunningThumbnailTest(final List<ExpectedThumbnail> expectedThumbnails,
            final List<ExpectedAssoc> expectedAssocs, final LongRunningConcurrentWork concurrentWork,
            final Integer retryPeriod, final Integer quietPeriod) throws Exception
    {
        long saveRetryPeriod = failureHandlingOptions.getRetryPeriod();
        long saveQuietPeriod = failureHandlingOptions.getQuietPeriod();

        try
        {
            // Reset our transformer count for each test
            LongRunningTransformer transformer = (LongRunningTransformer) contentService
                    .getTransformer(TEST_LONG_RUNNING_MIME_TYPE, MimetypeMap.MIMETYPE_IMAGE_JPEG);
            transformer.setTransformCount(0);

            Map<QName, Serializable> props = new HashMap<QName, Serializable>(1);
            props.put(ContentModel.PROP_NAME, "original.test");
            final NodeRef source = secureNodeService.createNode(folder, ContentModel.ASSOC_CONTAINS,
                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "original.test"),
                    ContentModel.TYPE_CONTENT, props).getChildRef();
            ContentWriter writer = contentService.getWriter(source, ContentModel.PROP_CONTENT, true);
            writer.setMimetype(TEST_LONG_RUNNING_MIME_TYPE);
            writer.setEncoding("UTF-8");
            writer.putContent("OrigContent");
            logger.debug("Created source content: " + source);

            long startTime = (new Date()).getTime();

            if (retryPeriod != null) {
                failureHandlingOptions.setRetryPeriod(retryPeriod);
            }

            if (quietPeriod != null) {
                failureHandlingOptions.setQuietPeriod(quietPeriod);
            }

            // Create our thumbnail(s)
            for (ExpectedThumbnail expectedThumbnail : expectedThumbnails) {
                ThumbnailDefinition thumbnailDef = thumbnailService.getThumbnailRegistry()
                        .getThumbnailDefinition(expectedThumbnail.getThumbnailName());

                Action createThumbnailAction = ThumbnailHelper.createCreateThumbnailAction(thumbnailDef, services);

                logger.debug("Creating thumbnail " + expectedThumbnail.getThumbnailName() + " for " + source);
                actionService.executeAction(createThumbnailAction, source, true, true);
            }

            setComplete();
            endTransaction();

            // Thumbnailing process(es) are running in other threads, do the
            // concurrent work here
            if (concurrentWork != null) {
                logger.debug("Starting concurrent work for " + source);
                concurrentWork.run(source);
            }

            // Verify our concurrent work ran successfully
            if (concurrentWork != null) {
                logger.debug("Verifying concurrent work for " + source);
                concurrentWork.verify(source);
            }

            final int numIterations = 20;

            // Wait for thumbnail(s) to finish
            long endTime = (new Date()).getTime();
            for (final ExpectedThumbnail expectedThumbnail : expectedThumbnails) {
                NodeRef thumbnail = null;
                while ((endTime - startTime) < (TEST_LONG_RUNNING_TRANSFORM_TIME * numIterations)) {
                    thumbnail = transactionService.getRetryingTransactionHelper()
                            .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>() {
                                public NodeRef execute() throws Throwable {
                                    return thumbnailService.getThumbnailByName(source, ContentModel.PROP_CONTENT,
                                            expectedThumbnail.getThumbnailName());
                                }
                            }, false, true);
                    if (thumbnail == null) {
                        Thread.sleep(200);
                        logger.debug("Elapsed " + (endTime - startTime) + " ms of "
                                + TEST_LONG_RUNNING_TRANSFORM_TIME * numIterations + " ms waiting for "
                                + expectedThumbnail.getThumbnailName());
                        endTime = (new Date()).getTime();
                    } else {
                        break;
                    }
                }
                assertNotNull("The thumbnail " + expectedThumbnail.getThumbnailName() + " was not generated in time.",
                        thumbnail);
            }

            transactionService.getRetryingTransactionHelper()
                    .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>() {
                        public Void execute() throws Throwable {
                            // Verify that the thumbnail(s) was/were created
                            for (final ExpectedThumbnail expectedThumbnail : expectedThumbnails) {
                                String thumbnailName = expectedThumbnail.getThumbnailName();
                                NodeRef thumbnailNodeRef = thumbnailService.getThumbnailByName(source,
                                        ContentModel.PROP_CONTENT, thumbnailName);
                                checkRendition(thumbnailName, thumbnailNodeRef);
                            }

                            // verify associations
                            checkRenditioned(source, expectedAssocs);

                            return null;
	            };
	        });
        }
        finally
        {
        	failureHandlingOptions.setRetryPeriod(saveRetryPeriod);
        	failureHandlingOptions.setQuietPeriod(saveQuietPeriod);
        }
    }
    
    /**
     * Verifies that our long-running test setup passes with simple behavior of
     * a single thumbnail requested with no other concurrent work.
     * 
     * @throws Exception
     */
    public void testLongRunningThumbnails() throws Exception
    {
        logger.debug("Starting testLongRunningThumbnails");
        performLongRunningThumbnailTest(
        		Collections.singletonList(ExpectedThumbnail.withName("imgpreview")),
        		Collections.singletonList(new ExpectedAssoc(RegexQNamePattern.MATCH_ALL, "imgpreview", 1)),
        		new EmptyLongRunningConcurrentWork(), 60, null);
    }
    
    /**
     * Verifies that a property can be updated on the source node during 
     * a long-running thumbnailing process without affecting the thumbnailing or 
     * property update.
     * 
     * @throws Exception
     * @see <a href="https://issues.alfresco.com/jira/browse/MNT-15135">MNT-15135</a>
     */
    public void testUpdatePropertyDuringLongRunningThumbnail() throws Exception
    {
        //see REPO-1528
        if(shouldTestBeSkippedForCurrentDB())
        {
            return;
        }
        logger.debug("Starting testUpdatePropertyDuringLongRunningThumbnail");
        LongRunningConcurrentWork updatePropertyWork = new LongRunningConcurrentWork()
        {
            @Override
            public void run(NodeRef source) throws Exception
            {
                Thread.sleep(500);  // Wait for transform(s) to get started
                logger.debug("Updating description of a node: " + source);
                secureNodeService.setProperty(source, 
                        ContentModel.PROP_DESCRIPTION, TEST_LONG_RUNNING_PROPERTY_VALUE);
            }
            
            @Override
            public void verify(NodeRef source) throws Exception
            {
                String description = (String) secureNodeService.getProperty(
                        source, ContentModel.PROP_DESCRIPTION);
                assertEquals("The node's property was not updated.", TEST_LONG_RUNNING_PROPERTY_VALUE, description);
            }
        };

        performLongRunningThumbnailTest(
        		Collections.singletonList(ExpectedThumbnail.withName("imgpreview")),
        		Collections.singletonList(new ExpectedAssoc(RegexQNamePattern.MATCH_ALL, "imgpreview", 1)),
        		updatePropertyWork, 1, null);
    }
    
    /**
     * Verifies that multiple thumbnails can be successfully created.
     * 
     * Note: the current architecture of the thumbnail and rendition services can't guarantee that there
     * won't be failed thumbnails for the scenario covered by this test. In particular, given long-running
     * thumbnails/renditions, the concurrent creation of more than one thumbnail may fail with a primary key constraint 
     * exception because both transactions try to add the same aspect to the same parent content node. Whilst
     * the retrying transaction handler correctly handles this scenario, the {@link org.alfresco.service.cmr.action.ActionService} 
     * incorrectly generates a compensating action (failed thumbnail) when in fact the thumbnail creation is recoverable.
     * 
     * @throws Exception
     */
    public void testCreateMultipleLongRunningThumbnails() throws Exception
    {
        logger.debug("Starting testCreateMultipleLongRunningThumbnails");

        // MNT-15135 note: at least one of the thumbnails will fail due to a deadlock/primary key constraint exception.
        // This is expected, given the current structure of the thumbnail/rendition/action code and can't be avoided
        // without major refactoring in the way renditions and thumbnails are architected.
        List<ExpectedThumbnail> expectedThumbnails = new ArrayList<>(2);
        expectedThumbnails.add(ExpectedThumbnail.withName("imgpreview"));
        expectedThumbnails.add(ExpectedThumbnail.withName("avatar"));

        List<ExpectedAssoc> expectedAssocs = new ArrayList<>(5);
        expectedAssocs.add(new ExpectedAssoc(RenditionModel.ASSOC_RENDITION, "imgpreview", 1));
        expectedAssocs.add(new ExpectedAssoc(RenditionModel.ASSOC_RENDITION, "avatar", 1));
//        expectedAssocs.add(new ExpectedAssoc(ContentModel.ASSOC_FAILED_THUMBNAIL, null, 1));

        performLongRunningThumbnailTest(expectedThumbnails, expectedAssocs, new EmptyLongRunningConcurrentWork(), 1, 1);
    }
    
    /**
     * Test transformer.
     * 
     * @since 4.0.1
     */
    private static class TransientFailTransformer extends AbstractContentTransformer2
    {
        public boolean isTransformable(String sourceMimetype, String targetMimetype, TransformationOptions options)
        {
            return sourceMimetype.equals(MimetypeMap.MIMETYPE_PDF) && targetMimetype.equals(TEST_FAILING_MIME_TYPE);
        }
        
        protected void transformInternal(ContentReader reader, ContentWriter writer, TransformationOptions options) throws Exception
        {
            // fail every time.
            throw new ContentServiceTransientException("Transformation intentionally failed for test purposes.");
        }
    }
    
    /**
     * Bogus transformer that simulates a somewhat longer running transformation
     */
    private static class LongRunningTransformer extends AbstractContentTransformer2
    {
        private int transformCount = 0;

        @Override
        public void register()
        {
            super.register();
            setStrictMimeTypeCheck(false);
        }

        public void setTransformCount(int transformCount)
        {
            this.transformCount = transformCount;
        }

        public int getTransformCount()
        {
            return transformCount;
        }
        
        @Override
        public boolean isTransformable(String sourceMimetype, String targetMimetype, TransformationOptions options)
        {
            return sourceMimetype.equals(TEST_LONG_RUNNING_MIME_TYPE) && 
                    (targetMimetype.equals(MimetypeMap.MIMETYPE_IMAGE_JPEG) ||
                            targetMimetype.equals(MimetypeMap.MIMETYPE_IMAGE_PNG));
        }

        @Override
        protected void transformInternal(ContentReader reader, ContentWriter writer, TransformationOptions options)
                throws Exception
        {
            Thread.sleep(TEST_LONG_RUNNING_TRANSFORM_TIME);
            writer.putContent("SUCCESS");
            transformCount++;
        }
    }
    
    /**
     * Defines the work to be done while long running transformations are being performed
     * and the means to verify that work completed successfully.
     * <p>
     * Implementations might update a property then verify the updated value for example.
     */
    private interface LongRunningConcurrentWork
    {
        public void run(NodeRef source) throws Exception;
        public void verify(NodeRef source) throws Exception;
    }
    
    /**
     * Implementation of LongRunningConcurrentWork that does nothing
     */
    private class EmptyLongRunningConcurrentWork implements LongRunningConcurrentWork
    {
        @Override
        public void run(NodeRef source) throws Exception { }
        @Override
        public void verify(NodeRef source) throws Exception { }
    }

    private boolean shouldTestBeSkippedForCurrentDB()
    {
        Dialect dialect = (Dialect) applicationContext.getBean("dialect");
        return dialect instanceof AlfrescoOracle9Dialect 
                || dialect instanceof AlfrescoSQLServerDialect 
                || dialect instanceof DB2Dialect;
    }
}
