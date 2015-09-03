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

package org.alfresco.module.org_alfresco_module_rm.util;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

import static org.alfresco.module.org_alfresco_module_rm.test.util.ExceptionUtils.expectedException;
import static org.junit.Assert.assertEquals;

import static java.util.Arrays.asList;

import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Unit tests for {@link CoreServicesExtras}.
 *
 * @author Neil Mc Erlean
 * @since 2.4.a
 */
public class CoreServicesExtrasUnitTest
{
    private CoreServicesExtras serviceExtras;

    private final QName   testAspect = QName.createQName("test", "aspect");
    private final QName   testProp1  = QName.createQName("test", "prop1");
    private final QName   testProp2  = QName.createQName("test", "prop2");
    private final QName   testProp3  = QName.createQName("test", "prop3");
    private final NodeRef testNode1  = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "1");
    private final NodeRef testNode2  = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "2");

    @Before public void setUp()
    {
        serviceExtras = new CoreServicesExtras();
    }

    @Test public void copyingAnUnknownAspectShouldResultInAnException()
    {
        final DictionaryService mockDS = mock(DictionaryService.class);

        when(mockDS.getAspect(any(QName.class))).thenReturn(null);
        serviceExtras.setDictionaryService(mockDS);

        expectedException(DictionaryException.class, () -> serviceExtras.copyAspect(testNode1, testNode2, testAspect));
    }

    @Test public void copyingAnAspectWithNoProperties()
    {
        final DictionaryService mockDS = mock(DictionaryService.class);
        final NodeService       mockNS = mock(NodeService.class);

        final AspectDefinition mockAspect = mock(AspectDefinition.class);
        when(mockAspect.getProperties()).thenReturn(Collections.emptyMap());

        when(mockDS.getAspect(eq(testAspect))).thenReturn(mockAspect);
        when(mockNS.getProperties(eq(testNode1))).thenReturn(Collections.emptyMap());
        serviceExtras.setDictionaryService(mockDS);
        serviceExtras.setNodeService(mockNS);

        assertEquals(Collections.emptyMap(), serviceExtras.copyAspect(testNode1, testNode2, testAspect));
    }

    @Test public void copyingAnAspectWithProperties()
    {
        final DictionaryService mockDS = mock(DictionaryService.class);
        final NodeService       mockNS = mock(NodeService.class);

        final AspectDefinition mockAspect = mock(AspectDefinition.class);
        when(mockAspect.getName()).thenReturn(testAspect);

        final Map<QName, PropertyDefinition> props = new HashMap<>();
        final PropertyDefinition mockProp1 = mock(PropertyDefinition.class);
        final PropertyDefinition mockProp2 = mock(PropertyDefinition.class);
        for (PropertyDefinition p : asList(mockProp1, mockProp2))
        {
            when(p.getContainerClass()).thenReturn(mockAspect);
        }
        props.put(testProp1, mockProp1);
        props.put(testProp2, mockProp2);
        when(mockAspect.getProperties()).thenReturn(props);

        final Map<QName, Serializable> propVals = new HashMap<>();
        propVals.put(testProp1, "one");
        propVals.put(testProp2, "two");
        propVals.put(testProp3, "three"); // Not defined on the aspect above.
        when(mockDS.getAspect(eq(testAspect))).thenReturn(mockAspect);
        when(mockNS.getProperties(eq(testNode1))).thenReturn(propVals);

        serviceExtras.setDictionaryService(mockDS);
        serviceExtras.setNodeService(mockNS);

        Map<QName, Serializable> expected = new HashMap<>();
        expected.put(testProp1, "one");
        expected.put(testProp2, "two");
        assertEquals(expected, serviceExtras.copyAspect(testNode1, testNode2, testAspect));
    }
}
