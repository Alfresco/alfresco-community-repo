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

import java.util.List;

import org.alfresco.util.Pair;
import org.alfresco.util.schemacomp.model.DbObject;

/**
 * Result of a comparison between two database objects.
 * 
 * @author Matt Ward
 */
public class Result
{
    public enum Type { ONLY_IN_LEFT, ONLY_IN_RIGHT, IN_BOTH_NO_DIFFERENCE, IN_BOTH_BUT_DIFFERENCE };
    private Type type;
    
    // Field list, where differences lie - recorded for single level only, otherwise, an error deep down would end
    // up having the whole schema as being different, but that isn't useful. During reporting, climb back up the tree
    // to produce a path, e.g. "my_schema.my_table.my_column.nullable has differences"
    
    
    // Could hold the two items that (may - see type above) differ?
    // These objects are already structured, no field names required.
    DbObject left;
    DbObject right;
    
    // or, could...
    // Have differences
    
    
    public static class DiffField
    {
        String leftFieldName;
        String leftVal;
        
        String rightFieldName;
        String rightVal;
    }
    
}
