/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.util.schemacomp.model;

import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.util.schemacomp.ComparisonUtils;
import org.alfresco.util.schemacomp.DbObjectVisitor;
import org.alfresco.util.schemacomp.DiffContext;
import org.alfresco.util.schemacomp.Differences;
import org.alfresco.util.schemacomp.Result.Strength;
import org.alfresco.util.schemacomp.ValidationResult;
import org.hibernate.dialect.Dialect;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Abstract base class for tests for AbstractDbObject subclasses.
 * 
 * @author Matt Ward
 */
@RunWith(MockitoJUnitRunner.class)
public abstract class DbObjectTestBase<T extends AbstractDbObject>
{
    protected @Mock Dialect dialect;
    protected @Mock Differences differences;
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
        ctx = new DiffContext(dialect, differences, new ArrayList<ValidationResult>());
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
        thisObject.diff(thatObject, ctx, Strength.ERROR);
        
        // The name of the object should be pushed on to the differences path.
        inOrder.verify(differences).pushPath(thisObject.getName());
        
        // The name of the object should be diffed
        inOrder.verify(comparisonUtils).compareSimple(
                    thisObject.getName(),
                    thatObject.getName(),
                    ctx,
                    thisObject.getNameStrength());
        
        // Then the doDiff() method should be processed...
        doDiffTests();
        
        // Later, the path should be popped again
        inOrder.verify(differences).popPath();
    }
    
    protected abstract void doDiffTests();
}
