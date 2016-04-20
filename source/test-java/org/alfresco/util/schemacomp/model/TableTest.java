package org.alfresco.util.schemacomp.model;


import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.test_category.BaseSpringTestsCategory;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Tests for the Table class.
 * 
 * @author Matt Ward
 */
@RunWith(MockitoJUnitRunner.class)
@Category(BaseSpringTestsCategory.class)
public class TableTest extends DbObjectTestBase<Table>
{
    private Table table;
    private Table otherTable;
    private List<Column> columns;
    private @Mock PrimaryKey primaryKey;
    private List<ForeignKey> foreignKeys;
    private List<Index> indexes;
    
    
    @Before
    public void setUp() throws Exception
    {
        columns = listOfMocks(Column.class, 3);
        foreignKeys = listOfMocks(ForeignKey.class, 1);
        indexes = listOfMocks(Index.class, 1);
        table = new Table(null, "the_table", columns, primaryKey, foreignKeys, indexes);
        otherTable = new Table(null, "the_other_table", columns, primaryKey, foreignKeys, indexes);
    }
    
    
    private <T> List<T> listOfMocks(Class<T> c, int size)
    {
        List<T> list = new ArrayList<T>(size);
        for (int i = 0; i < size; i++)
        {
            list.add((T) Mockito.mock(c));
        }
        return list;
    }


    @Override
    protected List<Object> getMocksUsedInDiff()
    {
        List<Object> mocks = super.getMocksUsedInDiff();
        mocks.add(primaryKey);
        return mocks;
    }

    public void doDiffTests()
    {
        // Check columns
        inOrder.verify(comparisonUtils).compareCollections(table.getColumns(), otherTable.getColumns(), ctx);
        
        // Check primary key
        inOrder.verify(primaryKey).diff(otherTable.getPrimaryKey(), ctx);
        
        // Check foreign keys
        inOrder.verify(comparisonUtils).compareCollections(
                    table.getForeignKeys(), otherTable.getForeignKeys(), ctx);
                
        // Check indexes
        inOrder.verify(comparisonUtils).compareCollections(
                    table.getIndexes(), otherTable.getIndexes(), ctx);
    }

    @Override
    protected Table getThisObject()
    {
        return table;
    }

    @Override
    protected Table getThatObject()
    {
        return otherTable;
    }
    
    
    @Test
    public void acceptVisitor()
    {
       table.setColumns(columns);
       table.setForeignKeys(foreignKeys);
       table.setIndexes(indexes);
       table.setPrimaryKey(primaryKey);
       
       table.accept(visitor);
       
       // All the children should be visited
       List<DbObject> children = new ArrayList<DbObject>();
       children.addAll(columns);
       children.addAll(foreignKeys);
       children.addAll(indexes);
       children.add(primaryKey);
       
       for (DbObject child : children)
       {
           verify(child).accept(visitor);
       }
       
       // The parent itself should be visited
       verify(visitor).visit(table);
    }
}
