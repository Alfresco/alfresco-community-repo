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

import static org.mockito.Mockito.when;

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
    protected static NodeRef FILE_PLAN_COMPONENT    = generateNodeRef();
    protected static NodeRef FILE_PLAN              = generateNodeRef();
    
    @Mock(name="nodeService")       protected NodeService mockedNodeService; 
    @Mock(name="dictionaryService") protected DictionaryService mockedDictionaryService;
    @Mock(name="namespaceService")  protected NamespaceService mockedNamespaceService; 
    @Mock(name="identifierService") protected IdentifierService mockedIdentifierService;
    
    @Before
    public void before()
    {
        MockitoAnnotations.initMocks(this);
        
        // set-up node service 
        when(mockedNodeService.getProperty(FILE_PLAN_COMPONENT, PROP_ROOT_NODEREF)).thenReturn(FILE_PLAN);
        when(mockedNodeService.getType(FILE_PLAN)).thenReturn(TYPE_FILE_PLAN);
        
        // set-up namespace service
        when(mockedNamespaceService.getNamespaceURI(RM_PREFIX)).thenReturn(RM_URI);
        when(mockedNamespaceService.getPrefixes(RM_URI)).thenReturn(CollectionUtils.unmodifiableSet(RM_PREFIX));
        
    }
    
    protected static QName generateQName()
    {
        return QName.createQName(RM_URI, GUID.generate());
    }
    
    protected static NodeRef generateNodeRef()
    {
        return new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, GUID.generate());
    }
}
