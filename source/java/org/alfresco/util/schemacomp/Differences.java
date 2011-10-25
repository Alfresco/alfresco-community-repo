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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.alfresco.util.schemacomp.Result.Strength;
import org.alfresco.util.schemacomp.Result.Where;
import org.alfresco.util.schemacomp.model.DbObject;
import org.apache.commons.lang.StringUtils;

/**
 * Collects differences so that tools can report on or respond to differences between database schemas.
 * 
 * @author Matt Ward
 */
public class Differences implements Iterable<Result>
{
    private final List<Result> items = new ArrayList<Result>();
    private final Path path = new Path();
    
    
    /**
     * @see Path
     */
    public void pushPath(String component)
    {
        this.path.push(component);
    }

    /**
     * @see Path
     */
    public String popPath()
    {
        return this.path.pop();
    }

    /**
     * Record a difference between two objects, or specify that an object only appears in either the
     * 'left' or 'right' schemas.
     * 
     * @param where The type of difference, see {@link Where}
     * @param left Left value, or null if the item appears in the right, but not left schema.
     * @param right Right value, or null if the item appears in the left, but not right schema.
     * @param strength The Result.Strength of the difference, e.g. WARN or ERROR.
     */
    public void add(Where where, Object left, Object right, Strength strength)
    {
        Result result = new Result(where, left, right, path.getCurrentPath(), strength);
        items.add(result);
    }
   

    public void add(Where where, Object left, Object right)
    {
        add(where, left, right, null);
    }
    
    
    /**
     * Obtain an iterator for the top-level items held in this schema - since this is a hierarchical model,
     * deeper items are obtained by navigating through the top-level items.
     */
    @Override
    public Iterator<Result> iterator()
    {
        return items.iterator();
    }

    /**
     * @return How many top-level items are in the schema.
     */
    public int size()
    {
        return items.size();
    }
    
    /**
     * Specifies where in a database schema a difference occurs - this is largely the same as the fully qualified
     * name of a database object, e.g. <code>public.alf_node</code> except that if an object doesn't have a name
     * then a suitable label will be provided for that path element.
     * <p>
     * Elements can be <code>push</code>ed onto the path and <code>pop</code>ped from the path, making it easier for a
     * {@link DbObject} to process that object's data as part of a heirachy - the parent object should push an
     * appropriate label onto the path for itself, before invoking any child objects'
     * {@link DbObject#diff(DbObject, Differences)} method.
     * 
     * @author Matt Ward
     */
    private static class Path
    {
        private Stack<String> components = new Stack<String>();
        private String current;
        
        public void push(String component)
        {
            components.push(component);
            makeCurrentPath();
        }
        
        public String pop()
        {
            String component = components.pop();
            makeCurrentPath();
            return component;
        }
        
        public String getCurrentPath()
        {
            return current;
        }
        
        private void makeCurrentPath()
        {
            current = StringUtils.join(components, ".");
        }        
    }
}
