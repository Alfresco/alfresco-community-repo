/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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

package org.alfresco.repo.rendition;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.alfresco.model.ContentModel;
import org.alfresco.model.RenditionModel;
import org.alfresco.repo.action.RuntimeActionService;
import org.alfresco.repo.action.executer.ExporterActionExecuter;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.transform.AbstractContentTransformerTest;
import org.alfresco.repo.content.transform.magick.ImageTransformationOptions;
import org.alfresco.repo.jscript.ClasspathScriptLocation;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.rendition.executer.AbstractRenderingEngine;
import org.alfresco.repo.rendition.executer.FreemarkerRenderingEngine;
import org.alfresco.repo.rendition.executer.ImageRenderingEngine;
import org.alfresco.repo.rendition.executer.ReformatRenderingEngine;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.rendition.CompositeRenditionDefinition;
import org.alfresco.service.cmr.rendition.RenderCallback;
import org.alfresco.service.cmr.rendition.RenderingEngineDefinition;
import org.alfresco.service.cmr.rendition.RenditionDefinition;
import org.alfresco.service.cmr.rendition.RenditionService;
import org.alfresco.service.cmr.rendition.RenditionServiceException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.ScriptLocation;
import org.alfresco.service.cmr.repository.ScriptService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.BaseAlfrescoSpringTest;
import org.alfresco.util.Pair;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author Neil McErlean
 * @author Nick Smith
 * @since 3.3
 */
