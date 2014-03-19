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
package org.alfresco.module.org_alfresco_module_rm;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.identifier.IdentifierService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.alfresco.util.collections.CollectionUtils;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Unit test for RecordServiceImpl
 * 
 * @author Roy Wetherall
 */
public class BaseUnitTest implements RecordsManagementModel
{
    protected NodeRef filePlanComponent;
    protected NodeRef filePlan;
    
    /** core service mocks */
    @Mock(name="nodeService")           protected NodeService mockedNodeService; 
    @Mock(name="dictionaryService")     protected DictionaryService mockedDictionaryService;
    @Mock(name="namespaceService")      protected NamespaceService mockedNamespaceService; 
    @Mock(name="identifierService")     protected IdentifierService mockedIdentifierService;
    
    @Mock(name="filePlanService")       protected FilePlanService mockedFilePlanService;
    
    /**
     * Test method setup
     */
    @Before
    public void before()
    {
        MockitoAnnotations.initMocks(this);

        filePlan = generateNodeRef(TYPE_FILE_PLAN);
        filePlanComponent = generateNodeRef();
        setupAsFilePlanComponent(filePlanComponent);
        
        // set-up namespace service
        when(mockedNamespaceService.getNamespaceURI(RM_PREFIX)).thenReturn(RM_URI);
        when(mockedNamespaceService.getPrefixes(RM_URI)).thenReturn(CollectionUtils.unmodifiableSet(RM_PREFIX));
        
    }
    
    /**
     * Helper method to generate a qname.
     * 
     * @return  QName   qualified name
     */
    protected static QName generateQName()
    {
        return QName.createQName(RM_URI, GUID.generate());
    }
    
    protected NodeRef generateRecordFolder()
    {
        NodeRef recordFolder = generateNodeRef(TYPE_RECORD_FOLDER);
        setupAsFilePlanComponent(recordFolder);       
        return recordFolder;
        
    }
    
    protected NodeRef generateRecord()
    {
        NodeRef record = generateNodeRef(ContentModel.TYPE_CONTENT);
        setupAsFilePlanComponent(record);
        doReturn(true).when(mockedNodeService).hasAspect(record, ASPECT_RECORD);        
        return record;
    }
    
    protected void setupAsFilePlanComponent(NodeRef nodeRef)
    {
        doReturn(true).when(mockedNodeService).hasAspect(nodeRef, ASPECT_FILE_PLAN_COMPONENT);
        doReturn(filePlan).when(mockedFilePlanService).getFilePlan(nodeRef);
        doReturn(filePlan).when(mockedNodeService).getProperty(nodeRef, PROP_ROOT_NODEREF);
    }
        
    protected NodeRef generateNodeRef()
    {
        return generateNodeRef(null);
    }
    
    protected NodeRef generateNodeRef(QName type)
    {
        NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, GUID.generate());
        when(mockedNodeService.exists(nodeRef)).thenReturn(true);
        if (type != null)
        {
            when(mockedNodeService.getType(nodeRef)).thenReturn(type);
        }
        return nodeRef;
    }
}
