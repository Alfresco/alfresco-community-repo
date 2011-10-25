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
package org.alfresco.util.schemacomp;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.alfresco.util.schemacomp.Result.Strength;
import org.alfresco.util.schemacomp.Result.Where;
import org.alfresco.util.schemacomp.model.Column;
import org.alfresco.util.schemacomp.model.DbObject;
import org.alfresco.util.schemacomp.model.ForeignKey;
import org.alfresco.util.schemacomp.model.Index;
import org.alfresco.util.schemacomp.model.PrimaryKey;
import org.alfresco.util.schemacomp.model.Schema;
import org.alfresco.util.schemacomp.model.Table;
import org.apache.commons.lang.ArrayUtils;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the SchmeaComparator class.
 * 
 * @author Matt Ward
 */
public class SchemaComparatorTest
{
    private SchemaComparator comparator;
    private Schema left;
    private Schema right;
    
    @Before
    public void setup()
    {
        left = new Schema("left_schema");
        right = new Schema("right_schema");
    }
    

    @Test
    public void canPerformDiff()
    {
        // Left hand side's database objects.
        left.add(new Table("tbl_no_diff", columns("id NUMBER(10)", "nodeRef VARCHAR2(200)", "name VARCHAR2(150)"), 
                    pk("pk_tbl_no_diff", "id"), fkeys(fk("fk_tbl_no_diff", "nodeRef", "node", "nodeRef")),
                    indexes("idx_node id nodeRef")));
        left.add(table("table_in_left"));
        left.add(new Table("tbl_has_diff_pk", columns("id NUMBER(10)", "nodeRef VARCHAR2(200)"),
                    pk("pk_is_diff", "id"), fkeys(), indexes()));
        
        // Right hand side's database objects.
        right.add(new Table("tbl_no_diff", columns("id NUMBER(10)", "nodeRef VARCHAR2(200)", "name VARCHAR2(150)"), 
                    pk("pk_tbl_no_diff", "id"), fkeys(fk("fk_tbl_no_diff", "nodeRef", "node", "nodeRef")),
                    indexes("idx_node id nodeRef")));
        right.add(new Table("tbl_has_diff_pk", columns("id NUMBER(10)", "nodeRef VARCHAR2(200)"),
                    pk("pk_is_diff", "nodeRef"), fkeys(), indexes()));
        right.add(table("table_in_right"));
        
        
        comparator = new SchemaComparator(left, right);
        comparator.compare();
        
        dumpDiffs(comparator.getDifferences(), true);

        Iterator<Result> it = comparator.getDifferences().iterator();
        
        assertHasDifference("left_schema", "left_schema", "right_schema", it.next()); // schema names
        assertNoDifference("left_schema.tbl_no_diff", "tbl_no_diff", it.next());
        assertNoDifference("left_schema.tbl_no_diff.id", "id", it.next());
        assertNoDifference("left_schema.tbl_no_diff.id", "NUMBER(10)", it.next());        
        assertNoDifference("left_schema.tbl_no_diff.id", Boolean.FALSE, it.next()); // nullable
        assertNoDifference("left_schema.tbl_no_diff.nodeRef", "nodeRef", it.next());                
        assertNoDifference("left_schema.tbl_no_diff.nodeRef", "VARCHAR2(200)", it.next());        
        assertNoDifference("left_schema.tbl_no_diff.nodeRef", Boolean.FALSE, it.next()); // nullable
        assertNoDifference("left_schema.tbl_no_diff.name", "name", it.next());                
        assertNoDifference("left_schema.tbl_no_diff.name", "VARCHAR2(150)", it.next());        
        assertNoDifference("left_schema.tbl_no_diff.name", Boolean.FALSE, it.next()); // nullable
        assertNoDifference("left_schema.tbl_no_diff.pk_tbl_no_diff", "pk_tbl_no_diff", it.next()); // name field
        assertNoDifference("left_schema.tbl_no_diff.pk_tbl_no_diff", "id", it.next()); // first (& only) column of list
        assertNoDifference("left_schema.tbl_no_diff.fk_tbl_no_diff", "fk_tbl_no_diff", it.next()); // name field
        assertNoDifference("left_schema.tbl_no_diff.fk_tbl_no_diff", "nodeRef", it.next()); // localColumn
        assertNoDifference("left_schema.tbl_no_diff.fk_tbl_no_diff", "node", it.next()); // targetTable
        assertNoDifference("left_schema.tbl_no_diff.fk_tbl_no_diff", "nodeRef", it.next()); // targetColumn
        assertNoDifference("left_schema.tbl_no_diff.idx_node", "idx_node", it.next()); // index name
        assertNoDifference("left_schema.tbl_no_diff.idx_node", "id", it.next()); // first indexed column
        assertNoDifference("left_schema.tbl_no_diff.idx_node", "nodeRef", it.next()); // second indexed column
        // TODO: why are diffs for table not flattened out as for index?
        assertOnlyInOne("left_schema", Where.ONLY_IN_LEFT, table("table_in_left"), it.next());
        
        assertNoDifference("left_schema.tbl_has_diff_pk", "tbl_has_diff_pk", it.next());
        assertNoDifference("left_schema.tbl_has_diff_pk.id", "id", it.next());
        assertNoDifference("left_schema.tbl_has_diff_pk.id", "NUMBER(10)", it.next());        
        assertNoDifference("left_schema.tbl_has_diff_pk.id", Boolean.FALSE, it.next()); // nullable
        assertNoDifference("left_schema.tbl_has_diff_pk.nodeRef", "nodeRef", it.next());                
        assertNoDifference("left_schema.tbl_has_diff_pk.nodeRef", "VARCHAR2(200)", it.next());        
        assertNoDifference("left_schema.tbl_has_diff_pk.nodeRef", Boolean.FALSE, it.next()); // nullable
        assertNoDifference("left_schema.tbl_has_diff_pk.pk_is_diff", "pk_is_diff", it.next()); // name field

        // TODO: surely this should be a diff rather than a ONLY_IN_LEFT plus ONLY_IN_RIGHT?
//        assertHasDifference("left_schema.tbl_has_diff_pk.pk_is_diff", "id", "nodeRef", it.next()); // first (& only) column of list
        assertOnlyInOne("left_schema.tbl_has_diff_pk.pk_is_diff", Where.ONLY_IN_LEFT, "id", it.next()); // first (& only) column of list
        
        // This belong to the pk_is_diff above.
        assertOnlyInOne("left_schema.tbl_has_diff_pk.pk_is_diff", Where.ONLY_IN_RIGHT, "nodeRef", it.next()); // first (& only) column of list
        
        // Items that are ONLY_IN_RIGHT always come at the end
        assertEquals("Should be table with correct name", "tbl_has_diff_pk", ((DbObject) it.next().getRight()).getName());
        assertOnlyInOne("left_schema", Where.ONLY_IN_RIGHT, table("table_in_right"), it.next());
    }
    
    
    @Test
    public void canReportWarnings()
    {
        // Left hand side's database objects.
        left.add(new Table("tbl_example", columns("id NUMBER(10)"), pk("pk_tbl_example", "id"), fkeys(),
                    indexes("idx_specified_name id")));
        
        // Right hand side's database objects.
        right.add(new Table("tbl_example", columns("id NUMBER(10)"), pk("pk_tbl_example", "id"), fkeys(),
                    indexes("sys_random_idx_name id")));
        
        
        comparator = new SchemaComparator(left, right);
        comparator.compare();
        
        dumpDiffs(comparator.getDifferences(), true);

        Iterator<Result> it = comparator.getDifferences().iterator();
        assertHasDifference("left_schema", "left_schema", "right_schema", it.next());
        assertNoDifference("left_schema.tbl_example", "tbl_example", it.next());
        assertNoDifference("left_schema.tbl_example.id", "id", it.next());
        assertNoDifference("left_schema.tbl_example.id", "NUMBER(10)", it.next());        
        assertNoDifference("left_schema.tbl_example.id", Boolean.FALSE, it.next());
        assertNoDifference("left_schema.tbl_example.pk_tbl_example", "pk_tbl_example", it.next());
        assertNoDifference("left_schema.tbl_example.pk_tbl_example", "id", it.next());
        
        assertHasWarning(
                    "left_schema.tbl_example.idx_specified_name",
                    "idx_specified_name",
                    "sys_random_idx_name",
                    it.next());
    }
    
