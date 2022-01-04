/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.module.org_alfresco_module_rm.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.alfresco.module.org_alfresco_module_rm.test.util.AlfMock;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Unit test class for NodeTypeUtility
 *
 * @author Claudia Agache
 * @since 3.2
 */
public class NodeTypeUtilityUnitTest
{
    @InjectMocks
    private NodeTypeUtility nodeTypeUtility;

    @Mock
    private DictionaryService mockedDictionaryService;

    private QName type, ofType;

    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
        type = AlfMock.generateQName();
        ofType = AlfMock.generateQName();
    }

    /** test that instanceOf returns false if verified type is not subtype of the other */
    @Test
    public void testNotInstanceOf()
    {
        when(mockedDictionaryService.isSubClass(type, ofType)).thenReturn(false);
        assertFalse(nodeTypeUtility.instanceOf(type, ofType));
    }

    /** test that instanceOf returns true if verified type is subtype of the other */
    @Test
    public void testIsInstanceOf()
    {
        when(mockedDictionaryService.isSubClass(type, ofType)).thenReturn(true);
        assertTrue(nodeTypeUtility.instanceOf(type, ofType));
    }

    /** test that instanceOf checks the cache when verifying the same type twice */
    @Test
    public void testInstanceOfCacheSameTypes()
    {
        nodeTypeUtility.instanceOf(type, ofType);
        nodeTypeUtility.instanceOf(type, ofType);
        verify(mockedDictionaryService, times(1)).isSubClass(any(), any());
    }

    /** test the invocations when verifying different types */
    @Test
    public void testInstanceOfDifferentTypes()
    {
        QName anotherType = AlfMock.generateQName();
        nodeTypeUtility.instanceOf(type, ofType);
        nodeTypeUtility.instanceOf(anotherType, ofType);
        verify(mockedDictionaryService, times(2)).isSubClass(any(), any());
    }

    /** test that instanceOf returns true if verified type is equal to the other */
    @Test
    public void testTypesAreEqual()
    {
        assertTrue(nodeTypeUtility.instanceOf(type, type));
        verify(mockedDictionaryService, times(0)).isSubClass(any(), any());
    }
}
