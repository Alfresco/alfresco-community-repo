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


import static java.util.Arrays.asList;

import java.util.Collection;
import java.util.List;

import org.alfresco.util.schemacomp.Result.Strength;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Tests for the Table class.
 * 
 * @author Matt Ward
 */
@RunWith(MockitoJUnitRunner.class)
public class TableTest extends DbObjectTestBase<Table>
{
    private Table table;
    private Table otherTable;
    private Collection<Column> columns;
    private @Mock PrimaryKey primaryKey;
    private Collection<ForeignKey> foreignKeys;
    private Collection<Index> indexes;
    
    
    @Before
    public void setUp() throws Exception
    {
        columns = asList(
                    new Column("columnA", "VARCHAR2(50)", false),
                    new Column("columnB", "VARCHAR2(100)", false),
                    new Column("columnC", "VARCHAR2(200)", true));
        
        foreignKeys = asList(new ForeignKey("fk", "localcolumn", "targettable", "targetcolumn"));
        
        indexes = asList(new Index("an_index", asList("columnA", "columnC")));
        
        table = new Table("the_table", columns, primaryKey, foreignKeys, indexes);
        otherTable = new Table("the_other_table", columns, primaryKey, foreignKeys, indexes);
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
        inOrder.verify(comparisonUtils).compareCollections(table.getColumns(), otherTable.getColumns(), differences);
        
        // Check primary key
        inOrder.verify(primaryKey).diff(otherTable.getPrimaryKey(), differences, Strength.ERROR);
        
        // Check foreign keys
        inOrder.verify(comparisonUtils).compareCollections(
                    table.getForeignKeys(), otherTable.getForeignKeys(), differences);
                
        // Check indexes
        inOrder.verify(comparisonUtils).compareCollections(
                    table.getIndexes(), otherTable.getIndexes(), differences);
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
}