    /**
     * Assert that the result shows the value to have different values in the left and right items.
     */
    private void assertHasDifference(String path, Object leftValue, Object rightValue,
                Result result, Strength strength)
    {
        assertEquals(strength, result.getStrength());
        assertEquals(Where.IN_BOTH_BUT_DIFFERENCE, result.getWhere());
        assertEquals(path, result.getPath());
        assertEquals(leftValue, result.getLeft());
        assertEquals(rightValue, result.getRight());
    }
    
    /**
     * @see #assertHasDifference(String, Object, Object, Result, Strength)
     */
    private void assertHasDifference(String path, Object leftValue, Object rightValue, Result result)
    {
        assertHasDifference(path, leftValue, rightValue, result, Strength.ERROR);
    }
    
    /**
     * @see #assertHasDifference(String, Object, Object, Result, Strength)
     */
    private void assertHasWarning(String path, Object leftValue, Object rightValue, Result result)
    {
        assertHasDifference(path, leftValue, rightValue, result, Strength.WARN);
    }

    /**
     * Assert that the result shows the value to be present only in either the left or right items.
     */
    private void assertOnlyInOne(String path, Where which, Object value, Result result)
    {
        assertEquals(which, result.getWhere());
        assertEquals(path, result.getPath());
        
        if (which == Where.ONLY_IN_LEFT)
        {
            assertEquals(value, result.getLeft());
            assertNull(result.getRight());
        }
        else if (which == Where.ONLY_IN_RIGHT)
        {
            assertNull(result.getLeft());            
            assertEquals(value, result.getRight());
        }
        else
        {
            throw new IllegalArgumentException("The 'which' argument should be ONLY_IN_LEFT or ONLY_IN_RIGHT.");
        }
    }

