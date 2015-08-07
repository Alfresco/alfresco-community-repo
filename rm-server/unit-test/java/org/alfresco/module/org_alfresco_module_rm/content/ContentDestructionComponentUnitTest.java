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
package org.alfresco.module.org_alfresco_module_rm.content;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.classification.ContentClassificationService;
import org.alfresco.module.org_alfresco_module_rm.content.cleanser.ContentCleanser;
import org.alfresco.module.org_alfresco_module_rm.test.util.AlfMock;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

/**
 * Content destruction component unit test.
 * 
 * @author Roy Wetherall
 * @since 3.0.a
 */
public class ContentDestructionComponentUnitTest extends BaseUnitTest
{
    @InjectMocks private ContentDestructionComponent contentDestructionComponent;
    
    @Mock private ContentClassificationService mockedContentClassificationService;
    @Mock private ContentCleanser mockedContentCleanser;
    @Mock private EagerContentStoreCleaner mockedEagerContentStoreCleaner;
    
    /**
     * Given a non-sensitive node
     * When it is deleted
     * Then nothing happens
     */
    @Test
    public void deleteNonSensitiveNode()
    {
        NodeRef nodeRef = generateCmContent("myContent.txt");
        
        when(mockedRecordService.isRecord(nodeRef))
            .thenReturn(false);
        when(mockedContentClassificationService.isClassified(nodeRef))
            .thenReturn(false);
        
        contentDestructionComponent.beforeDeleteNode(nodeRef);
        
        verifyZeroInteractions(mockedEagerContentStoreCleaner, mockedDictionaryService);
    }
    
    /**
     * Given a record 
     * And that by default cleansing is off
     * When it is deleted
     * Then it is sent for immediate destruction
     * And not cleansed
     */
    @Test
    public void deleteRecord()
    {
        String contentURL = AlfMock.generateText();
        NodeRef nodeRef = generateDeletedNodeRef(contentURL);
                
        when(mockedRecordService.isRecord(nodeRef))
            .thenReturn(true);
        when(mockedContentClassificationService.isClassified(nodeRef))
            .thenReturn(false);
        
        contentDestructionComponent.beforeDeleteNode(nodeRef);
        
        verify(mockedEagerContentStoreCleaner).registerOrphanedContentUrl(contentURL, true);
    }
    
    /**
     * Given classified content
     * And that by default cleansing is off
     * When it is deleted
     * Then it is send for immediate destruction
     * And not cleansed
     */
    @Test
    public void deleteClassifiedContent()
    {
        String contentURL = AlfMock.generateText();
        NodeRef nodeRef = generateDeletedNodeRef(contentURL);
                
        when(mockedRecordService.isRecord(nodeRef))
            .thenReturn(false);
        when(mockedContentClassificationService.isClassified(nodeRef))
            .thenReturn(true);
        
        contentDestructionComponent.beforeDeleteNode(nodeRef);
        
        verify(mockedEagerContentStoreCleaner).registerOrphanedContentUrl(contentURL, true);
    }
    
    /**
     * Given that content cleansing is turned on
     * When a sensitive node is deleted
     * Then it is scheduled for cleansing before destruction
     */
    @Test
    public void contentCleansingOn()
    {
        String contentURL = AlfMock.generateText();
        NodeRef nodeRef = generateDeletedNodeRef(contentURL);
                
        when(mockedRecordService.isRecord(nodeRef))
            .thenReturn(false);
        when(mockedContentClassificationService.isClassified(nodeRef))
            .thenReturn(true);
        
        contentDestructionComponent.setCleansingEnabled(true);
        contentDestructionComponent.beforeDeleteNode(nodeRef);
        
        verify(mockedEagerContentStoreCleaner).registerOrphanedContentUrlForCleansing(contentURL);        
    }
    
    /**
     * Given that content cleansing is turned off
     * When a sensitive node is deleted
     * Then it is not scheduled for cleansing before destruction
     */
    @Test
    public void contentCleansingOff()
    {
        String contentURL = AlfMock.generateText();
        NodeRef nodeRef = generateDeletedNodeRef(contentURL);
                
        when(mockedRecordService.isRecord(nodeRef))
            .thenReturn(false);
        when(mockedContentClassificationService.isClassified(nodeRef))
            .thenReturn(true);
        
        contentDestructionComponent.setCleansingEnabled(false);
        contentDestructionComponent.beforeDeleteNode(nodeRef);
        
        verify(mockedEagerContentStoreCleaner).registerOrphanedContentUrl(contentURL, true);
        
    }
    
    /**
     * Given that a sensitive node has more than one content property
     * When is it deleted
     * Then all the content properties are scheduled for destruction 
     */
    @Test
    public void moreThanOneContentProperty()
    {
        String contentURL = AlfMock.generateText();
        NodeRef nodeRef = generateDeletedNodeRef(contentURL, 2);
                
        when(mockedRecordService.isRecord(nodeRef))
            .thenReturn(false);
        when(mockedContentClassificationService.isClassified(nodeRef))
            .thenReturn(true);
        
        contentDestructionComponent.setCleansingEnabled(true);
        contentDestructionComponent.beforeDeleteNode(nodeRef);
        
        verify(mockedEagerContentStoreCleaner, times(2)).registerOrphanedContentUrlForCleansing(contentURL);  
        
    }    

    /**
     * Helper method that creates deleted node reference
     */
    private NodeRef generateDeletedNodeRef(String contentURL)
    {
        return generateDeletedNodeRef(contentURL, 1);
    }
    
    /**
     * Helper method that creates deleted node reference
     */
    private NodeRef generateDeletedNodeRef(String contentURL, int contentPropertiesCount)
    {
        NodeRef nodeRef = generateCmContent("myContent.txt");
        
        ContentData mockedContentData = mock(ContentData.class);
        when(mockedContentData.getContentUrl())
            .thenReturn(contentURL);
        
        Map<QName, Serializable> propertiesMap = new HashMap<QName, Serializable>(contentPropertiesCount);
        for(int i = 0; i < contentPropertiesCount; i++)
        {
            propertiesMap.put(AlfMock.generateQName(), mockedContentData);
        }
                
        when(mockedNodeService.getProperties(nodeRef))
            .thenReturn(propertiesMap);
        
        return nodeRef;
    }
}
