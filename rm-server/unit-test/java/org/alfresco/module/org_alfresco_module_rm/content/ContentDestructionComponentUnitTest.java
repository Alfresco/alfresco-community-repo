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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.classification.ContentClassificationService;
import org.alfresco.module.org_alfresco_module_rm.content.cleanser.ContentCleanser;
import org.alfresco.module.org_alfresco_module_rm.test.util.AlfMock;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
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
     * When it is deleted
     * Then it is sent for immediate destruction
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
     * When it is deleted
     * Then it is send for immediate destruction
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
     * Then it is not scheduled for cleansing before destruction
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
     * Then it is scheduled for cleansing before destruction
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
        
        List<QName> contentProperties = new ArrayList<QName>(contentPropertiesCount);
        for (int i = 0; i < contentPropertiesCount; i++)
        {
            contentProperties.add(AlfMock.generateQName());            
        }        
        
        when(mockedDictionaryService.getAllProperties(ContentModel.TYPE_CONTENT))
            .thenReturn(contentProperties);
        
        DataTypeDefinition mockedDataTypeDefinition = mock(DataTypeDefinition.class);
        when(mockedDataTypeDefinition.getName())
            .thenReturn(DataTypeDefinition.CONTENT);
        
        PropertyDefinition mockedPropertyDefinition = mock(PropertyDefinition.class);       
        when(mockedPropertyDefinition.getDataType())
            .thenReturn(mockedDataTypeDefinition);
                    
        when(mockedDictionaryService.getProperty(any(QName.class)))
            .thenReturn(mockedPropertyDefinition);
        
        ContentData mockedDataContent = mock(ContentData.class);
        when(mockedDataContent.getContentUrl())
            .thenReturn(contentURL);
        when(mockedNodeService.getProperty(eq(nodeRef), any(QName.class)))
            .thenReturn(mockedDataContent);
        
        return nodeRef;
    }
}