    /**
     * Assert that the result shows no differences between the left and right items.
     */
    private void assertNoDifference(String path, Object value, Result result)
    {
        assertEquals(Where.IN_BOTH_NO_DIFFERENCE, result.getWhere());
        assertEquals(path, result.getPath());
        assertEquals(value, result.getLeft());
        assertEquals(value, result.getRight());
    }

    /**
     * @param differences
     */
    private void dumpDiffs(Differences differences, boolean showNonDifferences)
    {
        System.out.println("Differences (" + differences.size() + ")");
        for (Result d : differences)
        {
            if (d.getWhere() != Where.IN_BOTH_NO_DIFFERENCE || showNonDifferences)
            {
                System.out.println(d);
            }
        }
    }

    private Table table(String name)
    {
        return new Table(name, columns("id NUMBER(10)"), pk("pk_" + name, "id"), fkeys(), indexes());
    }
    
    private Collection<Column> columns(String... colDefs)
    {
        assertTrue("Tables must have columns", colDefs.length > 0);
        Column[] columns = new Column[colDefs.length];

        for (int i = 0; i < colDefs.length; i++)
        {
            String[] parts = colDefs[i].split(" ");
            columns[i] = new Column(parts[0], parts[1], false);
        }
        return Arrays.asList(columns);
    }
    
    private PrimaryKey pk(String name, String... columnNames)
    {
        assertTrue("No columns specified", columnNames.length > 0);
        PrimaryKey pk = new PrimaryKey(name, Arrays.asList(columnNames));
        return pk;
    }
    
    private List<ForeignKey> fkeys(ForeignKey... fkeys)
    {
        return Arrays.asList(fkeys);
    }
    
    private ForeignKey fk(String fkName, String localColumn, String targetTable, String targetColumn)
    {
        return new ForeignKey(fkName, localColumn, targetTable, targetColumn);
    }
    
    private Collection<Index> indexes(String... indexDefs)
    {
        Index[] indexes = new Index[indexDefs.length];
        for (int i = 0; i < indexDefs.length; i++)
        {
            String[] parts = indexDefs[i].split(" ");
            String name = parts[0];
            String[] columns = (String[]) ArrayUtils.subarray(parts, 1, parts.length);
            indexes[i] = new Index(name, Arrays.asList(columns));
        }
        return Arrays.asList(indexes);
    }
}
