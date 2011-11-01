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


import static org.alfresco.util.schemacomp.SchemaCompTestingUtils.columns;
import static org.alfresco.util.schemacomp.SchemaCompTestingUtils.dumpDiffs;
import static org.alfresco.util.schemacomp.SchemaCompTestingUtils.dumpValidation;
import static org.alfresco.util.schemacomp.SchemaCompTestingUtils.fk;
import static org.alfresco.util.schemacomp.SchemaCompTestingUtils.fkeys;
import static org.alfresco.util.schemacomp.SchemaCompTestingUtils.indexes;
import static org.alfresco.util.schemacomp.SchemaCompTestingUtils.pk;
import static org.alfresco.util.schemacomp.SchemaCompTestingUtils.table;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;

import java.util.Arrays;
import java.util.Iterator;

import org.alfresco.util.schemacomp.Difference.Where;
import org.alfresco.util.schemacomp.model.Schema;
import org.alfresco.util.schemacomp.model.Table;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.MySQL5InnoDBDialect;
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
    private Dialect dialect;
    
    @Before
    public void setup()
    {
        left = new Schema("left_schema");
        right = new Schema("right_schema");
        dialect = new MySQL5InnoDBDialect();
    }
    

    @Test
    public void canPerformDiff()
    {
        // Left hand side's database objects.
        left.add(new Table(left, "tbl_no_diff", columns("id NUMBER(10)", "nodeRef VARCHAR2(200)", "name VARCHAR2(150)"), 
                    pk("pk_tbl_no_diff", "id"), fkeys(fk("fk_tbl_no_diff", "nodeRef", "node", "nodeRef")),
                    indexes("idx_node id nodeRef")));
        left.add(table("table_in_left"));
        left.add(new Table(left, "tbl_has_diff_pk", columns("id NUMBER(10)", "nodeRef VARCHAR2(200)"),
                    pk("pk_is_diff", "id"), fkeys(), indexes("idx_one id nodeRef", "idx_two id")));
        
        // Right hand side's database objects.
        right.add(new Table(right, "tbl_no_diff", columns("id NUMBER(10)", "nodeRef VARCHAR2(200)", "name VARCHAR2(150)"), 
                    pk("pk_tbl_no_diff", "id"), fkeys(fk("fk_tbl_no_diff", "nodeRef", "node", "nodeRef")),
                    indexes("idx_node id nodeRef")));
        right.add(new Table(right, "tbl_has_diff_pk", columns("id NUMBER(10)", "nodeRef VARCHAR2(200)"),
                    pk("pk_is_diff", "nodeRef"), fkeys(), indexes("idx_one id nodeRef", "idx_two [unique] id")));
        right.add(table("table_in_right"));
        
        
        comparator = new SchemaComparator(left, right, dialect);
        comparator.validateAndCompare();
        
        // See stdout for diagnostics dump...
        dumpDiffs(comparator.getDifferences(), false);
        dumpValidation(comparator.getValidationResults());
        

        Results differences = comparator.getDifferences();
        
        Iterator<Difference> it = differences.iterator();
        
        // Schema names are different ("left_schema" vs "right_schema")
        Difference diff = it.next();
        assertEquals(Where.IN_BOTH_BUT_DIFFERENCE, diff.getWhere());
        assertEquals("left_schema.name", diff.getLeft().getPath());
        assertEquals("right_schema.name", diff.getRight().getPath());
        assertSame(left, diff.getLeft().getDbObject());
        assertSame(right, diff.getRight().getDbObject());
        assertEquals("name", diff.getLeft().getPropertyName());
        assertEquals("left_schema", diff.getLeft().getPropertyValue());
        assertEquals("name", diff.getRight().getPropertyName());
        assertEquals("right_schema", diff.getRight().getPropertyValue());
        
        // Table table_in_left only appears in the left schema
        diff = it.next();
        assertEquals(Where.ONLY_IN_LEFT, diff.getWhere());
        assertEquals("left_schema.table_in_left", diff.getLeft().getPath());
        assertEquals(null, diff.getRight());
        assertEquals(null, diff.getLeft().getPropertyName());
        assertEquals(null, diff.getLeft().getPropertyValue());
        
        // Table tbl_has_diff_pk has PK of "id" in left and "nodeRef" in right
        diff = it.next();
        assertEquals(Where.ONLY_IN_LEFT, diff.getWhere());
        assertEquals("left_schema.tbl_has_diff_pk.pk_is_diff.columnNames[0]", diff.getLeft().getPath());
        assertEquals("right_schema.tbl_has_diff_pk.pk_is_diff.columnNames", diff.getRight().getPath());
        assertEquals("columnNames[0]", diff.getLeft().getPropertyName());
        assertEquals("id", diff.getLeft().getPropertyValue());
        assertEquals("columnNames", diff.getRight().getPropertyName());
        assertEquals(Arrays.asList("nodeRef"), diff.getRight().getPropertyValue());
        
        // Table tbl_has_diff_pk has PK of "id" in left and "nodeRef" in right
        diff = it.next();
        assertEquals(Where.ONLY_IN_RIGHT, diff.getWhere());
        assertEquals("left_schema.tbl_has_diff_pk.pk_is_diff.columnNames", diff.getLeft().getPath());
        assertEquals("right_schema.tbl_has_diff_pk.pk_is_diff.columnNames[0]", diff.getRight().getPath());
        assertEquals("columnNames", diff.getLeft().getPropertyName());
        assertEquals(Arrays.asList("id"), diff.getLeft().getPropertyValue());
        assertEquals("columnNames[0]", diff.getRight().getPropertyName());
        assertEquals("nodeRef", diff.getRight().getPropertyValue());
        
        // idx_two is unique in the righ_schema but not in the left
        diff = it.next();
        assertEquals("left_schema.tbl_has_diff_pk.idx_two.unique", diff.getLeft().getPath());
        assertEquals("right_schema.tbl_has_diff_pk.idx_two.unique", diff.getRight().getPath());
        assertEquals("unique", diff.getLeft().getPropertyName());
        assertEquals(false, diff.getLeft().getPropertyValue());
        assertEquals("unique", diff.getRight().getPropertyName());
        assertEquals(true, diff.getRight().getPropertyValue());
        
        // Table table_in_right does not exist in the left schema
        diff = it.next();
        assertEquals(Where.ONLY_IN_RIGHT, diff.getWhere());
        assertEquals("right_schema.table_in_right", diff.getRight().getPath());
        assertEquals(null, diff.getLeft());
        assertEquals(null, diff.getRight().getPropertyName());
        assertEquals(null, diff.getRight().getPropertyValue());
        
        assertFalse("There should be no more differences", it.hasNext());
    }
}
