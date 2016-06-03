package org.alfresco.util.schemacomp.model;

import static org.mockito.Mockito.verify;

import org.alfresco.test_category.BaseSpringTestsCategory;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Tests for the Sequence class.
 * @author Matt Ward
 */
@Category(BaseSpringTestsCategory.class)
public class SequenceTest extends DbObjectTestBase<Sequence>
{
    private Sequence thisSequence;
    private Sequence thatSequence;

    @Before
    public void setUp()
    {
        thisSequence = new Sequence(null, "this_sequence");
        thatSequence = new Sequence(null, "that_sequence");
    }
    
    @Test
    public void acceptVisitor()
    {
       thisSequence.accept(visitor);
       
       verify(visitor).visit(thisSequence);
    }

    @Override
    protected Sequence getThisObject()
    {
        return thisSequence;
    }

    @Override
    protected Sequence getThatObject()
    {
        return thatSequence;
    }

    @Override
    protected void doDiffTests()
    {
        // Nothing extra to diff.
    }
}
