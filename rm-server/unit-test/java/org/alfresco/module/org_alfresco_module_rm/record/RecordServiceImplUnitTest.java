/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.record;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;

/**
 * Unit test for RecordServiceImpl
 *
 * @author Roy Wetherall
 * @since 2.2
 */
public class RecordServiceImplUnitTest extends BaseUnitTest
{
    private NodeRef nonStandardFilePlanComponent;
    private NodeRef nonStandardFilePlan;

    private static QName TYPE_MY_FILE_PLAN                  = generateQName();
    private static QName ASPECT_FOR_FILE_PLAN               = generateQName();

    @InjectMocks private RecordServiceImpl recordService;

    @SuppressWarnings("unchecked")
    @Before
    @Override
    public void before() throws Exception
    {
        super.before();

        nonStandardFilePlanComponent = generateNodeRef(TYPE_RECORD_CATEGORY);
        nonStandardFilePlan = generateNodeRef(TYPE_MY_FILE_PLAN);

        // set-up node service
        when(mockedNodeService.getProperty(nonStandardFilePlanComponent, PROP_ROOT_NODEREF)).thenReturn(nonStandardFilePlan);

        // set-up dictionary service
        when(mockedDictionaryService.getAllAspects()).thenReturn(CollectionUtils.EMPTY_COLLECTION);
    }

    @Test
    public void testRegisterRecordMetadataAspect()
    {
        Map<QName, Set<QName>> map = recordService.getRecordMetadataAspectsMap();
        assertTrue(map.isEmpty());
        recordService.registerRecordMetadataAspect(ASPECT_FOR_FILE_PLAN, TYPE_FILE_PLAN);
        map = recordService.getRecordMetadataAspectsMap();
        assertEquals(1, map.size());
        assertTrue(map.containsKey(ASPECT_FOR_FILE_PLAN));
        Set<QName> types = map.get(ASPECT_FOR_FILE_PLAN);
        assertNotNull(types);
        assertEquals(1, types.size());
        assertTrue(types.contains(TYPE_FILE_PLAN));
    }
    
    /**
     * Given invalid types
     * When linking
     * Then exception thrown
     */
    @Test
    public void linkNonRecord()
    {
        NodeRef nonRecord = generateNodeRef(TYPE_CONTENT);
        NodeRef recordFolder = generateRecordFolder();
        
        // set expected exception
        exception.expect(AlfrescoRuntimeException.class);
        
        // link
        recordService.link(nonRecord, recordFolder);
    }    
    @Test
    public void linkNonRecordFolder()
    {
        NodeRef record = generateRecord();
        NodeRef nonRecordFolder = generateNodeRef(TYPE_FOLDER);
        
        // set expected exception
        exception.expect(AlfrescoRuntimeException.class);
        
        // link
        recordService.link(record, nonRecordFolder);
    }
    
    /**
     * Given that the record is already a child of the record folder
     * When I try to link the record to the same record folder
     * Then an exception is thrown
     */
    @Test
    public void linkRecordToRecordFolderFailsIfAlreadyAChild()
    {
        NodeRef record = generateRecord();
        NodeRef recordFolder = generateRecordFolder();
        
        // given that the record is already a child of the record folder
        makeChildrenOf(recordFolder, record);
        
        // set expected exception
        exception.expect(AlfrescoRuntimeException.class);
        
        // link
        recordService.link(record, recordFolder);
    }
    
    /**
     * Given a record that is not a child of a record folder
     * When I link the record to the record folder
     * Then the record is now linked to the record folder
     */
    @Test
    public void linkRecordToRecordFolder()
    {
        NodeRef record = generateRecord();
        NodeRef recordFolder = generateRecordFolder();
        
        // given that the record is already a child of the record folder
        makeChildrenOf(generateRecordFolder(), record);
        
        // set the name of the record
        String name = generateText(); 
        doReturn(name).when(mockedNodeService).getProperty(record, PROP_NAME);
        
        // link
        recordService.link(record, recordFolder);
        
        // verify link was created
        verify(mockedNodeService, times(1)).addChild(
                    recordFolder, 
                    record, 
                    ContentModel.ASSOC_CONTAINS, 
                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name));        
    }
    
    /**
     * Given invalid types
     * When unlinking 
     * Then exception thrown
     */
    @Test
    public void unlinkNonRecord()
    {
        NodeRef nonRecord = generateNodeRef(TYPE_CONTENT);
        NodeRef recordFolder = generateRecordFolder();
        
        // set expected exception
        exception.expect(AlfrescoRuntimeException.class);
        
        // unlink
        recordService.unlink(nonRecord, recordFolder);        
    }    
    @Test
    public void unlinkNonRecordFolder()
    {
        NodeRef record = generateRecord();
        NodeRef nonRecordFolder = generateNodeRef(TYPE_FOLDER);
        
        // set expected exception
        exception.expect(AlfrescoRuntimeException.class);
        
        // unlink
        recordService.unlink(record, nonRecordFolder);
    }
    
    /**
     * Given a record folder is a records primary parent
     * When I try and unlink the record from that record folder
     * Then an exception is thrown
     */
    @Test
    public void unlinkRecordFromPrimaryRecordFolder()
    {
        NodeRef record = generateRecord();
        NodeRef recordFolder = generateRecordFolder();
        
        // given that the record is already a child of the record folder        
        makePrimaryParentOf(record, recordFolder);
        
        // set expected exception
        exception.expect(AlfrescoRuntimeException.class);
        
        // link
        recordService.unlink(record, recordFolder);
    }
    
    /**
     * Given a record that is linked to a record
     * And that the record is not the primary parent of the record
     * When I unlink the record to the record folder
     * Then the record is no longer linked to the record folder
     */
    @Test
    public void unlinkRecordFromRecordFolder()
    {
        NodeRef record = generateRecord();
        NodeRef recordFolder = generateRecordFolder();
        
        // the records primary parent is another record folder
        makePrimaryParentOf(record, generateRecordFolder());
        
        // unlink
        recordService.unlink(record, recordFolder);
        
        // verify link was created
        verify(mockedNodeService, times(1)).removeChild(recordFolder, record);       
    }    
}
