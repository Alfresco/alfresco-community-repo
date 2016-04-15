package org.alfresco.util.schemacomp.model;


import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;

import org.alfresco.test_category.BaseSpringTestsCategory;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.schemacomp.DbProperty;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Tests for the ForeignKey class.
 * 
 * @author Matt Ward
 */
@Category(BaseSpringTestsCategory.class)
public class ForeignKeyTest extends DbObjectTestBase<ForeignKey>
{
    private ForeignKey thisFK, thatFK;
    private Table parent;

    @Before
    public void setUp() throws Exception
    {
        parent = new Table("parent");
        thisFK = new ForeignKey(null, "this_fk", "local_col", "target_table", "target_col");
        thatFK = new ForeignKey(null, "that_fk", "local_col", "target_table", "target_col");
    }


    @Override
    protected ForeignKey getThisObject()
    {
        return thisFK;
    }


    @Override
    protected ForeignKey getThatObject()
    {
        return thatFK;
    }


    @Override
    protected void doDiffTests()
    {
        inOrder.verify(comparisonUtils).compareSimple(
                    new DbProperty(thisFK, "localColumn"),
                    new DbProperty(thatFK, "localColumn"),
                    ctx);
        inOrder.verify(comparisonUtils).compareSimple(
                    new DbProperty(thisFK, "targetTable"),
                    new DbProperty(thatFK, "targetTable"),
                    ctx);
        inOrder.verify(comparisonUtils).compareSimple(
                    new DbProperty(thisFK, "targetColumn"),
                    new DbProperty(thatFK, "targetColumn"),
                    ctx);
    }
    
    @Test
    public void acceptVisitor()
    {
       thisFK.accept(visitor);
       
       verify(visitor).visit(thisFK);
    }
    
    @Test
    public void sameAs()
    {
        // FKs are the same if they have all the same properties
        thisFK = new ForeignKey(parent, "the_fk", "local_col", "target_table", "target_col");
        thatFK = new ForeignKey(parent, "the_fk", "local_col", "target_table", "target_col");
        assertTrue("FKs should be considered the same", thisFK.sameAs(thatFK));
        
        // FKs are the same even if they have different names (but all other properties are the same)
        thisFK = new ForeignKey(parent, "the_fk", "local_col", "target_table", "target_col");
        thatFK = new ForeignKey(parent, "different_name", "local_col", "target_table", "target_col");
        assertTrue("FKs should be considered the same", thisFK.sameAs(thatFK));
        
        // Two references to the same FK are the same of course
        assertTrue("FKs should be considered the same", thisFK.sameAs(thisFK));
        
        // A null is never the same
        assertFalse("FKs should be considered the different", thisFK.sameAs(null));
       
        thisFK = new ForeignKey(parent, "the_fk", "local_col", "target_table", "target_col");
        thatFK = new ForeignKey(new Table("different_parent"), "the_fk", "local_col", "target_table", "target_col");
        assertFalse("FKs should be different: parents are different.", thisFK.sameAs(thatFK));
        
        thisFK = new ForeignKey(parent, "the_fk", "local_col", "target_table", "target_col");
        thatFK = new ForeignKey(parent, "the_fk", "local_col2", "target_table", "target_col");
        assertFalse("FKs have different local columns.", thisFK.sameAs(thatFK));
        
        thisFK = new ForeignKey(parent, "the_fk", "local_col", "target_table", "target_col");
        thatFK = new ForeignKey(parent, "the_fk", "local_col", "target_table2", "target_col");
        assertFalse("FKs have different target table.", thisFK.sameAs(thatFK));
        
        thisFK = new ForeignKey(parent, "the_fk", "local_col", "target_table", "target_col");
        thatFK = new ForeignKey(parent, "the_fk", "local_col", "target_table", "target_col2");
        assertFalse("FKs have different target column.", thisFK.sameAs(thatFK));

        // ALF-14129 fix test
        thisFK = new ForeignKey(parent, "the_fk", "local_col", "target_table", "target_col");
        thatFK = new ForeignKey(parent, "the_fk", "local_col", "TARGET_TABLE", "target_col");
        assertTrue("FKs are case sensitive to targetTable's name.", thisFK.sameAs(thatFK));
    }
}
