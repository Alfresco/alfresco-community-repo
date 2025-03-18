/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.security.permissions.impl.acegi;

import java.util.ArrayList;
import java.util.ListIterator;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.search.results.ChildAssocRefResultSet;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.namespace.QName;

public class FilteringResultSetTest extends TestCase
{

    public FilteringResultSetTest()
    {
        super();
    }

    public FilteringResultSetTest(String arg0)
    {
        super(arg0);
    }

    public void test()
    {
        StoreRef storeRef = new StoreRef("protocol", "test");
        NodeRef root = new NodeRef(storeRef, "n0");
        NodeRef n1 = new NodeRef(storeRef, "n1");
        NodeRef n2 = new NodeRef(storeRef, "n2");
        NodeRef n3 = new NodeRef(storeRef, "n3");
        NodeRef n4 = new NodeRef(storeRef, "n4");
        NodeRef n5 = new NodeRef(storeRef, "n5");

        ArrayList<ChildAssociationRef> cars = new ArrayList<ChildAssociationRef>();
        ChildAssociationRef car0 = new ChildAssociationRef(null, null, null, root);
        ChildAssociationRef car1 = new ChildAssociationRef(ContentModel.ASSOC_CHILDREN, root, QName.createQName("{test}n2"), n1);
        ChildAssociationRef car2 = new ChildAssociationRef(ContentModel.ASSOC_CHILDREN, n1, QName.createQName("{test}n3"), n2);
        ChildAssociationRef car3 = new ChildAssociationRef(ContentModel.ASSOC_CHILDREN, n2, QName.createQName("{test}n4"), n3);
        ChildAssociationRef car4 = new ChildAssociationRef(ContentModel.ASSOC_CHILDREN, n3, QName.createQName("{test}n5"), n4);
        ChildAssociationRef car5 = new ChildAssociationRef(ContentModel.ASSOC_CHILDREN, n4, QName.createQName("{test}n6"), n5);
        cars.add(car0);
        cars.add(car1);
        cars.add(car2);
        cars.add(car3);
        cars.add(car4);
        cars.add(car5);

        ResultSet in = new ChildAssocRefResultSet(null, cars);

        FilteringResultSet filtering = new FilteringResultSet(in);

        assertEquals(0, filtering.length());
        for (int i = 0; i < 6; i++)
        {
            filtering.setIncluded(i, true);
            assertEquals(1, filtering.length());
            assertEquals("n" + i, filtering.getNodeRef(0).getId());
            assertEquals(1, filtering.getNodeRefs().size());
            assertEquals(1, filtering.getChildAssocRefs().size());
            assertEquals("n" + i, filtering.getNodeRefs().get(0).getId());
            filtering.setIncluded(i, false);
            assertEquals(0, filtering.length());
        }

        for (int i = 0; i < 6; i++)
        {
            filtering.setIncluded(i, true);
            assertEquals(i + 1, filtering.length());
            assertEquals("n" + i, filtering.getNodeRef(i).getId());
        }

        int count = 0;
        for (ResultSetRow row : filtering)
        {
            assertNotNull(row);
            assertTrue(count < 6);
            count++;
        }

        ResultSetRow last = null;
        for (ListIterator<ResultSetRow> it = filtering.iterator(); it.hasNext(); /**/)
        {
            ResultSetRow row = it.next();
            if (last != null)
            {
                assertTrue(it.hasPrevious());
                ResultSetRow previous = it.previous();
                assertEquals(last.getIndex(), previous.getIndex());
                row = it.next();

            }
            else
            {
                assertFalse(it.hasPrevious());
            }
            last = row;

        }
    }

}
