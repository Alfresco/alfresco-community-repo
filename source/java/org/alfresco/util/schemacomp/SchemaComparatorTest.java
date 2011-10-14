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


import org.alfresco.util.schemacomp.model.Schema;
import org.alfresco.util.schemacomp.model.Table;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * TODO: comment me!
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
        left = new Schema();
        right = new Schema();
    }

    @Test
    public void canDetermineSameTables()
    {
        left.put(new Table("alf_node"));
        left.put(new Table("table_in_left"));
        left.put(new Table("in_both_but_different"));
        right.put(new Table("alf_node"));
        // Note this table is in different position in the RHS list.
        Table rightTable = new Table("in_both_but_different");
        right.put(rightTable);
        right.put(new Table("table_in_right"));
        
        comparator = new SchemaComparator(left, right);
        comparator.compare();
    }
}
