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
package org.alfresco.module.org_alfresco_module_rm.test.integration.destroy;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.action.impl.CutOffAction;
import org.alfresco.module.org_alfresco_module_rm.action.impl.DestroyAction;
import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationAspectProperties;
import org.alfresco.module.org_alfresco_module_rm.content.ContentDestructionComponent;
import org.alfresco.module.org_alfresco_module_rm.content.EagerContentStoreCleaner;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.module.org_alfresco_module_rm.test.util.CommonRMTestUtils;
import org.alfresco.module.org_alfresco_module_rm.test.util.TestContentCleanser;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.rendition.RenditionService;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.test.AlfrescoTest;
import org.alfresco.util.GUID;

/**
 * Acceptance criteria for content destruction and content cleansing.
 *
 * @author Roy Wetherall
 * @Author 3.0.a
 */
public class DestroyContentTest extends BaseRMTestCase
{
    private static final String BEAN_NAME_CONTENT_CLEANSER = "contentCleanser.test";
    
    private ContentStore contentStore;
    private TestContentCleanser contentCleanser;
    private EagerContentStoreCleaner eagerContentStoreCleaner;
    private ContentDestructionComponent contentDestructionComponent;
    private RenditionService renditionService;
    
    @Override
    protected void initServices()
    {
        super.initServices();        
        contentStore = (ContentStore)applicationContext.getBean("fileContentStore");
        contentCleanser = (TestContentCleanser)applicationContext.getBean(BEAN_NAME_CONTENT_CLEANSER);
        eagerContentStoreCleaner = (EagerContentStoreCleaner)applicationContext.getBean("eagerContentStoreCleaner");
        contentDestructionComponent = (ContentDestructionComponent)applicationContext.getBean("contentDestructionComponent");
        renditionService = (RenditionService)applicationContext.getBean("renditionService");
        
        // set the test content store cleaner
        eagerContentStoreCleaner.setContentCleanser(contentCleanser);
    }
    
    /**
     * Given that a record folder is eligible for destruction
     * And record ghosting is applied
     * When the record folder is destroyed
     * Then the record folder and records are ghosted
     * And the content is destroyed
     */
    @AlfrescoTest (jira="RM-2506")
    public void testRecordFolderDestroy() throws Exception
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {    
            private NodeRef recordCategoryFolderLevel;
            private NodeRef destroyableFolder;
            private NodeRef subRecord;
            
            public void given() throws Exception
            {
                // create destroyable record folder that contains a record
                recordCategoryFolderLevel = filePlanService.createRecordCategory(filePlan, GUID.generate());
                utils.createBasicDispositionSchedule(
                            recordCategoryFolderLevel, 
                            CommonRMTestUtils.DEFAULT_DISPOSITION_INSTRUCTIONS, 
                            CommonRMTestUtils.DEFAULT_DISPOSITION_AUTHORITY, 
                            false, 
                            true);
                destroyableFolder = recordFolderService.createRecordFolder(recordCategoryFolderLevel, GUID.generate());
                
                Map<QName, Serializable> props = new HashMap<QName, Serializable>(1);
                props.put(ContentModel.PROP_TITLE, GUID.generate());
                InputStream is = System.class.getResourceAsStream("/alfresco/test/content/Image.jpg");                
                subRecord = utils.createRecord(destroyableFolder, GUID.generate(), props, MimetypeMap.MIMETYPE_IMAGE_JPEG, is);
                
                renditionService.render(subRecord, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "medium"));
                
                utils.completeRecord(subRecord);
                utils.completeEvent(destroyableFolder, CommonRMTestUtils.DEFAULT_EVENT_NAME);
                rmActionService.executeRecordsManagementAction(destroyableFolder, CutOffAction.NAME);
                
                // assert things are as we expect
                assertEquals(DestroyAction.NAME, dispositionService.getNextDispositionAction(destroyableFolder).getName());
                assertTrue(dispositionService.isNextDispositionActionEligible(destroyableFolder));
                
                // reset test content cleanser
                contentCleanser.reset();
                assertFalse(contentDestructionComponent.isCleansingEnabled());
            }
            
            public void when() throws Exception
            {
                // destroy the folder
                rmActionService.executeRecordsManagementAction(destroyableFolder, DestroyAction.NAME);
            }

