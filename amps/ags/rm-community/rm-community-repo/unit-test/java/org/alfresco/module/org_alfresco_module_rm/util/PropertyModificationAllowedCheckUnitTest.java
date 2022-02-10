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


import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Unit test class for PropertyModificationAllowedCheck
 *
 * @author Ross Gale
 * @since 3.2
 */
public class PropertyModificationAllowedCheckUnitTest
{

    private PropertyModificationAllowedCheck propertyModificationAllowedCheck;

    private Map<QName, Serializable> before, after;

    private QName qName, qName2;

    private List<QName> list;
    private List<String> editableURIs;

    @Mock
    private Serializable serializable, serializable2;

    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
        propertyModificationAllowedCheck = new PropertyModificationAllowedCheck();
        before = new HashMap();
        after = new HashMap();
        qName = QName.createQName("foo", "bar");
        qName2 = QName.createQName("bar", "foo");
        before.put(qName, serializable);
        after.put(qName, serializable2);
        list = new ArrayList();
        editableURIs = new ArrayList<>();
        propertyModificationAllowedCheck.setWhiteList(list);
        propertyModificationAllowedCheck.setEditableURIs(editableURIs);
    }

    /**
     * Test modification check passes when property is in whitelist
     */
    @Test
    public void testCheckMethodReturnsTrueWhenPropertyInList()
    {
        list.add(qName);
        propertyModificationAllowedCheck.setWhiteList(list);
        assertTrue(propertyModificationAllowedCheck.check(before, after));
    }

    /**
     * Test modification check fails when property is not in whitelist
     */
    @Test
    public void testCheckMethodReturnsFalseIfAnyNonAllowedPropertyInListIsChanged()
    {
        list.add(qName);
        before.put(qName2, serializable2);
        after.put(qName2, serializable);
        propertyModificationAllowedCheck.setWhiteList(list);
        assertFalse(propertyModificationAllowedCheck.check(before, after));
    }

    /**
     * Test modification check fails when first property is not in whitelist
     */
    @Test
    public void testCheckMethodReturnsFalseIfFirstPropertyInListIsChangedWithoutWhitelist()
    {
        list.add(qName2);
        before.put(qName2, serializable2);
        after.put(qName2, serializable);
        propertyModificationAllowedCheck.setWhiteList(list);
        assertFalse(propertyModificationAllowedCheck.check(before, after));
    }

    /**
     * Test modification check passes when all properties are in whitelist
     */
    @Test
    public void testCheckMethodReturnsTrueIfAllEditedPropertiesInWhitelist()
    {
        list.add(qName);
        list.add(qName2);
        before.put(qName2, serializable2);
        after.put(qName2, serializable);
        propertyModificationAllowedCheck.setWhiteList(list);
        assertTrue(propertyModificationAllowedCheck.check(before, after));
    }

    /**
     * Test modification check fails when property added
     */
    @Test
    public void testCheckMethodReturnsFalseIfPropertyNotInBeforeList()
    {
        list.add(qName);
        after.put(qName2, serializable);
        propertyModificationAllowedCheck.setWhiteList(list);
        assertFalse(propertyModificationAllowedCheck.check(before, after));
    }

    /**
     * Test modification check passes when allowed property added
     */
    @Test
    public void testCheckMethodReturnsTrueIfAllowedPropertyNotInBeforeList()
    {
        list.add(qName2);
        after.put(qName2, serializable);
        propertyModificationAllowedCheck.setWhiteList(list);
        assertFalse(propertyModificationAllowedCheck.check(before, after));
    }

    /**
     * Test modification check fails when property removed
     */
    @Test
    public void testCheckMethodReturnsFalseIfPropertyNotInAfterList()
    {
        list.add(qName);
        before.put(qName2, serializable);
        propertyModificationAllowedCheck.setWhiteList(list);
        assertFalse(propertyModificationAllowedCheck.check(before, after));
    }

    /**
     * Test modification check passes when allowed property removed
     */
    @Test
    public void testCheckMethodReturnsTrueIfAllowedPropertyNotInAfterList()
    {
        list.add(qName);
        list.add(qName2);
        before.put(qName2, serializable);
        propertyModificationAllowedCheck.setWhiteList(list);
        assertTrue(propertyModificationAllowedCheck.check(before, after));
    }

    /**
     * Test modification check for empty property in before map without whitelist
     */
    @Test
    public void testNullValueInBeforeList()
    {
        before.put(qName, null);
        propertyModificationAllowedCheck.setWhiteList(list);
        assertFalse(propertyModificationAllowedCheck.check(before, after));
    }

    /**
     * Test modification check for empty property in after map without whitelist
     */
    @Test
    public void testNullValueInAfterList()
    {
        after.put(qName, null);
        propertyModificationAllowedCheck.setWhiteList(list);
        assertFalse(propertyModificationAllowedCheck.check(before, after));
    }

    /**
     * Test modification check for empty property in before map with whitelist
     */
    @Test
    public void testNullValueInBeforeListWithAllowedProperty()
    {
        list.add(qName);
        before.put(qName, null);
        propertyModificationAllowedCheck.setWhiteList(list);
        assertTrue(propertyModificationAllowedCheck.check(before, after));
    }

    /**
     * Test modification check for empty property in after list with whitelist
     */
    @Test
    public void testNullValueInAfterListWithAllowedProperty()
    {
        list.add(qName);
        after.put(qName, null);
        propertyModificationAllowedCheck.setWhiteList(list);
        assertTrue(propertyModificationAllowedCheck.check(before, after));
    }

    /**
     * Test modification check for empty property in both maps
     */
    @Test
    public void testNullValueInBoth()
    {
        before.put(qName, null);
        after.put(qName, null);
        assertTrue(propertyModificationAllowedCheck.check(before, after));
    }

    /**
     * Test update of a property from the model URI for which properties can be updated
     */
    @Test
    public void testUpdatePropertyFromAllowedModelURI()
    {
        editableURIs.add("foo");
        propertyModificationAllowedCheck.setEditableURIs(editableURIs);
        assertTrue(propertyModificationAllowedCheck.check(before, after));
    }

    /**
     * Test update of a property that is not in the model URI for which properties can be updated
     */
    @Test
    public void testUpdatePropertyFromNotAllowedModelURI()
    {
        editableURIs.add("bar");
        propertyModificationAllowedCheck.setEditableURIs(editableURIs);
        assertFalse(propertyModificationAllowedCheck.check(before, after));
    }
}
