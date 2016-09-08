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
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest;
import org.alfresco.service.cmr.repository.NodeRef;
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
    public void before()
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
}
