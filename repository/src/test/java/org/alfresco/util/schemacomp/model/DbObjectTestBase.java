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
package org.alfresco.util.schemacomp.model;

import static org.mockito.Mockito.inOrder;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.alfresco.repo.domain.dialect.Dialect;
import org.alfresco.util.schemacomp.ComparisonUtils;
import org.alfresco.util.schemacomp.DbObjectVisitor;
import org.alfresco.util.schemacomp.DbProperty;
import org.alfresco.util.schemacomp.DiffContext;
import org.alfresco.util.schemacomp.Results;

/**
 * Abstract base class for tests for AbstractDbObject subclasses.
 * 
 * @author Matt Ward
 */
@RunWith(MockitoJUnitRunner.class)
public abstract class DbObjectTestBase<T extends AbstractDbObject>
{
    @Mock
    protected Dialect dialect;
    protected @Mock Results differences;
    protected DiffContext ctx;
    protected @Mock ComparisonUtils comparisonUtils;
    protected InOrder inOrder;

    protected abstract T getThisObject();

    protected abstract T getThatObject();

    protected @Mock DbObjectVisitor visitor;

    @Before
    public final void baseSetUp()
    {
        // Check that the correct calls happened in the correct order.
        List<Object> mocks = getMocksUsedInDiff();
        inOrder = inOrder(mocks.toArray());
        ctx = new DiffContext(dialect, differences, null, null);
    }

    /**
     * Override to add additional mocks to the InOrder call verification.
     * 
     * @return List<Object>
     */
    protected List<Object> getMocksUsedInDiff()
    {
        List<Object> objects = new ArrayList<Object>();
        objects.add(differences);
        objects.add(comparisonUtils);
        return objects;
    }

    @Test
    public void canDiffObjects()
    {
        AbstractDbObject thisObject = getThisObject();
        thisObject.setComparisonUtils(comparisonUtils);
        AbstractDbObject thatObject = getThatObject();
        thatObject.setComparisonUtils(comparisonUtils);

        // Invoke the method under test
        thisObject.diff(thatObject, ctx);

        // The name of the object should be diffed
        inOrder.verify(comparisonUtils).compareSimple(
                new DbProperty(thisObject, "name"),
                new DbProperty(thatObject, "name"),
                ctx);

        // Then the doDiff() method should be processed...
        doDiffTests();
    }

    protected abstract void doDiffTests();
}