@SuppressWarnings("deprecation")
public class RenditionServiceIntegrationTest extends BaseAlfrescoSpringTest
{
    private static final String WHITE = "ffffff";
    private static final String BLACK = "000000";
    private final static QName REFORMAT_RENDER_DEFN_NAME = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
                ReformatRenderingEngine.NAME + System.currentTimeMillis());
    private final static QName RESCALE_RENDER_DEFN_NAME = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
                ImageRenderingEngine.NAME + System.currentTimeMillis());
    private final static String QUICK_CONTENT = "The quick brown fox jumps over the lazy dog";
    private final static String FM_TEMPLATE = "/org/alfresco/repo/rendition/renditionTestTemplate.ftl";

    private NodeRef nodeWithDocContent;
    private NodeRef nodeWithImageContent;
    private NodeRef nodeWithFreeMarkerContent;
    private NodeRef testTargetFolder;

    private NodeRef renditionNode = null;

    private NamespaceService namespaceService;
    private RenditionService renditionService;
    private Repository repositoryHelper;
    private ScriptService scriptService;
    private RetryingTransactionHelper transactionHelper;
    
    @Override
    protected void onSetUpInTransaction() throws Exception
    {
        super.onSetUpInTransaction();
        this.namespaceService= (NamespaceService) this.applicationContext.getBean("namespaceService");
        this.renditionService = (RenditionService) this.applicationContext.getBean("renditionService");
        this.repositoryHelper = (Repository) this.applicationContext.getBean("repositoryHelper");
        this.scriptService = (ScriptService) this.applicationContext.getBean("scriptService");
        this.transactionHelper = (RetryingTransactionHelper) this.applicationContext
                    .getBean("retryingTransactionHelper");

        // Set the current security context as admin
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        
        // Register our test rendering engine if needed
        DummyHelloWorldRenditionEngine.registerIfNeeded(this.applicationContext);

        // Create test folders
        NodeRef companyHome = this.repositoryHelper.getCompanyHome();

        // Create the test folder used for these tests
        this.testTargetFolder = createNode(companyHome, "testFolder", ContentModel.TYPE_FOLDER);

        // Create the node used as a content supplier for tests
        this.nodeWithDocContent  =  createContentNode(companyHome, "testDocContent");
        
        // Put some known PDF content in it.
        File pdfQuickFile = AbstractContentTransformerTest.loadQuickTestFile("pdf");
        assertNotNull("Failed to load required test file.", pdfQuickFile);

        nodeService.setProperty(nodeWithDocContent, ContentModel.PROP_CONTENT, new ContentData(null,
                    MimetypeMap.MIMETYPE_PDF, 0L, null));
        ContentWriter writer = contentService.getWriter(nodeWithDocContent, ContentModel.PROP_CONTENT, true);
        writer.setMimetype(MimetypeMap.MIMETYPE_PDF);
        writer.setEncoding("UTF-8");
        writer.putContent(pdfQuickFile);
        
        // Put the titled aspect on it - used for testing rendition updates
        Map<QName, Serializable> titledProps = new HashMap<QName, Serializable>();
        titledProps.put(ContentModel.PROP_TITLE, "Original test title");
        titledProps.put(ContentModel.PROP_DESCRIPTION, "Dummy description");
        nodeService.addAspect(nodeWithDocContent, ContentModel.ASPECT_TITLED, titledProps);


        // Create a test image
        this.nodeWithImageContent = createContentNode(companyHome, "testImageNode"); 
        // Stream some well-known image content into the node.       
        URL url = RenditionServiceIntegrationTest.class.getClassLoader().getResource("images/gray21.512.png");
        assertNotNull("url of test image was null", url);
        File imageFile = new File(url.getFile());
        setImageContentOnNode(nodeWithImageContent, MimetypeMap.MIMETYPE_IMAGE_PNG, imageFile);

        // Create a test template node.
        this.nodeWithFreeMarkerContent = createFreeMarkerNode(companyHome);
    }

    private NodeRef createContentNode(NodeRef companyHome, String name)
    {
        return createNode(companyHome, name, ContentModel.TYPE_CONTENT);
    }
    
    private void setImageContentOnNode(NodeRef nodeWithImage, String mimetypeImage, File imageFile)
    {
        assertTrue(imageFile.exists());
        nodeService.setProperty(nodeWithImage, ContentModel.PROP_CONTENT, new ContentData(null,
                    mimetypeImage, 0L, null));
        ContentWriter writer = contentService.getWriter(nodeWithImage, ContentModel.PROP_CONTENT, true);
        writer.setMimetype(mimetypeImage);
        writer.setEncoding("UTF-8");
        writer.putContent(imageFile);
    }

    private NodeRef createFreeMarkerNode(NodeRef companyHome)
    {
        NodeRef fmNode = createContentNode(companyHome, "testFreeMarkerNode");
        nodeService.setProperty(fmNode, ContentModel.PROP_CONTENT, new ContentData(null,
                    MimetypeMap.MIMETYPE_TEXT_PLAIN, 0L, null));

        URL url = getClass().getResource(FM_TEMPLATE);
        assertNotNull("The url is null", url);
        File templateFile = new File(url.getFile());
        assertTrue("The template file does not exist", templateFile.exists());

        ContentWriter fmWriter = contentService.getWriter(fmNode, ContentModel.PROP_CONTENT, true);
        fmWriter.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        fmWriter.setEncoding("UTF-8");
        fmWriter.putContent(templateFile);
        return fmNode;
    }

    @Override
    protected void onTearDownInTransaction() throws Exception
    {
        nodeService.deleteNode(nodeWithImageContent);
        nodeService.deleteNode(nodeWithDocContent);
        nodeService.deleteNode(nodeWithFreeMarkerContent);
        nodeService.deleteNode(testTargetFolder);
    }

    public void testRenderFreeMarkerTemplate() throws Exception
    {
        this.setComplete();
        this.endTransaction();
        final QName renditionName = QName.createQName(NamespaceService.RENDITION_MODEL_1_0_URI,
                    FreemarkerRenderingEngine.NAME);

        this.renditionNode = transactionHelper
                    .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
                    {
                        @Override
                        public NodeRef execute() throws Throwable
                        {
                            // create test model
                            RenditionDefinition definition = renditionService.createRenditionDefinition(renditionName,
                                        FreemarkerRenderingEngine.NAME);
                            definition.setParameterValue(FreemarkerRenderingEngine.PARAM_TEMPLATE_NODE,
                                        nodeWithFreeMarkerContent);
                            ChildAssociationRef renditionAssoc = renditionService
                                        .render(nodeWithDocContent, definition);
                            assertNotNull("The rendition association was null", renditionAssoc);
                            return renditionAssoc.getChildRef();
                        }
                    });

        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                String output = readTextContent(renditionNode);
                assertNotNull("The rendition content was null.", output);
                // check the output contains root node Id as expected.
                assertTrue(output.contains(nodeWithDocContent.getId()));
                return null;
            }
        });
    }
    
    public void testRenderFreeMarkerTemplateOneTransaction() throws Exception
    {
        this.setComplete();
        this.endTransaction();
        final QName renditionName = QName.createQName(NamespaceService.RENDITION_MODEL_1_0_URI,
                    FreemarkerRenderingEngine.NAME);

        this.renditionNode = transactionHelper
                    .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
                    {
                        public NodeRef execute() throws Throwable
                        {
                            // create test model
                            RenditionDefinition definition = renditionService.createRenditionDefinition(renditionName,
                                        FreemarkerRenderingEngine.NAME);
                            definition.setParameterValue(FreemarkerRenderingEngine.PARAM_TEMPLATE_NODE,
                                        nodeWithFreeMarkerContent);
                            ChildAssociationRef renditionAssoc = renditionService
                                        .render(nodeWithDocContent, definition);
                            assertNotNull("The rendition association was null", renditionAssoc);
                            String output = readTextContent(renditionAssoc.getChildRef());
                            assertNotNull("The rendition content was null.", output);
                            // check the output contains root node Id as expected.
                            assertTrue(output.contains(nodeWithDocContent.getId()));
                            return null;
                        }
                    });
    }
    
    public void testRenderFreemarkerTemplatePath() throws Exception
    {
        //TODO displayName paths.
        this.setComplete();
        this.endTransaction();
        final QName renditionName1 = QName.createQName(NamespaceService.RENDITION_MODEL_1_0_URI,
                FreemarkerRenderingEngine.NAME + "_UpdateOnAnyPropChange");
        final QName renditionName2 = QName.createQName(NamespaceService.RENDITION_MODEL_1_0_URI,
                FreemarkerRenderingEngine.NAME + "_UpdateOnContentPropChange");
        
        final Pair<NodeRef, NodeRef> renditions = transactionHelper
            .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Pair<NodeRef, NodeRef>>()
        {
            public Pair<NodeRef, NodeRef> execute() throws Throwable
            {
                ChildAssociationRef parentAssoc = nodeService.getPrimaryParent(nodeWithFreeMarkerContent);
                QName assocName = parentAssoc.getQName();
                String templatePath="/app:company_home/"+assocName.toPrefixString(namespaceService);

                // create test model 1 - rendition to update on any property change
                RenditionDefinition definition1 = renditionService.createRenditionDefinition(renditionName1,
                            FreemarkerRenderingEngine.NAME);
                definition1.setParameterValue(FreemarkerRenderingEngine.PARAM_TEMPLATE_PATH,
                            templatePath);
                definition1.setParameterValue(AbstractRenderingEngine.PARAM_UPDATE_RENDITIONS_ON_ANY_PROPERTY_CHANGE, Boolean.TRUE);

                // create test model 2 - rendition to update on content property change
                RenditionDefinition definition2 = renditionService.createRenditionDefinition(renditionName2,
                            FreemarkerRenderingEngine.NAME);
                definition2.setParameterValue(FreemarkerRenderingEngine.PARAM_TEMPLATE_PATH,
                            templatePath);
                definition2.setParameterValue(AbstractRenderingEngine.PARAM_UPDATE_RENDITIONS_ON_ANY_PROPERTY_CHANGE, Boolean.FALSE);
                
                // We need to save these renditions in order to have them eligible
                // for automatic update.
                if (null == renditionService.loadRenditionDefinition(renditionName1))
                {
                    renditionService.saveRenditionDefinition(definition1);
                }
                if (null == renditionService.loadRenditionDefinition(renditionName2))
                {
                    renditionService.saveRenditionDefinition(definition2);
                }

                ChildAssociationRef renditionAssoc1 = renditionService
                    .render(nodeWithDocContent, definition1);
                assertNotNull("The rendition association was null", renditionAssoc1);
                
                ChildAssociationRef renditionAssoc2 = renditionService
                    .render(nodeWithDocContent, definition2);
                assertNotNull("The rendition association was null", renditionAssoc2);

                Pair<NodeRef, NodeRef> result = new Pair<NodeRef, NodeRef>(renditionAssoc1.getChildRef(), renditionAssoc2.getChildRef());
                return result;
            }
        });

        final String titleInitialValue = "Original test title";
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
                    {
                        public Void execute() throws Throwable
                        {
                            assertEquals("The source node should have title " + titleInitialValue, titleInitialValue,
                                    nodeService.getProperty(nodeWithDocContent, ContentModel.PROP_TITLE));

                            String output1 = readTextContent(renditions.getFirst());
                            assertNotNull("The rendition content was null.", output1);
                            
                            assertRenditionContainsTitle(titleInitialValue,
                                    output1);
                            
                            // check the output contains root node Id as expected.
                            assertTrue(output1.contains(nodeWithDocContent.getId()));
                            
                            String output2 = readTextContent(renditions.getSecond());
                            assertNotNull("The rendition content was null.", output2);
                            
                            assertRenditionContainsTitle(titleInitialValue,
                                    output2);

                            return null;
                        }
                    });

        // Now change some properties on the source node and ensure that the renditions
        // are updated appropriately
        final String updatedTitle = "updatedTitle";
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
                {
                    public Void execute() throws Throwable
                    {
                        nodeService.setProperty(nodeWithDocContent, ContentModel.PROP_TITLE, updatedTitle);
                        return null;
                    }
                });
        // Sleep to let the asynchronous action queue perform the updates to the renditions.
        // TODO Is there a better way?
        Thread.sleep(30000);
        
        // Get the renditions and check their content for the new title
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
                {
                    public Void execute() throws Throwable
                    {
                        String output1 = readTextContent(renditions.getFirst());
                        assertNotNull("The rendition content was null.", output1);
                        
                        assertRenditionContainsTitle(updatedTitle, output1);
                        
                        String output2 = readTextContent(renditions.getSecond());
                        assertNotNull("The rendition content was null.", output2);
                        
                        assertRenditionContainsTitle(titleInitialValue, output2);

                        return null;
                    }
                });
    }

    private void assertRenditionContainsTitle(final String titleValue, String output)
    {
        final String titleMarker = "TestTitle=";
        final String beforeAfterMarker = "xxx";
        int indexOfTitleMarker = output.indexOf(titleMarker);
        int indexOfStartMarker = output.indexOf(beforeAfterMarker, indexOfTitleMarker);
        int indexOfEndMarker = output.indexOf(beforeAfterMarker, indexOfStartMarker + beforeAfterMarker.length());
        final String titleAsTakenFromRendition = output.substring(indexOfStartMarker + beforeAfterMarker.length(), indexOfEndMarker);
        
        assertEquals("The rendition should contain title " + titleValue, titleValue, titleAsTakenFromRendition);
    }

    /**
     * This test method uses the RenditionService to render a test document (of
     * type PDF) into a different format (of type plain_text) and place the
     * rendition under the source node.
     */
    public void testRenderDocumentInAnotherFormatInSitu() throws Exception
    {
        this.setComplete();
        this.endTransaction();

        this.renditionNode = transactionHelper
                    .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
                    {
                        public NodeRef execute() throws Throwable
                        {
                            // Initially the node that provides the content
                            // should not have the rn:renditioned aspect on it.
                            assertFalse("Source node has unexpected renditioned aspect.", nodeService.hasAspect(
                                        nodeWithDocContent, RenditionModel.ASPECT_RENDITIONED));
                            // and no renditions
                            assertTrue("Renditions should have been empty", renditionService.getRenditions(
                                        nodeWithDocContent).isEmpty());
                            assertNull("Renditions should have been null", renditionService.getRenditionByName(
                                        nodeWithDocContent, REFORMAT_RENDER_DEFN_NAME));

                            validateRenderingActionDefinition(ReformatRenderingEngine.NAME);

                            RenditionDefinition action = makeReformatAction(null, MimetypeMap.MIMETYPE_TEXT_PLAIN);

                            // Render the content and put the result underneath
                            // the content node
                            ChildAssociationRef renditionAssoc = renditionService.render(nodeWithDocContent, action);
                            NodeRef rendition = renditionAssoc.getChildRef();
                            assertEquals("The parent node was not correct", nodeWithDocContent, renditionAssoc
                                        .getParentRef());
                            validateRenditionAssociation(renditionAssoc, REFORMAT_RENDER_DEFN_NAME);

                            // The rendition node should have no other
                            // parent-associations - in this case
                            assertEquals("Wrong value for rendition node parent count.",
                                        1, nodeService.getParentAssocs(rendition).size());

                            // Now the source content node should have the
                            // renditioned aspect
                            assertTrue("Source node is missing renditioned aspect.", nodeService.hasAspect(
                                        nodeWithDocContent, RenditionModel.ASPECT_RENDITIONED));
                            // and one rendition

                            assertEquals("Renditions size wrong", 1, renditionService.getRenditions(nodeWithDocContent)
                                        .size());
                            assertNotNull("Renditions should not have been null", renditionService.getRenditionByName(
                                        nodeWithDocContent, REFORMAT_RENDER_DEFN_NAME));

                            return rendition;
                        }
                    });

        // Now in a separate transaction, we'll check that the reformatted
        // content is actually there.
        assertNotNull("The rendition node was null.", renditionNode);

        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                String contentAsString = readTextContent(renditionNode);
                assertTrue("Wrong content in rendition", contentAsString.contains(QUICK_CONTENT));
                return null;
            }
        });
    }

    /**
     * This test method uses the RenditionService to render a test document (of
     * type PDF) into a different format (of type
     * application/x-shockwave-flash).
     */
    public void testRenderPdfDocumentToFlash() throws Exception
    {
        this.setComplete();
        this.endTransaction();

        this.renditionNode = transactionHelper
                    .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
                    {
                        public NodeRef execute() throws Throwable
                        {
                            // Initially the node that provides the content
                            // should not have the rn:renditioned aspect on it.
                            assertFalse("Source node has unexpected renditioned aspect.", nodeService.hasAspect(
                                        nodeWithDocContent, RenditionModel.ASPECT_RENDITIONED));

                            validateRenderingActionDefinition(ReformatRenderingEngine.NAME);

                            RenditionDefinition action = makeReformatAction(null, MimetypeMap.MIMETYPE_FLASH);
                            action.setParameterValue(ReformatRenderingEngine.PARAM_FLASH_VERSION, "9");

                            // Render the content and put the result underneath
                            // the content node
                            ChildAssociationRef renditionAssoc = renditionService.render(nodeWithDocContent, action);

                            assertEquals("The parent node was not correct", nodeWithDocContent, renditionAssoc
                                        .getParentRef());
                            validateRenditionAssociation(renditionAssoc, REFORMAT_RENDER_DEFN_NAME);

                            // The rendition node should have no other
                            // parent-associations - in this case
                            assertEquals("Wrong value for rendition node parent count.", 1, nodeService
                                        .getParentAssocs(renditionAssoc.getChildRef()).size());

                            // Now the source content node should have the
                            // renditioned aspect
                            assertTrue("Source node is missing renditioned aspect.", nodeService.hasAspect(
                                        nodeWithDocContent, RenditionModel.ASPECT_RENDITIONED));
                            return renditionAssoc.getChildRef();
                        }
                    });

        // Now in a separate transaction, we'll check that the reformatted
        // content is actually there.
        assertNotNull("The rendition node was null.", renditionNode);
    }

    public void testCompositeReformatAndResizeRendition() throws Exception
    {
        this.setComplete();
        this.endTransaction();

        final QName renditionName = QName.createQName(NamespaceService.RENDITION_MODEL_1_0_URI, "composite");
        final int newX = 20;
        final int newY = 30;

        renditionNode = transactionHelper.doInTransaction(new RetryingTransactionHelper.//
                    RetryingTransactionCallback<NodeRef>()
                    {
                        public NodeRef execute() throws Throwable
                        {
                            CompositeRenditionDefinition compositeDefinition = makeCompositeReformatAndResizeDefinition(
                                        renditionName, newX, newY);
                            ChildAssociationRef renditionAssoc = renditionService.render(nodeWithDocContent,
                                        compositeDefinition);
                            validateRenditionAssociation(renditionAssoc, renditionName);
                            return renditionAssoc.getChildRef();
                        }

                    });

        transactionHelper.doInTransaction(new RetryingTransactionHelper.//
                    RetryingTransactionCallback<Void>()
                    {
                        public Void execute() throws Throwable
                        {
                            List<ChildAssociationRef> renditions = renditionService.getRenditions(nodeWithDocContent);
                            assertEquals("There should only be one rendition", 1, renditions.size());

                            ChildAssociationRef renditionAssoc = renditions.get(0);
                            assertEquals("The association name should match the composite rendition name",
                                        renditionName, renditionAssoc.getQName());

                            NodeRef rendition = renditionAssoc.getChildRef();
                            ContentReader reader = contentService.getReader(rendition, ContentModel.PROP_CONTENT);
                            assertEquals("The mimetype is wrong", MimetypeMap.MIMETYPE_IMAGE_JPEG, reader.getMimetype());

                            assertNotNull("Reader to rendered image was null", reader);
                            BufferedImage img = ImageIO.read(reader.getContentInputStream());

                            assertEquals("Rendered image had wrong height", newY, img.getHeight());
                            assertEquals("Rendered image had wrong width", newX, img.getWidth());
                            return null;
                        }
                    });

    }

    /**
     * This test method used the RenditionService to render a test document (of
     * type PDF) into a different format (of type plain_text) and place the
     * rendition under the specified folder.
     */
    public void testRenderDocumentInAnotherFormatUnderSpecifiedFolder() throws Exception
    {
        this.setComplete();
        this.endTransaction();

        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                // Initially the node that provides the content should not have
                // the rn:renditioned aspect on it.
                assertFalse("Source node has unexpected renditioned aspect.", nodeService.hasAspect(nodeWithDocContent,
                            RenditionModel.ASPECT_RENDITIONED));

                validateRenderingActionDefinition(ReformatRenderingEngine.NAME);

                // Create the rendering action.
                RenditionDefinition definition = makeReformatAction(ContentModel.TYPE_CONTENT,
                            MimetypeMap.MIMETYPE_TEXT_PLAIN);
                Serializable targetFolderName = nodeService.getProperty(testTargetFolder, ContentModel.PROP_NAME);
                String path = "${companyHome.name}/" + targetFolderName +"/test.txt";
                definition.setParameterValue(RenditionService.PARAM_DESTINATION_PATH_TEMPLATE, path);

                // Perform the action with an explicit destination folder
                ChildAssociationRef renditionAssoc = renditionService.render(nodeWithDocContent, definition);
                NodeRef rendition = renditionAssoc.getChildRef();
                // A secondary parent
                assertEquals("The parent node was not correct", nodeWithDocContent, renditionAssoc.getParentRef());

                validateRenditionAssociation(renditionAssoc, REFORMAT_RENDER_DEFN_NAME);

                // The rendition node should have 2 parents: the containing
                // folder and the source node
                List<ChildAssociationRef> parentAssocs = nodeService.getParentAssocs(rendition);
                assertEquals("Wrong value for rendition node parent count.", 2, parentAssocs.size());

                // Check the parent nodeRefs are correct
                List<NodeRef> parents = new ArrayList<NodeRef>(2);
                parents.add(parentAssocs.get(0).getParentRef());
                parents.add(parentAssocs.get(1).getParentRef());
                assertTrue("Missing containing folder as parent", parents.contains(testTargetFolder));
                assertTrue("Missing source node as parent", parents.contains(nodeWithDocContent));

                // Now the source content node should have the rn:renditioned
                // aspect
                assertTrue("Source node is missing renditioned aspect.", nodeService.hasAspect(nodeWithDocContent,
                            RenditionModel.ASPECT_RENDITIONED));

                return null;
            }
        });
    }

    /**
     * This test method used the RenditionService to render a test image (of
     * type PNG) as a cropped image of the same type.
     */
    @SuppressWarnings("unused")
    public void testRenderCropImage() throws Exception
    {
        this.setComplete();
        this.endTransaction();

        final int originalImageWidth = 512;
        final int originalImageHeight = 512;

        // Create a rendition of an existing image with specified absolute x and
        // y scale.
        final int imageNewXSize = 36;
        final int imageNewYSize = 47;
        final Map<String, Serializable> parameterValues = new HashMap<String, Serializable>();
        parameterValues.put(ImageRenderingEngine.PARAM_CROP_WIDTH, imageNewXSize);
        parameterValues.put(ImageRenderingEngine.PARAM_CROP_HEIGHT, imageNewYSize);

        ImageTransformationOptions imageTransformationOptions = new ImageTransformationOptions();
        final NodeRef newRenditionNode = performImageRendition(parameterValues,nodeWithImageContent);

        // Assert that the rendition is of the correct size and has reasonable
        // content.
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                
                // The rescaled image rendition is a child of the original test
                // node.
                List<ChildAssociationRef> children = nodeService.getChildAssocs(nodeWithImageContent,
                            new RegexQNamePattern(getLongNameWithEscapedBraces(RenditionModel.ASSOC_RENDITION)),
                            new RegexQNamePattern(getLongNameWithEscapedBraces(RESCALE_RENDER_DEFN_NAME)));

                // There should only be one child of the image node: the
                // rendition we've just created.
                assertEquals("Unexpected number of children", 1, children.size());

                NodeRef newImageRendition = children.get(0).getChildRef();
                assertEquals(newRenditionNode, newImageRendition);

                ContentReader reader = contentService.getReader(newImageRendition, ContentModel.PROP_CONTENT);
                assertNotNull("Reader to rendered image was null", reader);
                BufferedImage img = ImageIO.read(reader.getContentInputStream());

                assertEquals("Rendered image had wrong height", imageNewYSize, img.getHeight());
                assertEquals("Rendered image had wrong width", imageNewXSize, img.getWidth());


                ContentReader srcReader = contentService.getReader(nodeWithImageContent, ContentModel.PROP_CONTENT);
                BufferedImage srcImg = ImageIO.read(srcReader.getContentInputStream());

                // The upper left pixel of the image should be pure black.
                int rgbAtTopLeft = img.getRGB(1, 1);
                int expRgbAtTopLeft = img.getRGB(1, 1);
                assertEquals("Incorrect image content.", expRgbAtTopLeft, rgbAtTopLeft);

                // The lower right pixel of the image should be pure white
                int rightIndex = img.getWidth() - 1;
                int bottomIndex = img.getHeight() - 1;
                int rgbAtBottomRight = img.getRGB(rightIndex, bottomIndex);
                int expRgbAtBottomRight = srcImg.getRGB(rightIndex, bottomIndex);
                assertEquals("Incorrect image content.", expRgbAtBottomRight, rgbAtBottomRight);

                return null;
            }
        });

        // Create a rendition of the same image, this time cropping by 50/25%
        parameterValues.clear();
        parameterValues.put(ImageRenderingEngine.PARAM_CROP_WIDTH, 50); // 256 picels
        parameterValues.put(ImageRenderingEngine.PARAM_CROP_HEIGHT, 25); // 128 pixels
        parameterValues.put(ImageRenderingEngine.PARAM_IS_PERCENT_CROP, true);

        final NodeRef secondRenditionNode = performImageRendition(parameterValues,nodeWithImageContent);

        // Assert that the rendition is of the correct size and has reasonable
        // content.
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                // The rescaled image rendition is a child of the original test
                // node.
                List<ChildAssociationRef> children = nodeService.getChildAssocs(nodeWithImageContent,
                            new RegexQNamePattern(getLongNameWithEscapedBraces(RenditionModel.ASSOC_RENDITION)),
                            new RegexQNamePattern(getLongNameWithEscapedBraces(RESCALE_RENDER_DEFN_NAME)));

                // There should only be one child of the image node: the
                // rendition we've just created.
                assertEquals("Unexpected number of children", 1, children.size());

                NodeRef newImageRendition = children.get(0).getChildRef();
                assertEquals(secondRenditionNode, newImageRendition);

                ContentReader srcReader = contentService.getReader(nodeWithImageContent, ContentModel.PROP_CONTENT);
                BufferedImage srcImg = ImageIO.read(srcReader.getContentInputStream());

                ContentReader reader = contentService.getReader(newImageRendition, ContentModel.PROP_CONTENT);
                assertNotNull("Reader to rendered image was null", reader);
                BufferedImage img = ImageIO.read(reader.getContentInputStream());

                assertEquals("Rendered image had wrong height", 128, img.getHeight());
                assertEquals("Rendered image had wrong width", 256, img.getWidth());

                // The upper left pixel of the image should be pure black.
                int rgbAtTopLeft = img.getRGB(1, 1);
                int expRgbAtTopLeft = srcImg.getRGB(1, 1);
                assertEquals("Incorrect image content.", expRgbAtTopLeft, rgbAtTopLeft);

                // The lower right pixel of the image should be pure white
                int widthIndex = img.getWidth() - 1;
                int heightIndex = img.getHeight() - 1;
                int rgbAtBottomRight = img.getRGB(widthIndex, heightIndex);
                int expRgbAtBottomRight = srcImg.getRGB(widthIndex, heightIndex);
                assertEquals("Incorrect image content.", expRgbAtBottomRight, rgbAtBottomRight);

                return null;
            }
        });
    }

    /**
     * This test method used the RenditionService to render a test image (of
     * type PNG) as a rescaled image of the same type.
     */
    public void testRenderRescaledImage() throws Exception
    {
        this.setComplete();
        this.endTransaction();

        final int originalImageWidth = 512;
        final int originalImageHeight = 512;

        // Create a rendition of an existing image with specified absolute x and
        // y scale.
        final Integer imageNewXSize = new Integer(36);
        final Integer imageNewYSize = new Integer(48);
        final Map<String, Serializable> parameterValues = new HashMap<String, Serializable>();
        parameterValues.put(ImageRenderingEngine.PARAM_RESIZE_WIDTH, imageNewXSize);
        parameterValues.put(ImageRenderingEngine.PARAM_RESIZE_HEIGHT, imageNewYSize);

        final NodeRef newRenditionNode = performImageRendition(parameterValues, nodeWithImageContent);

        // Assert that the rendition is of the correct size and has reasonable
        // content.
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                // The rescaled image rendition is a child of the original test
                // node.
                List<ChildAssociationRef> children = nodeService.getChildAssocs(nodeWithImageContent,
                            new RegexQNamePattern(getLongNameWithEscapedBraces(RenditionModel.ASSOC_RENDITION)),
                            new RegexQNamePattern(getLongNameWithEscapedBraces(RESCALE_RENDER_DEFN_NAME)));

                // There should only be one child of the image node: the
                // rendition we've just created.
                assertEquals("Unexpected number of children", 1, children.size());

                NodeRef newImageRendition = children.get(0).getChildRef();
                assertEquals(newRenditionNode, newImageRendition);

                ContentReader reader = contentService.getReader(newImageRendition, ContentModel.PROP_CONTENT);
                assertNotNull("Reader to rendered image was null", reader);
                BufferedImage img = ImageIO.read(reader.getContentInputStream());

                assertEquals("Rendered image had wrong height", imageNewYSize, new Integer(img.getHeight()));
                assertEquals("Rendered image had wrong width", imageNewXSize, new Integer(img.getWidth()));

                // The upper left pixel of the image should be pure black.
                int rgbAtTopLeft = img.getRGB(1, 1);
                assertTrue("Incorrect image content.", Integer.toHexString(rgbAtTopLeft).endsWith(BLACK));

                // The lower right pixel of the image should be pure white
                int rgbAtBottomRight = img.getRGB(img.getWidth() - 1, img.getHeight() - 1);
                assertTrue("Incorrect image content.", Integer.toHexString(rgbAtBottomRight).endsWith(WHITE));

                return null;
            }
        });

        // Create a rendition of the same image, this time rescaling by 200%
        parameterValues.clear();
        parameterValues.put(ImageRenderingEngine.PARAM_RESIZE_WIDTH, 200);
        parameterValues.put(ImageRenderingEngine.PARAM_RESIZE_HEIGHT, 200);
        parameterValues.put(ImageRenderingEngine.PARAM_IS_PERCENT_RESIZE, true);

        final NodeRef secondRenditionNode = performImageRendition(parameterValues, nodeWithImageContent);

        // Assert that the rendition is of the correct size and has reasonable
        // content.
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                // The rescaled image rendition is a child of the original test
                // node.
                List<ChildAssociationRef> children = nodeService.getChildAssocs(nodeWithImageContent,
                            new RegexQNamePattern(getLongNameWithEscapedBraces(RenditionModel.ASSOC_RENDITION)),
                            new RegexQNamePattern(getLongNameWithEscapedBraces(RESCALE_RENDER_DEFN_NAME)));

                // There should only be one child of the image node: the
                // rendition we've just created.
                assertEquals("Unexpected number of children", 1, children.size());

                NodeRef newImageRendition = children.get(0).getChildRef();
                assertEquals(secondRenditionNode, newImageRendition);

                ContentReader reader = contentService.getReader(newImageRendition, ContentModel.PROP_CONTENT);
                assertNotNull("Reader to rendered image was null", reader);
                BufferedImage img = ImageIO.read(reader.getContentInputStream());

                assertEquals("Rendered image had wrong height", originalImageWidth * 2, img.getHeight());
                assertEquals("Rendered image had wrong width", originalImageHeight * 2, img.getWidth());

                // The upper left pixel of the image should be pure black.
                int rgbAtTopLeft = img.getRGB(1, 1);
                assertTrue("Incorrect image content.", Integer.toHexString(rgbAtTopLeft).endsWith(BLACK));

                // The lower right pixel of the image should be pure white
                int rgbAtBottomRight = img.getRGB(img.getWidth() - 1, img.getHeight() - 1);
                assertTrue("Incorrect image content.", Integer.toHexString(rgbAtBottomRight).endsWith(WHITE));

                return null;
            }
        });
    }

    /**
     * Tests that the ReformatActionExecutor can be used to render images into
     * different formats.
     * 
     * @throws Exception
     */
    public void testReformatImage() throws Exception
    {
        setComplete();
        endTransaction();

        this.renditionNode = transactionHelper
                    .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
                    {
                        public NodeRef execute() throws Throwable
                        {
                            // Initially the node that provides the content
                            // should not have the rn:renditioned aspect on it.
                            assertFalse("Source node has unexpected renditioned aspect.", nodeService.hasAspect(
                                        nodeWithImageContent, RenditionModel.ASPECT_RENDITIONED));

                            RenditionDefinition action = makeReformatAction(null, MimetypeMap.MIMETYPE_TEXT_PLAIN);

                            // Set output Mimetype to JPEG.
                            action.setParameterValue(AbstractRenderingEngine.PARAM_MIME_TYPE,
                                        MimetypeMap.MIMETYPE_IMAGE_JPEG);

                            ChildAssociationRef renditionAssoc = renditionService.render(nodeWithImageContent, action);
                            return renditionAssoc.getChildRef();
                        }
                    });
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                ContentReader reader = contentService.getReader(renditionNode, ContentModel.PROP_CONTENT);
                assertNotNull("Reader to rendered image was null", reader);
                assertEquals(MimetypeMap.MIMETYPE_IMAGE_JPEG, reader.getMimetype());

                BufferedImage img = ImageIO.read(reader.getContentInputStream());

                assertEquals("Rendered image had wrong height", 512, img.getHeight());
                assertEquals("Rendered image had wrong width", 512, img.getWidth());

                // The upper left pixel of the image should be pure black.
                int rgbAtTopLeft = img.getRGB(1, 1);
                assertTrue("Incorrect image content.", Integer.toHexString(rgbAtTopLeft).endsWith(BLACK));

                // The lower right pixel of the image should be pure white
                int rgbAtBottomRight = img.getRGB(img.getWidth() - 1, img.getHeight() - 1);
                assertTrue("Incorrect image content.", Integer.toHexString(rgbAtBottomRight).endsWith(WHITE));

                return null;
            }
        });
    }
    
    public void testSuccessfulAsynchronousRendition() throws Exception
    {
        // There are two relevant threads here: the JUnit test thread and the background
        // asynchronousActionExecution thread. It is this second thread that will do
        // the rendering work and I want to make sure that any failures on that thread
        // are returned to the JUnit thread in order to fail the test.
        // I also need to ensure that the asynchronous rendering is complete before
        // asserting anything about the results.
        // The countdown latch below is the mechanism by which the JUnit test code
        // waits for the completion of the other thread.
        final CountDownLatch latch = new CountDownLatch(1);
        final AsyncResultsHolder results = new AsyncResultsHolder();
        final RenderCallback callback = new RenderCallback()
        {
            public void handleFailedRendition(Throwable t)
            {
                results.setMessage("Rendition failed unexpectedly.");
                latch.countDown();
            }
            
            public void handleSuccessfulRendition(
                    ChildAssociationRef primaryParentOfNewRendition)
            {
                results.setAssoc(primaryParentOfNewRendition);
                latch.countDown();
            }
        };

        // We're performing this on a valid piece of content.
        // We expect this to succeed.
        performAsyncRendition(nodeWithImageContent, callback, latch, results);
        
        assertNotNull("ChildAssociationRef was null.", results.getAssoc());
        // We'll simply assert that the association has the correct parent.
        assertEquals(nodeWithImageContent, results.getAssoc().getParentRef());
        assertNull(results.getThrowable());
    }


    /**
     * 
     * @throws Exception
     * @see {@link #testSuccessfulAsynchronousRendition()}
     */
    public void testFailedAsynchronousRendition() throws Exception
    {
        // see comment in method above for explanation of the countdown latch.
        final CountDownLatch latch = new CountDownLatch(1);
        final AsyncResultsHolder results = new AsyncResultsHolder();
        final RenderCallback callback = new RenderCallback()
        {
            public void handleFailedRendition(Throwable t)
            {
                results.setThrowable(t);
                latch.countDown();
            }

            public void handleSuccessfulRendition(
                    ChildAssociationRef primaryParentOfNewRendition)
            {
                results.setMessage("Rendition succeeded unexpectedly.");
                latch.countDown();
            }
        };

        // We're performing the render on an invalid node. We expect this to fail.
        performAsyncRendition(testTargetFolder, callback, latch, results);
        
        assertNull(results.getAssoc());
        assertEquals("Expected a RenditionServiceException", RenditionServiceException.class, results.getThrowable().getClass());
    }

    /**
     * This method performs an asynchronous rendition and calls back the result to the
     * provided callback object. It uses the provided latch to coordinate the JUnit and
     * the asynchronous action thread and uses the provided MutableString to send back
     * any error messages for JUnit reporting.
     * <P/>
     * This method blocks until the action service thread either completes its work
     * or times out.
     * 
     * @param callback
     * @param latch
     * @param failureMessage
     * @param nodeToRender
     * @throws InterruptedException
     */
    private void performAsyncRendition(final NodeRef nodeToRender, final RenderCallback callback, CountDownLatch latch,
            final AsyncResultsHolder failureMessage) throws InterruptedException
    {
        setComplete();
        endTransaction();
        
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                RenditionDefinition action = makeReformatAction(null, MimetypeMap.MIMETYPE_TEXT_PLAIN);
        
                action.setParameterValue(AbstractRenderingEngine.PARAM_MIME_TYPE, MimetypeMap.MIMETYPE_IMAGE_JPEG);
        
                renditionService.render(nodeToRender, action, callback);
                return null;
            }
        });
        
        // Now wait for the actionService thread to complete the rendering.
        // We'll arbitrarily timeout after 30 seconds, which should be plenty for the
        // action to execute.
        boolean endedNormally = latch.await(30, TimeUnit.SECONDS);
        
        String failedString = failureMessage.getValue();
        if (failedString != null)
        {
            fail(failedString);
        }
        if (endedNormally == false)
        {
            fail("ActionService thread took too long to perform rendering.");
        }
    }
    
    /**
     * A simple struct class to allow the return of failure messages, exception objects
     * and ChildAssociationRef results from asynchronous calls.
     */
    private static class AsyncResultsHolder
    {
        private String message;
        private ChildAssociationRef assoc;
        private Throwable throwable;

        public synchronized String getValue()
        {
            return message;
        }
        public synchronized void setMessage(String message)
        {
            this.message = message;
        }
        public synchronized ChildAssociationRef getAssoc()
        {
            return assoc;
        }
        public synchronized void setAssoc(ChildAssociationRef assoc)
        {
            this.assoc = assoc;
        }
        public synchronized Throwable getThrowable()
        {
            return throwable;
        }
        public synchronized void setThrowable(Throwable throwable)
        {
            this.throwable = throwable;
        }
    }
    
    public void testGetRenditionsForNode() throws Exception
    {
        setComplete();
        endTransaction();

        this.renditionNode = transactionHelper
                    .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
                    {
                        public NodeRef execute() throws Throwable
                        {
                            // Initially the node that provides the content
                            // should have no renditions
                            assertTrue("Test node should have no renditions initially", renditionService.getRenditions(
                                        nodeWithImageContent).isEmpty());

                            // Create 4 arbitrary rendition definitions
                            QName rendName1 = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "definition1"
                                        + System.currentTimeMillis());
                            RenditionDefinition action1 = renditionService.createRenditionDefinition(rendName1,
                                        ReformatRenderingEngine.NAME);
                            action1.setParameterValue(AbstractRenderingEngine.PARAM_MIME_TYPE,
                                        MimetypeMap.MIMETYPE_IMAGE_GIF);

                            QName rendName2 = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "definition2"
                                        + System.currentTimeMillis());
                            RenditionDefinition action2 = renditionService.createRenditionDefinition(rendName2,
                                        ReformatRenderingEngine.NAME);
                            action2.setParameterValue(AbstractRenderingEngine.PARAM_MIME_TYPE,
                                        MimetypeMap.MIMETYPE_IMAGE_JPEG);

                            QName rendName3 = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "definition3"
                                        + System.currentTimeMillis());
                            RenditionDefinition action3 = renditionService.createRenditionDefinition(rendName3,
                                        ImageRenderingEngine.NAME);
                            action3.setParameterValue(ImageRenderingEngine.PARAM_RESIZE_WIDTH, 64);
                            action3.setParameterValue(ImageRenderingEngine.PARAM_RESIZE_HEIGHT, 64);
                            action3.setParameterValue(AbstractRenderingEngine.PARAM_MIME_TYPE,
                                        MimetypeMap.MIMETYPE_IMAGE_PNG);

                            // The 4th intentionally reuses the rendition name
                            // from the third
                            QName rendName4 = rendName3;
                            RenditionDefinition action4 = renditionService.createRenditionDefinition(rendName4,
                                        ImageRenderingEngine.NAME);
                            action4.setParameterValue(ImageRenderingEngine.PARAM_RESIZE_WIDTH, 128);
                            action4.setParameterValue(ImageRenderingEngine.PARAM_RESIZE_HEIGHT, 128);
                            action4.setParameterValue(AbstractRenderingEngine.PARAM_MIME_TYPE,
                                        MimetypeMap.MIMETYPE_IMAGE_PNG);

                            // Execute the 4 renditions.
                            ChildAssociationRef createdRendition1 = renditionService.render(nodeWithImageContent,
                                        action1);
                            ChildAssociationRef createdRendition2 = renditionService.render(nodeWithImageContent,
                                        action2);
                            ChildAssociationRef createdRendition3 = renditionService.render(nodeWithImageContent,
                                        action3);
                            ChildAssociationRef createdRendition4 = renditionService.render(nodeWithImageContent,
                                        action4);

                            // Now validate the getRenditions methods
                            List<ChildAssociationRef> allRenditions = renditionService
                                        .getRenditions(nodeWithImageContent);
                            ChildAssociationRef retrievedRendition1 = renditionService.getRenditionByName(
                                        nodeWithImageContent, rendName1);
                            ChildAssociationRef retrievedRendition2 = renditionService.getRenditionByName(
                                        nodeWithImageContent, rendName2);
                            ChildAssociationRef retrievedRendition3 = renditionService.getRenditionByName(
                                        nodeWithImageContent, rendName3);
                            ChildAssociationRef retrievedRendition4 = renditionService.getRenditionByName(
                                        nodeWithImageContent, rendName4);

                            // allRenditions should contain only 3 renditions.
                            // The 4th should have replaced the 3rd.
                            assertEquals(3, allRenditions.size());
                            assertTrue(allRenditions.contains(createdRendition1));
                            assertTrue(allRenditions.contains(createdRendition2));
                            assertTrue(allRenditions.contains(createdRendition4));
                            for (ChildAssociationRef rendition : allRenditions)
                            {
                                assertNotSame(createdRendition3, rendition);
                            }

                            assertEquals(createdRendition1, retrievedRendition1);
                            assertEquals(createdRendition2, retrievedRendition2);
                            assertEquals(createdRendition4, retrievedRendition3);
                            assertEquals(createdRendition4, retrievedRendition4);

                            // CMIS-style filters. image/*
                            List<ChildAssociationRef> imageRenditions = renditionService.getRenditions(
                                        nodeWithImageContent, "image");
                            assertEquals(3, imageRenditions.size());

                            List<ChildAssociationRef> imageSlashJRenditions = renditionService.getRenditions(
                                        nodeWithImageContent, "image/j");
                            assertEquals(1, imageSlashJRenditions.size());

                            return null;
                        }
                    });
    }

    protected NodeRef performImageRendition(final Map<String, Serializable> parameterValues, final NodeRef imageToRender)
    {
        final NodeRef newRenditionNode = transactionHelper
                    .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
                    {
                        public NodeRef execute() throws Throwable
                        {
                            validateRenderingActionDefinition(ImageRenderingEngine.NAME);

                            // Create the rendering action.
                            RenditionDefinition action = renditionService.createRenditionDefinition(
                                        RESCALE_RENDER_DEFN_NAME, ImageRenderingEngine.NAME);

                            // Set the parameters. Can't call
                            // action.setParameterValues as we don't want
                            // to obliterate existing parameters such as the
                            // name
                            for (String s : parameterValues.keySet())
                            {
                                action.setParameterValue(s, parameterValues.get(s));
                            }

                            ChildAssociationRef renditionAssoc = renditionService.render(imageToRender, action);

                            validateRenditionAssociation(renditionAssoc, RESCALE_RENDER_DEFN_NAME);

                            return renditionAssoc.getChildRef();
                        }
                    });
        return newRenditionNode;
    }

    /**
     * Checks that the saveRenderingAction Method creates the proper node in the
     * repository.
     */
    public void testSaveRenderingAction() throws Exception
    {
        this.setComplete();
        this.endTransaction();

        final List<RenditionDefinition> savedRenditionsToDelete = new ArrayList<RenditionDefinition>();
        try
        {
            // Check that if no node exists already then a new node is created.
            transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
            {
                public Void execute() throws Throwable
                {
                    assertTrue("The rendering action space has not been created in the repository!",//
                                nodeService.exists(RenditionDefinitionPersisterImpl.RENDERING_ACTION_ROOT_NODE_REF));
                    List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(//
                                RenditionDefinitionPersisterImpl.RENDERING_ACTION_ROOT_NODE_REF,//
                                ContentModel.ASSOC_CONTAINS, REFORMAT_RENDER_DEFN_NAME);
                    assertTrue("There should be no persisted rendering actions of name: " + REFORMAT_RENDER_DEFN_NAME
                                + " at the start of this test!",//
                                childAssocs.isEmpty());
                    RenditionDefinition action = makeReformatAction(null, MimetypeMap.MIMETYPE_TEXT_PLAIN);
                    savedRenditionsToDelete.add(action);

                    renditionService.saveRenditionDefinition(action);
                    List<ChildAssociationRef> results = nodeService.getChildAssocs(//
                                RenditionDefinitionPersisterImpl.RENDERING_ACTION_ROOT_NODE_REF,//
                                ContentModel.ASSOC_CONTAINS, REFORMAT_RENDER_DEFN_NAME);
                    assertEquals(
                                "There should be one persisted rendering action of name: " + REFORMAT_RENDER_DEFN_NAME,//
                                1, results.size());
                    return null;
                }
            });

            // Check that if a node already exists then that node is updated.
            transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
            {
                public Void execute() throws Throwable
                {
                    List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(//
                                RenditionDefinitionPersisterImpl.RENDERING_ACTION_ROOT_NODE_REF,//
                                ContentModel.ASSOC_CONTAINS, REFORMAT_RENDER_DEFN_NAME);
                    assertEquals(
                                "There should be one persisted rendering action of name: " + REFORMAT_RENDER_DEFN_NAME,//
                                1, childAssocs.size());
                    NodeRef actionNode = childAssocs.get(0).getChildRef();

                    RenditionDefinition action = makeReformatAction(null, MimetypeMap.MIMETYPE_TEXT_PLAIN);
                    savedRenditionsToDelete.add(action);

                    renditionService.saveRenditionDefinition(action);
                    List<ChildAssociationRef> results = nodeService.getChildAssocs(//
                                RenditionDefinitionPersisterImpl.RENDERING_ACTION_ROOT_NODE_REF,//
                                ContentModel.ASSOC_CONTAINS, REFORMAT_RENDER_DEFN_NAME);
                    assertEquals(
                                "There should be one persisted rendering action of name: " + REFORMAT_RENDER_DEFN_NAME,//
                                1, results.size());
                    assertEquals("The node in which the action is stored should be the same.",//
                                actionNode, results.get(0).getChildRef());
                    return null;
                }
            });
        }
        finally
        {
            cleanUpPersistedActions(savedRenditionsToDelete);
        }
    }

    /**
     * This is not a real test method. It is used to create the ACP file that we have
     * added to the RenditionService for importing at startup.
     * This method should remain here as it will be needed if we need to change the
     * contents of the ACP.
     * 
     * @throws Exception
     * @deprecated
     */
    public void off_test_CleanPersistedRenditionsAndCreateExportedACP() throws Exception
    {
        this.setComplete();
        this.endTransaction();

        // Check that if no node exists already then a new node is created.
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                // Delete all renditionDefinitions and the folder that holds
                // them.
                // We can't delete and recreate the
                // RENDERING_ACTION_ROOT_NODE_REF and recreate it
                // here because we need to keep its well-known nodeRef id.

                assertTrue("The rendering action space has not been created in the repository!",//
                            nodeService.exists(RenditionDefinitionPersisterImpl.RENDERING_ACTION_ROOT_NODE_REF));

                // Clean up all existing saved actions here so as to ensure a
                // clean export.
                // Also delete any old acp file
                for (ChildAssociationRef chRef : nodeService
                            .getChildAssocs(RenditionDefinitionPersisterImpl.RENDERING_ACTION_ROOT_NODE_REF))
                {
                    System.out.println("Deleting rendition Definition "
                                + nodeService.getProperty(chRef.getChildRef(), ContentModel.PROP_NAME));
                    nodeService.deleteNode(chRef.getChildRef());
                }

                // Create the rendition definitions.

                // 1. "medium"
                Map<String, Serializable> parameterValues = new HashMap<String, Serializable>();
                parameterValues.put(AbstractRenderingEngine.PARAM_MIME_TYPE, MimetypeMap.MIMETYPE_IMAGE_JPEG);
                parameterValues.put(ImageRenderingEngine.PARAM_RESIZE_WIDTH, 100);
                parameterValues.put(ImageRenderingEngine.PARAM_RESIZE_HEIGHT, 100);
                parameterValues.put(ImageRenderingEngine.PARAM_MAINTAIN_ASPECT_RATIO, true);
                parameterValues.put(ImageRenderingEngine.PARAM_RESIZE_TO_THUMBNAIL, true);
                parameterValues.put(AbstractRenderingEngine.PARAM_PLACEHOLDER_RESOURCE_PATH,
                            "alfresco/thumbnail/thumbnail_placeholder_medium.jpg");
                parameterValues.put(AbstractRenderingEngine.PARAM_RUN_AS, AuthenticationUtil.getSystemUserName());

                RenditionDefinition action = createAction(ImageRenderingEngine.NAME, "medium", parameterValues);
                renditionService.saveRenditionDefinition(action);

                // 2. "doclib"
                parameterValues.clear();
                parameterValues.put(AbstractRenderingEngine.PARAM_MIME_TYPE, MimetypeMap.MIMETYPE_IMAGE_PNG);
                parameterValues.put(ImageRenderingEngine.PARAM_RESIZE_WIDTH, 100);
                parameterValues.put(ImageRenderingEngine.PARAM_RESIZE_HEIGHT, 100);
                parameterValues.put(ImageRenderingEngine.PARAM_MAINTAIN_ASPECT_RATIO, true);
                parameterValues.put(ImageRenderingEngine.PARAM_RESIZE_TO_THUMBNAIL, true);
                parameterValues.put(ImageRenderingEngine.PARAM_ALLOW_ENLARGEMENT, false);
                parameterValues.put(AbstractRenderingEngine.PARAM_PLACEHOLDER_RESOURCE_PATH,
                            "alfresco/thumbnail/thumbnail_placeholder_doclib.png");
                parameterValues.put(AbstractRenderingEngine.PARAM_RUN_AS, AuthenticationUtil.getSystemUserName());

                action = createAction(ImageRenderingEngine.NAME, "doclib", parameterValues);
                renditionService.saveRenditionDefinition(action);

                // 3. "webpreview"
                parameterValues.clear();
                parameterValues.put(AbstractRenderingEngine.PARAM_MIME_TYPE, MimetypeMap.MIMETYPE_FLASH);
                parameterValues.put(ReformatRenderingEngine.PARAM_FLASH_VERSION, "9");
                parameterValues.put(AbstractRenderingEngine.PARAM_RUN_AS, AuthenticationUtil.getSystemUserName());
                // no placeholder

                action = createAction("reformat", "webpreview", parameterValues);
                renditionService.saveRenditionDefinition(action);

                // 4. "imgpreview"
                parameterValues.clear();
                parameterValues.put(AbstractRenderingEngine.PARAM_MIME_TYPE, MimetypeMap.MIMETYPE_IMAGE_PNG);
                parameterValues.put(ImageRenderingEngine.PARAM_RESIZE_WIDTH, 480);
                parameterValues.put(ImageRenderingEngine.PARAM_RESIZE_HEIGHT, 480);
                parameterValues.put(ImageRenderingEngine.PARAM_MAINTAIN_ASPECT_RATIO, true);
                parameterValues.put(ImageRenderingEngine.PARAM_RESIZE_TO_THUMBNAIL, true);
                parameterValues.put(ImageRenderingEngine.PARAM_ALLOW_ENLARGEMENT, false);
                parameterValues.put(AbstractRenderingEngine.PARAM_PLACEHOLDER_RESOURCE_PATH,
                            "alfresco/thumbnail/thumbnail_placeholder_imgpreview.png");
                parameterValues.put(AbstractRenderingEngine.PARAM_RUN_AS, AuthenticationUtil.getSystemUserName());

                action = createAction(ImageRenderingEngine.NAME, "imgpreview", parameterValues);
                renditionService.saveRenditionDefinition(action);

                // 5. "avatar"
                parameterValues.clear();
                parameterValues.put(AbstractRenderingEngine.PARAM_MIME_TYPE, MimetypeMap.MIMETYPE_IMAGE_PNG);
                parameterValues.put(ImageRenderingEngine.PARAM_RESIZE_WIDTH, 64);
                parameterValues.put(ImageRenderingEngine.PARAM_RESIZE_HEIGHT, 64);
                parameterValues.put(ImageRenderingEngine.PARAM_MAINTAIN_ASPECT_RATIO, true);
                parameterValues.put(ImageRenderingEngine.PARAM_RESIZE_TO_THUMBNAIL, true);
                parameterValues.put(AbstractRenderingEngine.PARAM_PLACEHOLDER_RESOURCE_PATH,
                            "alfresco/thumbnail/thumbnail_placeholder_avatar.png");
                parameterValues.put(AbstractRenderingEngine.PARAM_RUN_AS, AuthenticationUtil.getSystemUserName());

                action = createAction(ImageRenderingEngine.NAME, "avatar", parameterValues);
                renditionService.saveRenditionDefinition(action);
                return null;
            }

            private RenditionDefinition createAction(String renderingEngineName, String renditionLocalName,
                        Map<String, Serializable> parameterValues)
            {
                QName renditionName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, renditionLocalName);
                RenditionDefinition action = renditionService.createRenditionDefinition(renditionName,
                            renderingEngineName);
                for (String paramKey : parameterValues.keySet())
                {
                    action.setParameterValue(paramKey, parameterValues.get(paramKey));
                }
                return action;
            }
        });

        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                Action exportAction = actionService.createAction("export");
                exportAction.setParameterValue(ExporterActionExecuter.PARAM_STORE,
                            StoreRef.STORE_REF_WORKSPACE_SPACESSTORE.toString());
                exportAction.setParameterValue(ExporterActionExecuter.PARAM_PACKAGE_NAME, "systemRenditionDefinitions");
                exportAction.setParameterValue(ExporterActionExecuter.PARAM_ENCODING, "UTF-8");
                exportAction.setParameterValue(ExporterActionExecuter.PARAM_DESTINATION_FOLDER,
                        RenditionDefinitionPersisterImpl.RENDERING_ACTION_ROOT_NODE_REF);
                exportAction.setParameterValue(ExporterActionExecuter.PARAM_INCLUDE_SELF, true);
                exportAction.setParameterValue(ExporterActionExecuter.PARAM_INCLUDE_CHILDREN, true);

                actionService.executeAction(exportAction, RenditionDefinitionPersisterImpl.RENDERING_ACTION_ROOT_NODE_REF);

                return null;
            }
        });

        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                List<ChildAssociationRef> children = nodeService
                            .getChildAssocs(RenditionDefinitionPersisterImpl.RENDERING_ACTION_ROOT_NODE_REF);
                for (ChildAssociationRef car : children)
                {
                    System.err.println(car.getChildRef() + "  "
                                + nodeService.getProperty(car.getChildRef(), ContentModel.PROP_NAME));
                }

                return null;
            }
        });
    }

    /**
     * This test method saves one RenderingAction to the repository and loads it
     * back up asserting that it is equivalent to the original. It then saves
     * some further RenderingActions, loads them back with a RenderingEngine
     * filter and asserts that the results are correct.
     * 
     * @throws Exception
     */
    public void testLoadRenderingAction() throws Exception
    {
        this.setComplete();
        this.endTransaction();

        final RenditionDefinition reformatAction = makeReformatAction(null, MimetypeMap.MIMETYPE_TEXT_PLAIN);
        final RenditionDefinition rescaleAction = makeRescaleImageAction();

        final List<RenditionDefinition> savedRenditionDefinitions = new ArrayList<RenditionDefinition>();
        try
        {
            // First save one action.
            transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
            {
                public Void execute() throws Throwable
                {
                    savedRenditionDefinitions.add(reformatAction);
                    renditionService.saveRenditionDefinition(reformatAction);
                    return null;
                }
            });

            // Then load the action back up and check it matches the original.
            transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
            {
                public Void execute() throws Throwable
                {
                    QName renditionName = reformatAction.getRenditionName();
                    RenditionDefinition result = renditionService.loadRenditionDefinition(renditionName);
                    assertEquals(renditionName, result.getRenditionName());
                    assertEquals(reformatAction.getActionDefinitionName(), result.getActionDefinitionName());
                    assertEquals(reformatAction.getCompensatingAction(), result.getCompensatingAction());
                    assertEquals(reformatAction.getDescription(), result.getDescription());
                    assertEquals(reformatAction.getExecuteAsychronously(), result.getExecuteAsychronously());
                    assertEquals(reformatAction.getModifiedDate(), result.getModifiedDate());
                    assertEquals(reformatAction.getModifier(), result.getModifier());
                    assertEquals(reformatAction.getTitle(), result.getTitle());

                    return null;
                }
            });

            // Now save the second action.
            transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
            {
                public Void execute() throws Throwable
                {
                    savedRenditionDefinitions.add(rescaleAction);
                    renditionService.saveRenditionDefinition(rescaleAction);
                    return null;
                }
            });

            // Then load the actions back up and check they exist and are
            // filtered correctly.
            transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
            {
                public Void execute() throws Throwable
                {
                    // Retrieve persisted Rendering Definitions by name.
                    RenditionDefinition firstLoadedAction = renditionService.loadRenditionDefinition(reformatAction
                                .getRenditionName());
                    RenditionDefinition secondLoadedAction = renditionService.loadRenditionDefinition(rescaleAction
                                .getRenditionName());
                    assertNotNull("The reformat action was null.", firstLoadedAction);
                    assertNotNull("The rescale action was null.", secondLoadedAction);

                    /*
                     * We'll not retest the action metadata here as that is
                     * tested in this method above. Just the names to ensure
                     * they are distinct.
                     */
                    assertEquals(reformatAction.getRenditionName(), firstLoadedAction.getRenditionName());
                    assertEquals(rescaleAction.getRenditionName(), secondLoadedAction.getRenditionName());

                    // Retrieve all rendering definitions
                    // List<RenditionDefinition> renderingDefinitions =
                    // renditionService.loadRenditionDefinitions();
                    // assertEquals("Wrong number of 'all actions'.", 2,
                    // renderingDefinitions.size());

                    // Retrieve all rendering definitions for a given Rendering
                    // Engine.
                    // List<RenditionDefinition> renderDefinitionsForReformat =
                    // renditionService
                    // .loadRenditionDefinitions("reformat");
                    // assertEquals("Wrong number of actions for engine name.",
                    // 1, renderDefinitionsForReformat.size());

                    return null;
                }
            });
        }
        finally
        {
            cleanUpPersistedActions(savedRenditionDefinitions);
        }
    }

    public void testSaveAndLoadCompositeRenditionDefinition() throws Exception
    {
        this.setComplete();
        this.endTransaction();

        final QName renditionName = QName.createQName(NamespaceService.RENDITION_MODEL_1_0_URI, "composite");
        final CompositeRenditionDefinition compositeDefinition = makeCompositeReformatAndResizeDefinition(
                    renditionName, 20, 30);
        final List<RenditionDefinition> savedRenditionDefinitions = new ArrayList<RenditionDefinition>();

        try
        {
            // First save composite rendition definition.
            transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
            {
                public Void execute() throws Throwable
                {
                    renditionService.saveRenditionDefinition(compositeDefinition);
                    return null;
                }
            });

            // Then load the definition back up and check it matches the
            // original.
            transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
            {
                public Void execute() throws Throwable
                {
                    RenditionDefinition result = renditionService.loadRenditionDefinition(renditionName);

                    // Check basic rendition definition properties.
                    assertEquals(renditionName, result.getRenditionName());
                    assertEquals(compositeDefinition.getActionDefinitionName(), result.getActionDefinitionName());
                    assertEquals(compositeDefinition.getCompensatingAction(), result.getCompensatingAction());
                    assertEquals(compositeDefinition.getDescription(), result.getDescription());
                    assertEquals(compositeDefinition.getExecuteAsychronously(), result.getExecuteAsychronously());
                    assertEquals(compositeDefinition.getModifiedDate(), result.getModifiedDate());
                    assertEquals(compositeDefinition.getModifier(), result.getModifier());
                    assertEquals(compositeDefinition.getTitle(), result.getTitle());

                    // Check sub-actions.
                    if (result instanceof CompositeRenditionDefinition)
                    {
                        CompositeRenditionDefinition compositeResult = (CompositeRenditionDefinition) result;
                        savedRenditionDefinitions.add(compositeResult);

                        List<RenditionDefinition> subDefinitions = compositeResult.getActions();
                        assertEquals(2, subDefinitions.size());

                        // Check the first sub-definition is correct.
                        RenditionDefinition firstDef = subDefinitions.get(0);
                        assertEquals(ReformatRenderingEngine.NAME, firstDef.getActionDefinitionName());

                        // Check the second sub-definition is correct.
                        RenditionDefinition secondDef = subDefinitions.get(1);
                        assertEquals(ImageRenderingEngine.NAME, secondDef.getActionDefinitionName());
                    }
                    else
                        fail("The retrieved rendition should be a CompositeRenditionDefinition.");
                    return null;
                }
            });
        }
        finally
        {
            cleanUpPersistedActions(savedRenditionDefinitions);
        }

    }

    /**
     * This method deletes the specified saved action nodes.
     */
    private void cleanUpPersistedActions(final List<RenditionDefinition> savedRenditionDefinitions)
    {
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                for (RenditionDefinition savedRenditionDefinition : savedRenditionDefinitions)
                {
                    NodeRef actionNodeRef = savedRenditionDefinition.getNodeRef();
                    if (actionNodeRef != null)
                    {
                        nodeService.deleteNode(actionNodeRef);
                    }
                }
                return null;
            }
        });
    }

    /**
     * This test method ensures that all the 'built-in' renditionDefinitions are
     * available after startup and that their configuration is correct.
     * 
     * @throws Exception
     */
    public void testBuiltinRenditionDefinitions() throws Exception
    {
        final RenditionDefinition mediumRenditionDef = loadAndValidateRenditionDefinition("medium");
        final RenditionDefinition doclibRenditionDef = loadAndValidateRenditionDefinition("doclib");
        final RenditionDefinition imgpreviewRenditionDef = loadAndValidateRenditionDefinition("imgpreview");
        final RenditionDefinition webpreviewRenditionDef = loadAndValidateRenditionDefinition("webpreview");
        final RenditionDefinition avatarRenditionDef = loadAndValidateRenditionDefinition("avatar");

        assertEquals(MimetypeMap.MIMETYPE_IMAGE_JPEG, mediumRenditionDef.getParameterValue(AbstractRenderingEngine.PARAM_MIME_TYPE));
        assertEquals(MimetypeMap.MIMETYPE_IMAGE_PNG, doclibRenditionDef.getParameterValue(AbstractRenderingEngine.PARAM_MIME_TYPE));
        assertEquals(MimetypeMap.MIMETYPE_IMAGE_PNG, imgpreviewRenditionDef.getParameterValue(AbstractRenderingEngine.PARAM_MIME_TYPE));
        assertEquals(MimetypeMap.MIMETYPE_FLASH, webpreviewRenditionDef.getParameterValue(AbstractRenderingEngine.PARAM_MIME_TYPE));
        assertEquals(MimetypeMap.MIMETYPE_IMAGE_PNG, avatarRenditionDef.getParameterValue(AbstractRenderingEngine.PARAM_MIME_TYPE));
    }
    
    /**
     * This test checks that for a node with an existing rendition, that if you update its content with content
     * that cannot be renditioned (thumbnailed), that existing rendition nodes for failed re-renditions are removed.
     * See ALF-6730.
     * 
     * @since 3.4.2
     */
    public void testRenderValidContentThenUpdateToInvalidContent() throws Exception
    {
        setComplete();
        endTransaction();
        
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                // Need to use one of the standard, persisted rendition definitions, as non-persisted rendition definitions
                // do not get automatcially updated.
                RenditionDefinition doclib = loadAndValidateRenditionDefinition("doclib");

                // We want the rendition to be asynchronous, but we don't care about called-back data.
                RenderCallback dummyCallback = new RenderCallback(){
                    @Override
                    public void handleFailedRendition(Throwable t)
                    {
                    }
                    @Override
                    public void handleSuccessfulRendition(
                            ChildAssociationRef primaryParentOfNewRendition)
                    {
                    }};
                renditionService.render(nodeWithDocContent, doclib, dummyCallback);
                return null;
            }
        });
        
        // Now wait for the actionService thread to complete the rendering.
        Thread.sleep(5000);

        // The rendition should have succeeded.
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
                {
                    public Void execute() throws Throwable
                    {
                        List<ChildAssociationRef> renditions = renditionService.getRenditions(nodeWithDocContent);
                        assertNotNull("Renditions missing.", renditions);
                        assertEquals("Wrong rendition count", 1, renditions.size());
                        return null;
                    }
                });
        
        // Now update the content to a corrupt PDF file that cannot be rendered.
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
                {
                    public Void execute() throws Throwable
                    {
                        ContentWriter writer = contentService.getWriter(nodeWithDocContent, ContentModel.PROP_CONTENT, true);
                        writer.setMimetype(MimetypeMap.MIMETYPE_PDF);
                        writer.setEncoding("UTF-8");
                        
                        InputStream corruptIn = applicationContext.getResource("classpath:/quick/quickCorrupt.pdf").getInputStream();
                        writer.putContent(corruptIn);
                        corruptIn.close();
                        
                        return null;
                    }
                });
        
        // Now wait for the actionService thread to complete the rendition removal.
        Thread.sleep(5000);

        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
                {
                    public Void execute() throws Throwable
                    {
                        List<ChildAssociationRef> renditions = renditionService.getRenditions(nodeWithDocContent);
                        assertNotNull("Renditions missing.", renditions);
                        assertEquals("Wrong rendition count", 0, renditions.size());
                        return null;
                    }
                });
    }

    
    public void testALF3733() throws Exception
    {
    	// ALF-3733 was caused by ${cwd} evaluating to the empty string and a path "//sourceNodeName"
    	// being passed to the FileFolderService for creation. This then splits the string using '/' as
    	// a delimiter which leads to the attempted creation of nodes with the empty string as a name,
    	// which is illegal.
        QName renditionDefName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "myDummyRendition");
    	String renderingEngineName = "imageRenderingEngine";
    			
    	RenditionDefinition renditionDef = renditionService.createRenditionDefinition(renditionDefName, renderingEngineName);
    	Map<String, Serializable> params = new HashMap<String, Serializable>();
    	params.put(ImageRenderingEngine.PARAM_RESIZE_WIDTH, 99);
    	params.put(ImageRenderingEngine.PARAM_RESIZE_HEIGHT, 99);
    	params.put(RenditionService.PARAM_DESTINATION_PATH_TEMPLATE, "${cwd}/resized/${name}_Thumb.${extension}");
    	renditionDef.addParameterValues(params);
    	
    	ChildAssociationRef rendition = renditionService.render(this.nodeWithImageContent, renditionDef);

        assertNotNull("rendition was null.", rendition);
        assertTrue(nodeService.hasAspect(rendition.getChildRef(), RenditionModel.ASPECT_VISIBLE_RENDITION));
        
        // The rendition should have 2 parents.
        List<ChildAssociationRef> allParents = nodeService.getParentAssocs(rendition.getChildRef());
        assertEquals(2, allParents.size());
        
        // The rendition should be created under a new folder called 'resized'
        NodeRef primaryParent = nodeService.getPrimaryParent(rendition.getChildRef()).getParentRef();
        assertEquals("resized", nodeService.getProperty(primaryParent, ContentModel.PROP_NAME));
        assertEquals(ContentModel.TYPE_FOLDER, nodeService.getType(primaryParent));
        
        // The rendition should have the source node as a non-primary parent.
        NodeRef nonPrimaryParent = null;
        for (ChildAssociationRef chAss : allParents)
        {
        	if (! chAss.getParentRef().equals(primaryParent))
        	{
        		nonPrimaryParent = chAss.getParentRef();
        	}
        }
        assertNotNull("Non-primary parent was not found.", nonPrimaryParent);
        assertEquals(nodeWithImageContent, nonPrimaryParent);
    }
    
    /**
     * If we're using path based renditions, it's allowed for 
     *  two source nodes to end up rendering into folder with
     *  the same name as each other.
     * One case is
     *   /images/world/logo.png -> /smaller/logo.png
     *   /images/europe/logo.png -> /smaller/logo.png
     * Clearly this isn't a great situation to be in, since the
     *  two source nodes are fighting over the rendered node!
     * However, people will sometimes end up doing this, so we
     *  need to ensure that the last rendered version wins, and
     *  the previous rendered version is fully gone. We mustn't
     *  end up with both source nodes thinking they have the same
     *  rendition node, because it'd only be one of theirs!
     */
    public void testPathBasedRenditionOverwrite() throws Exception
    {
       
    }
    
    /**
     * Using a dummy rendition engine, perform a number of
     *  renditions, both single and composite, to check that
     *  the renditions always end up as they should do.
     */
    public void testRenditionPlacements() throws Exception
    {
       QName plainQName = QName.createQName("Plain");
       RenditionDefinition rdPlain = renditionService.createRenditionDefinition(
             plainQName, DummyHelloWorldRenditionEngine.ENGINE_NAME
       );
       
       QName compositeQName = QName.createQName("Composite");
       CompositeRenditionDefinition rdComposite = renditionService.createCompositeRenditionDefinition(
             compositeQName
       );
       rdComposite.addAction(renditionService.createRenditionDefinition(
             QName.createQName("CompositePart1"), DummyHelloWorldRenditionEngine.ENGINE_NAME
       ));
       rdComposite.addAction(renditionService.createRenditionDefinition(
             QName.createQName("CompositePart2"), DummyHelloWorldRenditionEngine.ENGINE_NAME
       ));
       
       // ============================================== //
       //   Anonymous Rendition, no existing one there   //
       // ============================================== //
       
       assertNotNull(nodeWithDocContent);
       assertEquals(0, renditionService.getRenditions(nodeWithDocContent).size());
       assertEquals(0, nodeService.getChildAssocs(nodeWithDocContent).size());
       
       // Do a plain rendition, and check we acquired the one node
       renditionService.render(nodeWithDocContent, rdPlain);
       ChildAssociationRef renditionAssoc = renditionService.getRenditionByName(nodeWithDocContent, plainQName);
       assertNotNull(renditionAssoc);
       assertEquals(1, nodeService.getChildAssocs(nodeWithDocContent).size());
       assertEquals(plainQName, nodeService.getChildAssocs(nodeWithDocContent).get(0).getQName());
       
       // Tidy
       nodeService.deleteNode(
             renditionService.getRenditions(nodeWithDocContent).get(0).getChildRef()
       );
       assertNotNull(nodeWithDocContent);
       assertEquals(0, renditionService.getRenditions(nodeWithDocContent).size());
       assertEquals(0, nodeService.getChildAssocs(nodeWithDocContent).size());
       
       // Now do a composite rendition
       // Should once again have one node, despite there having been intermediate
       //  nodes created during the composite stage
       renditionService.render(nodeWithDocContent, rdComposite);
       assertEquals(1, renditionService.getRenditions(nodeWithDocContent).size());
       assertEquals(1, nodeService.getChildAssocs(nodeWithDocContent).size());
       renditionAssoc = renditionService.getRenditionByName(nodeWithDocContent, compositeQName);
       assertNotNull(renditionAssoc);
       assertEquals(compositeQName, nodeService.getChildAssocs(nodeWithDocContent).get(0).getQName());
       
       // Tidy
       nodeService.deleteNode(
             renditionService.getRenditions(nodeWithDocContent).get(0).getChildRef()
       );
       
       
       // ================================================ //
       //   Anonymous Rendition, existing one, same type   //
       // ================================================ //

       // Create one of the right type for a plain rendition
       renditionService.render(nodeWithDocContent, rdPlain);
       assertEquals(1, renditionService.getRenditions(nodeWithDocContent).size());
       assertEquals(1, nodeService.getChildAssocs(nodeWithDocContent).size());
       renditionAssoc = renditionService.getRenditionByName(nodeWithDocContent, plainQName);
       assertNotNull(renditionAssoc);

       // Run again, shouldn't change, should re-use the node
       renditionNode = renditionAssoc.getChildRef();
       renditionService.render(nodeWithDocContent, rdPlain);
       assertEquals(1, renditionService.getRenditions(nodeWithDocContent).size());
       assertEquals(1, nodeService.getChildAssocs(nodeWithDocContent).size());
       renditionAssoc = renditionService.getRenditionByName(nodeWithDocContent, plainQName);
       assertNotNull(renditionAssoc);
       assertEquals(renditionNode, renditionAssoc.getChildRef());
       assertEquals(plainQName, nodeService.getChildAssocs(nodeWithDocContent).get(0).getQName());
       
       // Tidy, and re-create for composite
       nodeService.deleteNode(
             renditionService.getRenditions(nodeWithDocContent).get(0).getChildRef()
       );
       renditionService.render(nodeWithDocContent, rdComposite);
       assertEquals(1, renditionService.getRenditions(nodeWithDocContent).size());
       assertEquals(1, nodeService.getChildAssocs(nodeWithDocContent).size());
       renditionAssoc = renditionService.getRenditionByName(nodeWithDocContent, compositeQName);
       assertNotNull(renditionAssoc);

       // Run again, shouldn't change, should re-use the node
       renditionNode = renditionAssoc.getChildRef();
       renditionService.render(nodeWithDocContent, rdComposite);
       assertEquals(1, renditionService.getRenditions(nodeWithDocContent).size());
       assertEquals(1, nodeService.getChildAssocs(nodeWithDocContent).size());
       renditionAssoc = renditionService.getRenditionByName(nodeWithDocContent, compositeQName);
       assertNotNull(renditionAssoc);
       assertEquals(renditionNode, renditionAssoc.getChildRef());
       assertEquals(compositeQName, nodeService.getChildAssocs(nodeWithDocContent).get(0).getQName());
       
       // Tidy
       nodeService.deleteNode(
             renditionService.getRenditions(nodeWithDocContent).get(0).getChildRef()
       );

       
       // ================================================= //
       //   Anonymous Rendition, existing one, wrong type   //
       // Note - this situation is not possible!            //
       // ================================================= //
       
       
       // ================================================= //
       //        Switch to being path based                 //
       // ================================================= //

       String fileName = "HelloWorld.txt";
    String path = "/" +
           (String) nodeService.getProperty(repositoryHelper.getCompanyHome(), ContentModel.PROP_NAME) +
           "/" +
           (String) nodeService.getProperty(testTargetFolder, ContentModel.PROP_NAME) +
           "/" + fileName;
       
       rdPlain.setParameterValue(
             RenditionService.PARAM_DESTINATION_PATH_TEMPLATE, path
       );
       rdComposite.setParameterValue(
             RenditionService.PARAM_DESTINATION_PATH_TEMPLATE, path
       );
       QName expectedName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, fileName);
       
       // ================================================= //
       //   Path based rendition, no existing one there     //
       // ================================================= //
       
       assertNotNull(nodeWithDocContent);
       assertEquals(0, renditionService.getRenditions(nodeWithDocContent).size());
       assertEquals(0, nodeService.getChildAssocs(nodeWithDocContent).size());
       List<ChildAssociationRef> children = nodeService.getChildAssocs(testTargetFolder);
       assertEquals(0, children.size());
       
       // Do a plain rendition, and check we acquired the one node
       renditionService.render(nodeWithDocContent, rdPlain);
       assertEquals(1, renditionService.getRenditions(nodeWithDocContent).size());
       assertEquals(1, nodeService.getChildAssocs(nodeWithDocContent).size());
       renditionAssoc = renditionService.getRenditionByName(nodeWithDocContent, plainQName);
       assertNotNull(renditionAssoc);
       assertFalse(renditionAssoc.isPrimary());
       children = nodeService.getChildAssocs(testTargetFolder);
       assertEquals(1, children.size());
       ChildAssociationRef childAssoc = children.get(0);
       assertEquals(expectedName, childAssoc.getQName());
       assertTrue(childAssoc.isPrimary());
       
       nodeService.deleteNode(
             renditionService.getRenditions(nodeWithDocContent).get(0).getChildRef()
       );
       
       assertNotNull(nodeWithDocContent);
       assertEquals(0, renditionService.getRenditions(nodeWithDocContent).size());
       // FIXME There is a bug in the NodeService whereby associations to children in the archive store
       //       i.e. deleted children, are included in the results to the getChildAssocs call.
       //       Therefore, pending a fix to that, we need to suppress this check and similar checks below.
       //assertEquals(0, nodeService.getChildAssocs(nodeWithDocContent).size());
       assertEquals(0, nodeService.getChildAssocs(testTargetFolder).size());
       
       // Now do a composite rendition
       // Should once again have one node, despite there having been intermediate
       //  nodes created during the composite stage
       renditionService.render(nodeWithDocContent, rdComposite);
       assertEquals(1, renditionService.getRenditions(nodeWithDocContent).size());
       //assertEquals(1, nodeService.getChildAssocs(nodeWithDocContent).size());
       renditionAssoc = renditionService.getRenditionByName(nodeWithDocContent, compositeQName);
       assertNotNull(renditionAssoc);
       assertFalse(renditionAssoc.isPrimary());
       children = nodeService.getChildAssocs(testTargetFolder);
       assertEquals(1, children.size());
       childAssoc = children.get(0);
       assertEquals(expectedName, childAssoc.getQName());
       assertTrue(childAssoc.isPrimary());
              
       // Tidy
       nodeService.deleteNode(
             renditionService.getRenditions(nodeWithDocContent).get(0).getChildRef()
       );

       
       // ================================================= //
       //   Path Based Rendition, existing one, same type   //
       // ================================================= //
       
       // Create one of the right type for a plain rendition
       renditionService.render(nodeWithDocContent, rdPlain);
       assertEquals(1, renditionService.getRenditions(nodeWithDocContent).size());
       //assertEquals(1, nodeService.getChildAssocs(nodeWithDocContent).size());
       assertEquals(1, children.size());
       renditionAssoc = renditionService.getRenditionByName(nodeWithDocContent, plainQName);
       assertNotNull(renditionAssoc);
       
       // Run again, shouldn't change, should re-use the node
       renditionNode = renditionAssoc.getChildRef();
       renditionService.render(nodeWithDocContent, rdPlain);
       assertEquals(1, renditionService.getRenditions(nodeWithDocContent).size());
       //assertEquals(1, nodeService.getChildAssocs(nodeWithDocContent).size());
       renditionAssoc = renditionService.getRenditionByName(nodeWithDocContent, plainQName);
       assertNotNull(renditionAssoc);
       assertFalse(renditionAssoc.isPrimary());
       assertEquals(renditionNode, renditionAssoc.getChildRef());
       children = nodeService.getChildAssocs(testTargetFolder);
       assertEquals(1, children.size());
       childAssoc = children.get(0);
       assertEquals(expectedName, childAssoc.getQName());
       assertTrue(childAssoc.isPrimary());
       assertEquals(renditionNode, childAssoc.getChildRef());
       
       // Tidy, and re-create for composite
       nodeService.deleteNode(
             renditionService.getRenditions(nodeWithDocContent).get(0).getChildRef()
       );
       renditionService.render(nodeWithDocContent, rdComposite);
       assertEquals(1, renditionService.getRenditions(nodeWithDocContent).size());
       //assertEquals(1, nodeService.getChildAssocs(nodeWithDocContent).size());
       assertEquals(1, children.size());
       renditionAssoc = renditionService.getRenditionByName(nodeWithDocContent, compositeQName);
       assertNotNull(renditionAssoc);
       
       // Run again, shouldn't change, should re-use the node
       renditionNode = renditionAssoc.getChildRef();
       renditionService.render(nodeWithDocContent, rdComposite);
       assertEquals(1, renditionService.getRenditions(nodeWithDocContent).size());
       //assertEquals(1, nodeService.getChildAssocs(nodeWithDocContent).size());
       renditionAssoc = renditionService.getRenditionByName(nodeWithDocContent, compositeQName);
       assertNotNull(renditionAssoc);
       assertFalse(renditionAssoc.isPrimary());
       assertEquals(renditionNode, renditionAssoc.getChildRef());
       children = nodeService.getChildAssocs(testTargetFolder);
       assertEquals(1, children.size());
       childAssoc = children.get(0);
       assertEquals(expectedName, childAssoc.getQName());
       assertTrue(childAssoc.isPrimary());
       assertEquals(renditionNode, childAssoc.getChildRef());

       
       // ================================================= //
       //   Path Based Rendition, existing one, wrong type  //
       // ================================================= //

       // We currently have a composite one
       assertEquals(expectedName, children.get(0).getQName());
       
       // Run the plain rendition, the composite one should be replaced
       //  with the new one
       renditionNode = children.get(0).getChildRef();

       // Currently, there is only one scenario in which it is legal for a rendition to overwrite an existing
       // node. That is when the rendition is an update of the source node i.e. the rendition node is already
       // linked to its source node by a rn:rendition association of the same name as the new rendition.
       try
       {
           renditionService.render(nodeWithDocContent, rdPlain);
           fail("Expected RenditionServiceException not thrown");
       } catch (RenditionServiceException expected)
       {
           // Do Nothing.
       }
       
    }

    
    private RenditionDefinition loadAndValidateRenditionDefinition(String renditionLocalName)
    {
        QName renditionQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, renditionLocalName);
        RenditionDefinition renditionDefinition = renditionService.loadRenditionDefinition(renditionQName);
        assertNotNull("'" + renditionLocalName + "' rendition definition was missing.", renditionDefinition);
        assertEquals("'" + renditionLocalName + "' renditionDefinition had wrong renditionName", renditionQName,
                    renditionDefinition.getRenditionName());

        assertNotNull("'" + renditionLocalName + "' renditionDefinition had null " +
                RenditionDefinitionImpl.RENDITION_DEFINITION_NAME + " parameter",
                    renditionDefinition.getParameterValue(RenditionDefinitionImpl.RENDITION_DEFINITION_NAME));
        
        // All builtin renditions should be "runas" system
        assertEquals(AuthenticationUtil.getSystemUserName(), renditionDefinition.getParameterValue(AbstractRenderingEngine.PARAM_RUN_AS));

        return renditionDefinition;
    }

    /**
     * Creates a RenderingAction (RenditionDefinition) for the
     * ReformatActionExecutor, setting the mimetype parameter value to plain
     * text.
     * 
     * @param renditionObjectType requested node type of the rendition object
     * @param targetMimetype
     * @return A new RenderingAction.
     */
    private RenditionDefinition makeReformatAction(QName renditionObjectType, String targetMimetype)
    {
        // Create the rendering action.
        RenditionDefinition action = renditionService.createRenditionDefinition(REFORMAT_RENDER_DEFN_NAME,
                    ReformatRenderingEngine.NAME);
        action.setParameterValue(AbstractRenderingEngine.PARAM_MIME_TYPE, targetMimetype);
        if (renditionObjectType != null)
        {
            action.setParameterValue(RenditionService.PARAM_RENDITION_NODETYPE, renditionObjectType);
        }
        return action;
    }

    /**
     * Creates a RenderingAction (RenditionDefinition) for the
     * RescaleImageActionExecutor.
     * 
     * @return A new RenderingAction.
     */
    private RenditionDefinition makeRescaleImageAction()
    {
        RenditionDefinition action = renditionService.createRenditionDefinition(RESCALE_RENDER_DEFN_NAME,
                    ImageRenderingEngine.NAME);
        action.setParameterValue(ImageRenderingEngine.PARAM_RESIZE_WIDTH, 42);
        return action;
    }

    private void validateRenderingActionDefinition(final String renderingEngineName)
    {
        // Make sure we can get the action definition.
        RenderingEngineDefinition renderingActionDefn = renditionService
                    .getRenderingEngineDefinition(renderingEngineName);
        assertNotNull("renderingActionDefn was null", renderingActionDefn);
        assertEquals("Incorrect renderingActionDefn name", renderingEngineName, renderingActionDefn.getName());
    }

    private void validateRenditionAssociation(ChildAssociationRef chAssRef, QName renderingActionQName)
    {
        assertEquals("The assoc type name was wrong", RenditionModel.ASSOC_RENDITION, chAssRef.getTypeQName());
        assertEquals("The assoc name was wrong", renderingActionQName, chAssRef.getQName());

        assertTrue("The source node should have the rn:renditioned aspect applied", nodeService.hasAspect(chAssRef
                    .getParentRef(), RenditionModel.ASPECT_RENDITIONED));

        final NodeRef newRenditionNodeRef = chAssRef.getChildRef();
        assertTrue("The new rendition node was not a rendition.", renditionService.isRendition(newRenditionNodeRef));

        // If the source node for the rendition equals the primary parent
        NodeRef renditionSource = renditionService.getSourceNode(newRenditionNodeRef).getParentRef();
        NodeRef renditionPrimaryParent = nodeService.getPrimaryParent(newRenditionNodeRef).getParentRef();
        if (renditionSource.equals(renditionPrimaryParent))
        {
            assertTrue("Rendition node was missing the hiddenRendition aspect", nodeService.hasAspect(
                        newRenditionNodeRef, RenditionModel.ASPECT_HIDDEN_RENDITION));
        }
        else
        {
            assertTrue("Rendition node was missing the visibleRendition aspect", nodeService.hasAspect(
                        newRenditionNodeRef, RenditionModel.ASPECT_VISIBLE_RENDITION));
        }

        assertEquals(ContentModel.PROP_CONTENT, nodeService.getProperty(newRenditionNodeRef,
                    ContentModel.PROP_CONTENT_PROPERTY_NAME));
    }

    /**
     * Given a QName this method returns the long-form String with the braces
     * escaped.
     */
    private String getLongNameWithEscapedBraces(QName qn)
    {
        String longName = qn.toString();
        String escapedBraces = longName.replace("{", "\\{").replace("}", "\\}");
        return escapedBraces;
    }

    private String readTextContent(NodeRef nodeRef)
    {
        ContentReader reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
        assertNotNull("reader was null", reader);
        reader.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        return reader.getContentString();
    }

    private CompositeRenditionDefinition makeCompositeReformatAndResizeDefinition(final QName renditionName,
                final int newX, final int newY)
    {
        CompositeRenditionDefinition compositeDefinition = renditionService
                    .createCompositeRenditionDefinition(renditionName);
        RenditionDefinition reformatDefinition = makeReformatAction(null, MimetypeMap.MIMETYPE_IMAGE_JPEG);
        RenditionDefinition rescaleImageDefinition = makeRescaleImageAction();
        rescaleImageDefinition.setParameterValue(ImageRenderingEngine.PARAM_RESIZE_WIDTH, newX);
        rescaleImageDefinition.setParameterValue(ImageRenderingEngine.PARAM_RESIZE_HEIGHT, newY);

        compositeDefinition.addAction(reformatDefinition);
        compositeDefinition.addAction(rescaleImageDefinition);
        return compositeDefinition;
    }
    
    public void testJavascriptAPI() throws Exception
    {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("testSourceNode", this.nodeWithImageContent);
        model.put("testDocNode", this.nodeWithDocContent);
        
        ScriptLocation location = new ClasspathScriptLocation("org/alfresco/repo/rendition/script/test_renditionService.js");
        this.scriptService.executeScript(location, model);
    }

    /**
     * This test method takes an image with Exif Orientation information and checks if it is automatially rotated.
     */
    public void testAutoRotateImage() throws Exception
    {
        NodeRef companyHome = repositoryHelper.getCompanyHome();

        //Check image is there
        String imageSource = "images/rotated_gray21.512.jpg";
        File imageFile = retrieveValidImageFile(imageSource);

        //Create node and save contents
        final NodeRef newNodeForRotate = createNode(companyHome, "rotateImageNode", ContentModel.TYPE_CONTENT);
        setImageContentOnNode(newNodeForRotate, MimetypeMap.MIMETYPE_IMAGE_JPEG, imageFile);

        //Test auto rotates
        final Map<String, Serializable> parameterValues = new HashMap<String, Serializable>();        
        resizeImageAndCheckOrientation(newNodeForRotate, parameterValues, BLACK, WHITE);     
        
        //Test doesn't auto rotate
        parameterValues.clear();
        parameterValues.put(ImageRenderingEngine.PARAM_AUTO_ORIENTATION, false);
        resizeImageAndCheckOrientation(newNodeForRotate, parameterValues, WHITE, BLACK);     
        
        //Clean up
        nodeService.deleteNode(newNodeForRotate);
    }

    private void resizeImageAndCheckOrientation(final NodeRef nodeToResize, final Map<String, Serializable> parameterValues, final String topLeft, final String bottomRight)
    {
        //Resize to 100 by 100
        final Integer imageNewXSize = new Integer(100);
        final Integer imageNewYSize = new Integer(100);
        parameterValues.put(ImageRenderingEngine.PARAM_RESIZE_WIDTH, imageNewXSize);
        parameterValues.put(ImageRenderingEngine.PARAM_RESIZE_HEIGHT, imageNewYSize);

        final NodeRef newRenditionNode = performImageRendition(parameterValues, nodeToResize);

        // Assert that the rendition is of the correct size and has reasonable content.
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                // The rescaled image rendition is a child of the original test
                // node.
                List<ChildAssociationRef> children = nodeService.getChildAssocs(nodeToResize,
                            new RegexQNamePattern(getLongNameWithEscapedBraces(RenditionModel.ASSOC_RENDITION)),
                            new RegexQNamePattern(getLongNameWithEscapedBraces(RESCALE_RENDER_DEFN_NAME)));

                // There should only be one child of the image node: the
                // rendition we've just created.
                assertEquals("Unexpected number of children", 1, children.size());

                NodeRef newImageRendition = children.get(0).getChildRef();
                assertEquals(newRenditionNode, newImageRendition);

                ContentReader reader = contentService.getReader(newImageRendition, ContentModel.PROP_CONTENT);
                assertNotNull("Reader to rendered image was null", reader);
                BufferedImage srcImg = ImageIO.read(reader.getContentInputStream());
                checkTopLeftBottomRight(srcImg, topLeft, bottomRight);

                return null;
            }
        });
    } 
    
    private static File retrieveValidImageFile(String imageSource) throws IOException
    {
        URL url = RenditionServiceIntegrationTest.class.getClassLoader().getResource(imageSource);
        File imageFile = new File(url.getFile());
        BufferedImage img = ImageIO.read(url);
        assertNotNull("image was null", img);
        checkTopLeftBottomRight(img, WHITE, BLACK);
        return imageFile;
    }
    
    private static void checkTopLeftBottomRight(BufferedImage img, String topLeft, String bottomRight)
    {
        int rgbAtTopLeft = img.getRGB(1, 1);
        assertTrue("upper left should be "+topLeft, Integer.toHexString(rgbAtTopLeft).endsWith(topLeft));
        int rgbAtBottomRight = img.getRGB(img.getWidth() - 1, img.getHeight() - 1);
        assertTrue("lower right should be "+bottomRight, Integer.toHexString(rgbAtBottomRight).endsWith(bottomRight));
    }
    
    /**
     * A dummy rendering engine used in testing
     */
    private static class DummyHelloWorldRenditionEngine extends AbstractRenderingEngine
    {
       private static final String ENGINE_NAME = "helloWorldRenderingEngine";
       
       /**
        * Loads this executor into the ApplicationContext, if it
        *  isn't already there
        */
       public static void registerIfNeeded(ConfigurableApplicationContext ctx)
       {
          if(!ctx.containsBean(ENGINE_NAME))
          {
             // Create, and do dependencies
             DummyHelloWorldRenditionEngine hw = new DummyHelloWorldRenditionEngine();
             hw.setRuntimeActionService(
                   (RuntimeActionService)ctx.getBean("actionService")
             );
             hw.setNodeService(
                   (NodeService)ctx.getBean("NodeService")
             );
             hw.setContentService(
                   (ContentService)ctx.getBean("ContentService")
             );
             hw.setRenditionService(
                   (RenditionService)ctx.getBean("RenditionService")
             );
             hw.setBehaviourFilter(
                   (BehaviourFilter)ctx.getBean("policyBehaviourFilter")
             );
             hw.setRenditionLocationResolver(
                   (RenditionLocationResolver)ctx.getBean("renditionLocationResolver")
             );
             
             // Register
             ctx.getBeanFactory().registerSingleton(
                   ENGINE_NAME, hw
             );
             hw.init();
          }
       }
       
       @Override
       protected void render(RenderingContext context) {
           ContentWriter contentWriter = context.makeContentWriter();
           contentWriter.setMimetype("text/plain");
           contentWriter.putContent( "Hello, world!" );
       }
    }
}
