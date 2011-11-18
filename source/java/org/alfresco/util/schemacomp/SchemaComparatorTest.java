/*
 * Copytarget (C) 2005-2011 Alfresco Software Limited.
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
    private Schema reference;
    private Schema target;
    private Dialect dialect;
    
    @Before
    public void setup()
    {
        reference = new Schema("schema");
        target = new Schema("schema");
        dialect = new MySQL5InnoDBDialect();
    }
    

    @Test
    public void canPerformDiff()
    {
        // Reference schema's database objects.
        reference.add(new Table(reference, "tbl_no_diff", columns("id NUMBER(10)", "nodeRef VARCHAR2(200)", "name VARCHAR2(150)"), 
                    pk("pk_tbl_no_diff", "id"), fkeys(fk("fk_tbl_no_diff", "nodeRef", "node", "nodeRef")),
                    indexes("idx_node id nodeRef")));
        reference.add(table("table_in_reference"));
        reference.add(new Table(reference, "tbl_has_diff_pk", columns("id NUMBER(10)", "nodeRef VARCHAR2(200)"),
                    pk("pk_is_diff", "id"), fkeys(), indexes("idx_one id nodeRef", "idx_two id")));
        
        // Target schema's database objects.
        target.add(new Table(target, "tbl_no_diff", columns("id NUMBER(10)", "nodeRef VARCHAR2(200)", "name VARCHAR2(150)"), 
                    pk("pk_tbl_no_diff", "id"), fkeys(fk("fk_tbl_no_diff", "nodeRef", "node", "nodeRef")),
                    indexes("idx_node id nodeRef")));
        target.add(new Table(target, "tbl_has_diff_pk", columns("id NUMBER(10)", "nodeRef VARCHAR2(200)"),
                    pk("pk_is_diff", "nodeRef"), fkeys(), indexes("idx_one id nodeRef", "idx_two [unique] id")));
        target.add(table("table_in_target"));
        
        
        comparator = new SchemaComparator(reference, target, dialect);
        comparator.validateAndCompare();
        
        // See stdout for diagnostics dump...
        dumpDiffs(comparator.getComparisonResults(), false);
        dumpValidation(comparator.getComparisonResults());
        

        Results results = comparator.getComparisonResults();
        
        Iterator<Result> it = results.iterator();
        
        // Table table_in_reference only appears in the reference schema
        Difference diff = (Difference) it.next();
        assertEquals(Where.ONLY_IN_LEFT, diff.getWhere());
        assertEquals("schema.table_in_reference", diff.getLeft().getPath());
        assertEquals(null, diff.getRight());
        assertEquals(null, diff.getLeft().getPropertyName());
        assertEquals(null, diff.getLeft().getPropertyValue());
        
        // Table tbl_has_diff_pk has PK of "id" in reference and "nodeRef" in target
        diff = (Difference) it.next();
        assertEquals(Where.ONLY_IN_LEFT, diff.getWhere());
        assertEquals("schema.tbl_has_diff_pk.pk_is_diff.columnNames[0]", diff.getLeft().getPath());
        assertEquals("schema.tbl_has_diff_pk.pk_is_diff.columnNames", diff.getRight().getPath());
        assertEquals("columnNames[0]", diff.getLeft().getPropertyName());
        assertEquals("id", diff.getLeft().getPropertyValue());
        assertEquals("columnNames", diff.getRight().getPropertyName());
        assertEquals(Arrays.asList("nodeRef"), diff.getRight().getPropertyValue());
        
        // Table tbl_has_diff_pk has PK of "id" in reference and "nodeRef" in target
        diff = (Difference) it.next();
        assertEquals(Where.ONLY_IN_RIGHT, diff.getWhere());
        assertEquals("schema.tbl_has_diff_pk.pk_is_diff.columnNames", diff.getLeft().getPath());
        assertEquals("schema.tbl_has_diff_pk.pk_is_diff.columnNames[0]", diff.getRight().getPath());
        assertEquals("columnNames", diff.getLeft().getPropertyName());
        assertEquals(Arrays.asList("id"), diff.getLeft().getPropertyValue());
        assertEquals("columnNames[0]", diff.getRight().getPropertyName());
        assertEquals("nodeRef", diff.getRight().getPropertyValue());
        
        // idx_two is unique in the righ_schema but not in the reference
        diff = (Difference) it.next();
        assertEquals("schema.tbl_has_diff_pk.idx_two.unique", diff.getLeft().getPath());
        assertEquals("schema.tbl_has_diff_pk.idx_two.unique", diff.getRight().getPath());
        assertEquals("unique", diff.getLeft().getPropertyName());
        assertEquals(false, diff.getLeft().getPropertyValue());
        assertEquals("unique", diff.getRight().getPropertyName());
        assertEquals(true, diff.getRight().getPropertyValue());
        
        // Table table_in_target does not exist in the reference schema
        diff = (Difference) it.next();
        assertEquals(Where.ONLY_IN_RIGHT, diff.getWhere());
        assertEquals("schema.table_in_target", diff.getRight().getPath());
        assertEquals(null, diff.getLeft());
        assertEquals(null, diff.getRight().getPropertyName());
        assertEquals(null, diff.getRight().getPropertyValue());
        
        assertFalse("There should be no more differences", it.hasNext());
    }
}
