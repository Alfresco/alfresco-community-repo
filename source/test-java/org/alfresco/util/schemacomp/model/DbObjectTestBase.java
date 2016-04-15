package org.alfresco.util.schemacomp.model;

import static org.mockito.Mockito.inOrder;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.util.schemacomp.ComparisonUtils;
import org.alfresco.util.schemacomp.DbObjectVisitor;
import org.alfresco.util.schemacomp.DbProperty;
import org.alfresco.util.schemacomp.DiffContext;
import org.alfresco.util.schemacomp.Results;
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
