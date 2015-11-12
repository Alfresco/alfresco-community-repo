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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see http://www.gnu.org/licenses/.
 */

package org.alfresco.repo.virtual.store;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.security.permissions.NodePermissionEntry;
import org.alfresco.repo.security.permissions.PermissionReference;
import org.alfresco.repo.virtual.VirtualizationException;
import org.alfresco.repo.virtual.ref.ClasspathResource;
import org.alfresco.repo.virtual.ref.Encodings;
import org.alfresco.repo.virtual.ref.Protocols;
import org.alfresco.repo.virtual.ref.Reference;
import org.alfresco.repo.virtual.template.FilingData;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;
import org.alfresco.util.Pair;
import org.junit.Test;
import org.mockito.Mockito;

public class ReferenceComparatorTest extends TestCase
{
    private static QName PROP_BOOLEAN_1 = QName.createQName("tst:bool1");

    private static QName PROP_BOOLEAN_2 = QName.createQName("tst:bool2");

    private static QName PROP_INTEGER_1 = QName.createQName("tst:int1");

    private static QName PROP_INTEGER_2 = QName.createQName("tst:int2");

    private static final Reference TEST_REFERENCE_1 = new Reference(Encodings.PLAIN.encoding,
                                                                    Protocols.VIRTUAL.protocol,
                                                                    new ClasspathResource("/some/class/path.js"));

    private static final Reference TEST_REFERENCE_2 = new Reference(Encodings.PLAIN.encoding,
                                                                    Protocols.VIRTUAL.protocol,
                                                                    new ClasspathResource("/some/other/class/path.js"));

    private static final Reference TEST_REFERENCE_3 = new Reference(Encodings.PLAIN.encoding,
                                                                    Protocols.VIRTUAL.protocol,
                                                                    new ClasspathResource("/and/an/other/class/path.js"));

    private VirtualStore testPropertiesStore = null;

    @Override
    protected void setUp() throws Exception
    {

        testPropertiesStore = Mockito.mock(VirtualStore.class);
        {
            final Map<QName, Serializable> ref1Props = new HashMap<>();

            ref1Props.put(PROP_INTEGER_1,
                          1);
            ref1Props.put(PROP_INTEGER_2,
                          2);
            ref1Props.put(PROP_BOOLEAN_1,
                          true);
            ref1Props.put(PROP_BOOLEAN_2,
                          false);

            Mockito.when(testPropertiesStore.getProperties(TEST_REFERENCE_1)).thenReturn(ref1Props);
        }
        {
            final Map<QName, Serializable> ref2Props = new HashMap<>();

            ref2Props.put(PROP_INTEGER_1,
                          1);
            ref2Props.put(PROP_INTEGER_2,
                          3);
            ref2Props.put(PROP_BOOLEAN_1,
                          false);
            ref2Props.put(PROP_BOOLEAN_2,
                          true);

            Mockito.when(testPropertiesStore.getProperties(TEST_REFERENCE_2)).thenReturn(ref2Props);
        }
        {
            final Map<QName, Serializable> ref3Props = new HashMap<>();

            ref3Props.put(PROP_INTEGER_1,
                          4);
            ref3Props.put(PROP_INTEGER_2,
                          5);
            ref3Props.put(PROP_BOOLEAN_1,
                          true);
            ref3Props.put(PROP_BOOLEAN_2,
                          false);

            Mockito.when(testPropertiesStore.getProperties(TEST_REFERENCE_3)).thenReturn(ref3Props);
        }

    }

    @Test
    public void testCompare1() throws Exception
    {
        ReferenceComparator c = new ReferenceComparator(testPropertiesStore,
                                                        Arrays.asList(new Pair<QName, Boolean>(PROP_INTEGER_1,
                                                                                               true),
                                                                      new Pair<QName, Boolean>(PROP_BOOLEAN_1,
                                                                                               true)));

        assertEquals(0,
                     c.compare(TEST_REFERENCE_1,
                               TEST_REFERENCE_1));
        assertEquals(1,
                     c.compare(TEST_REFERENCE_1,
                               TEST_REFERENCE_2));
        assertEquals(-1,
                     c.compare(TEST_REFERENCE_2,
                               TEST_REFERENCE_1));
        assertEquals(-1,
                     c.compare(TEST_REFERENCE_1,
                               TEST_REFERENCE_3));
    }
}
