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

import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.BaseUnitTest;
import org.alfresco.service.namespace.QName;
import org.junit.Test;
import org.mockito.InjectMocks;

/**
 * Unit test for RecordMetadataBootstrap
 * 
 * @author Roy Wetherall
 */
public class RecordMetadataBootstrapUnitTest extends BaseUnitTest
{
    @InjectMocks private RecordMetadataBootstrap bootstrap;
    
    /**
     * Test init method to ensure set map will register correctly with record service.
     */
    @Test
    public void testInit()
    {
        // create and set map
        Map<String, String> map = new HashMap<String, String>(2);
        map.put("rma:test1", "rma:filePlan");
        map.put("rma:test2", "rma:filePlan");
        bootstrap.setRecordMetadataAspects(map);
        
        // call init
        bootstrap.init();
        
        // verify that the metedata aspects where registered
        QName test1 = QName.createQName(RM_URI, "test1");
        QName test2 = QName.createQName(RM_URI, "test2");
        verify(mockedRecordService).registerRecordMetadataAspect(test1, TYPE_FILE_PLAN);
        verify(mockedRecordService).registerRecordMetadataAspect(test2, TYPE_FILE_PLAN);
    }
}
