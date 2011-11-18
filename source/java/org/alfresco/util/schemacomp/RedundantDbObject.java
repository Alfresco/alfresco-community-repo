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

import org.alfresco.util.schemacomp.model.DbObject;

/**
 * If more than one DB item in the target schema matches a reference DB item
 * then this result will be issued.
 * 
 * @author Matt Ward
 */
public class RedundantDbObject extends Result
{
    private final static int SHOW_MAX_MATCHES = 3;
    private DbObject dbObject;
    private List<DbObject> matches;
    
    public RedundantDbObject(DbObject dbObject, List<DbObject> matches)
    {
        super(null);
        this.dbObject = dbObject;
        this.matches = matches;
    }

    @Override
    public String describe()
    {
        StringBuffer sb = new StringBuffer();
        sb.append(matches.size())
            .append(" redundant items? reference: ")
            .append(dbObject)
            .append(", matches: ");
        
        for (int i = 0; i < matches.size() && i < SHOW_MAX_MATCHES; i++)
        {
            if (i > 0)
            {
                sb.append(", ");
            }
            sb.append(matches.get(i));
        }
        
        if (matches.size() > SHOW_MAX_MATCHES)
        {
            sb.append(" and ")
                .append(matches.size() - SHOW_MAX_MATCHES).append(" more...");
            
        }
        return sb.toString();
    }

    @Override
    public String toString()
    {
        return "RedundantDbObject [dbObject=" + this.dbObject + ", matches=" + this.matches + "]";
    }
}
