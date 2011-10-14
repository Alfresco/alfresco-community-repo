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

import org.alfresco.util.schemacomp.model.DbObject;
import org.alfresco.util.schemacomp.model.Schema;

/**
 * TODO: comment me!
 * @author Matt Ward
 */
public class SchemaComparator
{
    private Schema leftSchema;
    private Schema rightSchema;
    
    /**
     * Construct a comparator to compare schemas left and right.
     * 
     * @param left
     * @param right
     */
    public SchemaComparator(Schema left, Schema right)
    {
        this.leftSchema = left;
        this.rightSchema = right;
    }
    
    public void compare()
    {
        for (DbObject leftObj : leftSchema)
        {
            DbObject rightObj = rightSchema.get(leftObj.getIdentifier());
            if (rightObj != null)
            {
                // There is an equivalent object in the right hand schema as in the left.
                System.out.println("Both schemas have object: " + leftObj.getIdentifier() + 
                            "(" + leftObj.getClass().getSimpleName() + ")");
            }
            else
            {
                // No equivalent object in the right hand schema.
                System.out.println("No matching object in right schema: " + leftObj.getIdentifier() + 
                            "(" + leftObj.getClass().getSimpleName() + ")");
            }
        }
        
        // Identify objects in the right schema but not the left
        for (DbObject rightObj : rightSchema)
        {
            if (!leftSchema.contains(rightObj.getIdentifier()))
            {
             // No equivalent object in the left hand schema.
                System.out.println("No matching object in left schema: " + rightObj.getIdentifier() + 
                            "(" + rightObj.getClass().getSimpleName() + ")");
            }
        }
    }
}