            public void then() throws Exception
            {
                // folder and record exist and are ghosted
                assertTrue(nodeService.exists(destroyableFolder));
                assertTrue(nodeService.hasAspect(destroyableFolder, ASPECT_GHOSTED));
                assertTrue(nodeService.exists(subRecord));
                assertTrue(nodeService.hasAspect(subRecord, ASPECT_GHOSTED));
                
                // record content is destroyed
                ContentReader reader = contentService.getReader(subRecord, PROP_CONTENT);
                assertNull(reader);    
                
                // content cleansing hasn't taken place
                assertFalse(contentCleanser.hasCleansed());
            }
        });
    }
    
    /**
     * Given that a record is eligible for destruction
     * And record ghosting is applied
     * When the record is destroyed
     * Then the record is ghosted
     * And the content is destroyed
     */
    @AlfrescoTest (jira="RM-2506")
    public void testRecordDestroy() throws Exception
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {    
            private NodeRef recordCategoryRecordLevel;
            private NodeRef recordFolder;
            private NodeRef destroyableRecord;
            
            public void given() throws Exception
            {
                // create destroyable record
                recordCategoryRecordLevel = filePlanService.createRecordCategory(filePlan, GUID.generate());
                utils.createBasicDispositionSchedule(
                            recordCategoryRecordLevel, 
                            CommonRMTestUtils.DEFAULT_DISPOSITION_INSTRUCTIONS, 
                            CommonRMTestUtils.DEFAULT_DISPOSITION_AUTHORITY, 
                            true, 
                            true);
                recordFolder = recordFolderService.createRecordFolder(recordCategoryRecordLevel, GUID.generate());
                destroyableRecord = utils.createRecord(recordFolder, GUID.generate(), GUID.generate());
                utils.completeRecord(destroyableRecord);
                utils.completeEvent(destroyableRecord, CommonRMTestUtils.DEFAULT_EVENT_NAME);
                rmActionService.executeRecordsManagementAction(destroyableRecord, CutOffAction.NAME);
                
                // assert things are as we expect
                assertEquals(DestroyAction.NAME, dispositionService.getNextDispositionAction(destroyableRecord).getName());
                assertTrue(dispositionService.isNextDispositionActionEligible(destroyableRecord));
                
                // reset test content cleanser
                contentCleanser.reset();
                assertFalse(contentDestructionComponent.isCleansingEnabled());
            }
            
            public void when() throws Exception
            {
                // destroy the folder
                rmActionService.executeRecordsManagementAction(destroyableRecord, DestroyAction.NAME);
            }

            public void then() throws Exception
            {
                // show that record still exists and has the ghosted aspect applied 
                assertTrue(nodeService.exists(destroyableRecord));
                assertTrue(nodeService.hasAspect(destroyableRecord, ASPECT_GHOSTED));
                
                // record content is destroyed
                ContentReader reader = contentService.getReader(destroyableRecord, PROP_CONTENT);
                assertNull(reader);        
                
                // content cleansing hasn't taken place
                assertFalse(contentCleanser.hasCleansed());            
            }
        });
    }
    
    /**
     * Given that a record is eligible for destruction
     * And record ghosting is applied
     * And cleansing is configured on
     * When the record is destroyed
     * Then the record is ghosted
     * And the content is cleansed
     * And then content is destroyed
     */
    @AlfrescoTest (jira="RM-2505")
    public void testRecordDestroyAndCleanse() throws Exception
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {    
            private NodeRef recordCategoryRecordLevel;
            private NodeRef recordFolder;
            private NodeRef destroyableRecord;
            
            public void given() throws Exception
            {
                // create destroyable record
                recordCategoryRecordLevel = filePlanService.createRecordCategory(filePlan, GUID.generate());
                utils.createBasicDispositionSchedule(
                            recordCategoryRecordLevel, 
                            CommonRMTestUtils.DEFAULT_DISPOSITION_INSTRUCTIONS, 
                            CommonRMTestUtils.DEFAULT_DISPOSITION_AUTHORITY, 
                            true, 
                            true);
                recordFolder = recordFolderService.createRecordFolder(recordCategoryRecordLevel, GUID.generate());
                destroyableRecord = utils.createRecord(recordFolder, GUID.generate(), GUID.generate());
                utils.completeRecord(destroyableRecord);
                utils.completeEvent(destroyableRecord, CommonRMTestUtils.DEFAULT_EVENT_NAME);
                rmActionService.executeRecordsManagementAction(destroyableRecord, CutOffAction.NAME);
                
                // assert things are as we expect
                assertEquals(DestroyAction.NAME, dispositionService.getNextDispositionAction(destroyableRecord).getName());
                assertTrue(dispositionService.isNextDispositionActionEligible(destroyableRecord));
                
                // reset test content cleanser and configure on
                contentCleanser.reset();
                contentDestructionComponent.setCleansingEnabled(true);
                assertTrue(contentDestructionComponent.isCleansingEnabled());
            }
            
            public void when() throws Exception
            {
                // destroy the folder
                rmActionService.executeRecordsManagementAction(destroyableRecord, DestroyAction.NAME);
            }

            public void then() throws Exception
            {
                // show that record still exists and has the ghosted aspect applied 
                assertTrue(nodeService.exists(destroyableRecord));
                assertTrue(nodeService.hasAspect(destroyableRecord, ASPECT_GHOSTED));
                
                // record content is destroyed
                ContentReader reader = contentService.getReader(destroyableRecord, PROP_CONTENT);
                assertNull(reader);        
                
                // content cleansing has taken place
                assertTrue(contentCleanser.hasCleansed());            
            }
            
            public void after() throws Exception
            {
                // reset cleansing to default 
                contentDestructionComponent.setCleansingEnabled(false);
            }
        });
    }
    
    /**
     * When the a record is deleted
     * Then the content is destroyed
     */
    @AlfrescoTest (jira="RM-2461")
    public void testRecordDelete() throws Exception
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {    
            private NodeRef recordCategoryRecordLevel;
            private NodeRef recordFolder;
            private NodeRef deleteableRecord;
            private ContentData contentData;
            
            public void given() throws Exception
            {
                // create destroyable record
                recordCategoryRecordLevel = filePlanService.createRecordCategory(filePlan, GUID.generate());
                recordFolder = recordFolderService.createRecordFolder(recordCategoryRecordLevel, GUID.generate());
                deleteableRecord = utils.createRecord(recordFolder, GUID.generate(), GUID.generate());
                contentData = (ContentData)nodeService.getProperty(deleteableRecord, PROP_CONTENT);
                
                // assert things are as we expect
                assertNotNull(contentData);
                assertTrue(contentStore.exists(contentData.getContentUrl()));
                
                // reset test content cleanser
                contentCleanser.reset();
                assertFalse(contentDestructionComponent.isCleansingEnabled());
            }
            
            public void when() throws Exception
            {
                // delete the record
                nodeService.deleteNode(deleteableRecord);
            }

            public void then() throws Exception
            {
                // record destroyed 
                assertFalse(nodeService.exists(deleteableRecord));
                assertFalse(contentStore.exists(contentData.getContentUrl()));         
                
                // content cleansing hasn't taken place
                assertFalse(contentCleanser.hasCleansed());         
            }
        });
    }
    
    /**
     * Given cleansing is configured on
     * When the a record is deleted
     * Then the content is cleansed
     * And then the content is destroyed
     */
    @AlfrescoTest (jira="RM-2460")
    public void testRecordDeleteAndCleanse() throws Exception
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {    
            private NodeRef recordCategoryRecordLevel;
            private NodeRef recordFolder;
            private NodeRef deleteableRecord;
            private ContentData contentData;
            
            public void given() throws Exception
            {
                // create destroyable record
                recordCategoryRecordLevel = filePlanService.createRecordCategory(filePlan, GUID.generate());
                recordFolder = recordFolderService.createRecordFolder(recordCategoryRecordLevel, GUID.generate());
                deleteableRecord = utils.createRecord(recordFolder, GUID.generate(), GUID.generate());
                contentData = (ContentData)nodeService.getProperty(deleteableRecord, PROP_CONTENT);
                
                // assert things are as we expect
                assertNotNull(contentData);
                assertTrue(contentStore.exists(contentData.getContentUrl()));
                
                // reset test content cleanser and configure on
                contentCleanser.reset();
                contentDestructionComponent.setCleansingEnabled(true);
                assertTrue(contentDestructionComponent.isCleansingEnabled());
            }
            
            public void when() throws Exception
            {
                // delete the record
                nodeService.deleteNode(deleteableRecord);
            }

            public void then() throws Exception
            {
                // record destroyed 
                assertFalse(nodeService.exists(deleteableRecord));
                assertFalse(contentStore.exists(contentData.getContentUrl()));         
                
                // content cleansing has taken place
                assertTrue(contentCleanser.hasCleansed());             
            }
            
            public void after() throws Exception
            {
                // reset cleansing to default 
                contentDestructionComponent.setCleansingEnabled(false);
            }
        });
    }
    
    /**
     * When classified content (non-record) is deleted
     * Then it is destroyed
     */
    @AlfrescoTest (jira="RM-2461")
    public void testClassifiedContentDelete() throws Exception
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {    
            private NodeRef deleteableContent;
            private ContentData contentData;
            
            public void given() throws Exception
            {
                // create deletable classified content
                assertTrue(nodeService.exists(folder));
                deleteableContent = fileFolderService.create(folder, "myDocument.txt", TYPE_CONTENT).getNodeRef();
                ContentWriter writer = fileFolderService.getWriter(deleteableContent);
                writer.setEncoding("UTF-8");
                writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
                writer.putContent(GUID.generate());
                
                // classify the content
                ClassificationAspectProperties properties = new ClassificationAspectProperties();
                properties.setClassificationLevelId("level1");
                properties.setClassifiedBy("me");
                properties.setClassificationReasonIds(Collections.singleton("Test Reason 1"));
                contentClassificationService.classifyContent(properties, deleteableContent);
                
                // grab the content data
                contentData = (ContentData)nodeService.getProperty(deleteableContent, PROP_CONTENT);
                
                // assert things are as we expect
                assertNotNull(contentData);
                assertTrue(contentStore.exists(contentData.getContentUrl()));
                
                // reset test content cleanser
                contentCleanser.reset();
                assertFalse(contentDestructionComponent.isCleansingEnabled());
            }
            
            public void when() throws Exception
            {
                // delete the content
                nodeService.deleteNode(deleteableContent);
            }

            public void then() throws Exception
            {
                // content destroyed 
                assertFalse(nodeService.exists(deleteableContent));
                assertFalse(contentStore.exists(contentData.getContentUrl()));      
                
                // content cleansing hasn't taken place
                assertFalse(contentCleanser.hasCleansed());            
            }
        });
    }
    
    /**
     * Given data cleansing is configured on
     * When classified content (non-record) is deleted
     * Then it is cleansed
     * And then it is destroyed
     */
    @AlfrescoTest (jira="RM-2460")
    public void testClassifiedContentDeleteAndCleanse() throws Exception
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {    
            private NodeRef deleteableContent;
            private ContentData contentData;
            
            public void given() throws Exception
            {
                // create deletable classified content
                assertTrue(nodeService.exists(folder));
                deleteableContent = fileFolderService.create(folder, "myDocument.txt", TYPE_CONTENT).getNodeRef();
                ContentWriter writer = fileFolderService.getWriter(deleteableContent);
                writer.setEncoding("UTF-8");
                writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
                writer.putContent(GUID.generate());
                
                // classify the content
                ClassificationAspectProperties properties = new ClassificationAspectProperties();
                properties.setClassificationLevelId("level1");
                properties.setClassifiedBy("me");
                properties.setClassificationReasonIds(Collections.singleton("Test Reason 1"));
                contentClassificationService.classifyContent(properties, deleteableContent);
                
                // grab the content data
                contentData = (ContentData)nodeService.getProperty(deleteableContent, PROP_CONTENT);
                
                // assert things are as we expect
                assertNotNull(contentData);
                assertTrue(contentStore.exists(contentData.getContentUrl()));
                
                // reset test content cleanser and configure on
                contentCleanser.reset();
                contentDestructionComponent.setCleansingEnabled(true);
                assertTrue(contentDestructionComponent.isCleansingEnabled());
            }
            
            public void when() throws Exception
            {
                // delete the content
                nodeService.deleteNode(deleteableContent);
            }

            public void then() throws Exception
            {
                // content destroyed 
                assertFalse(nodeService.exists(deleteableContent));
                assertFalse(contentStore.exists(contentData.getContentUrl()));           
                
                // content cleansing has taken place
                assertTrue(contentCleanser.hasCleansed());             
            }
            
            public void after() throws Exception
            {
                // reset cleansing to default 
                contentDestructionComponent.setCleansingEnabled(false);
            }
        });
    }
    
    /**
     * When a unclassified document (non-record) is deleted
     * Then it is deleted but the the content is not immediately destroyed
     */
    @AlfrescoTest (jira="RM-2507")
    public void testContentDelete() throws Exception
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {    
            private NodeRef deleteableContent;
            private ContentData contentData;
            
            public void given() throws Exception
            {
                // create deletable content
                assertTrue(nodeService.exists(folder));
                deleteableContent = fileFolderService.create(folder, "myDocument.txt", TYPE_CONTENT).getNodeRef();
                ContentWriter writer = fileFolderService.getWriter(deleteableContent);
                writer.setEncoding("UTF-8");
                writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
                writer.putContent(GUID.generate());
                
                contentData = (ContentData)nodeService.getProperty(deleteableContent, PROP_CONTENT);
                
                // assert things are as we expect
                assertNotNull(contentData);
                assertTrue(contentStore.exists(contentData.getContentUrl()));
                
                // reset test content cleanser
                contentCleanser.reset();
                assertFalse(contentDestructionComponent.isCleansingEnabled());
            }
            
            public void when() throws Exception
            {
                // delete the content
                nodeService.deleteNode(deleteableContent);
            }

            public void then() throws Exception
            {
                // content deleted but not destroyed
                assertFalse(nodeService.exists(deleteableContent));
                assertTrue(contentStore.exists(contentData.getContentUrl()));       
                
                // content cleansing hasn't taken place
                assertFalse(contentCleanser.hasCleansed());           
            }
        });
    }
}
