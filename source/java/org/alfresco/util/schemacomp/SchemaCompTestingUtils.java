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

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.alfresco.util.schemacomp.Difference.Where;
import org.alfresco.util.schemacomp.model.Column;
import org.alfresco.util.schemacomp.model.ForeignKey;
import org.alfresco.util.schemacomp.model.Index;
import org.alfresco.util.schemacomp.model.PrimaryKey;
import org.alfresco.util.schemacomp.model.Sequence;
import org.alfresco.util.schemacomp.model.Table;
import org.apache.commons.lang.ArrayUtils;


public class SchemaCompTestingUtils
{
    public static void dumpValidation(Results results)
    {
        System.out.println("Validation Results (" + results.size() + ")");
        for (Result r : results)
        {
            if (r instanceof ValidationResult)
            {
                System.out.println(r);
            }
        }
    }
        
    public static void dumpDiffs(Results differences, boolean showNonDifferences)
    {
        System.out.println("Differences (" + differences.size() + ")");
        for (Result d : differences)
        {
            if (d instanceof Difference)
            {
                Difference diff = (Difference) d;
                if (diff.getWhere() != Where.IN_BOTH_NO_DIFFERENCE || showNonDifferences)
                {
                    System.out.println(d);
                }
            }
        }
    }

    public static Table table(String name)
    {
        return new Table(null, name, columns("id NUMBER(10)"), pk("pk_" + name, "id"), fkeys(), indexes());
    }
    
    public static Table table(String name, Collection<Column> columns, PrimaryKey primaryKey, 
                Collection<ForeignKey> foreignKeys, Collection<Index> indexes)
    {
        return new Table(null, name, columns, primaryKey, foreignKeys, indexes);
    }
    
    public static Collection<Column> columns(String... colDefs)
    {
        assertTrue("Tables must have columns", colDefs.length > 0);
        Column[] columns = new Column[colDefs.length];

        for (int i = 0; i < colDefs.length; i++)
        {
            String[] parts = colDefs[i].split(" ");
            columns[i] = new Column(null, parts[0], parts[1], false);
        }
        return Arrays.asList(columns);
    }
    
    public static PrimaryKey pk(String name, String... columnNames)
    {
        assertTrue("No columns specified", columnNames.length > 0);
        PrimaryKey pk = new PrimaryKey(null, name, Arrays.asList(columnNames));
        return pk;
    }
    
    public static List<ForeignKey> fkeys(ForeignKey... fkeys)
    {
        return Arrays.asList(fkeys);
    }
    
    public static ForeignKey fk(String fkName, String localColumn, String targetTable, String targetColumn)
    {
        return new ForeignKey(null, fkName, localColumn, targetTable, targetColumn);
    }
    
    /**
     * Create collection of indexes using strings of format "name column1 [column2 ... columnN]"
     */
    public static Collection<Index> indexes(String... indexDefs)
    {
        Index[] indexes = new Index[indexDefs.length];
        for (int i = 0; i < indexDefs.length; i++)
        {
            String[] parts = indexDefs[i].split(" ");
            String name = parts[0];
            
            boolean unique = false;
            int columnsStart = 1;
            
            if (parts[1].equals("[unique]"))
            {
                unique = true;
                columnsStart++;
            }
            
            String[] columns = (String[]) ArrayUtils.subarray(parts, columnsStart, parts.length);
            indexes[i] = new Index(null, name, Arrays.asList(columns));
            indexes[i].setUnique(unique);
        }
        return Arrays.asList(indexes);
    }
    
    
    public static Sequence sequence(String name)
    {
        return new Sequence(name);
    }
}
