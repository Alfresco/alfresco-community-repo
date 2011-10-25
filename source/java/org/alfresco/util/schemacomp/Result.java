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



/**
 * Result of a comparison between two database objects.
 * 
 * @author Matt Ward
 */
public final class Result
{
    /** Specifies the type of differences */
    public enum Where { ONLY_IN_LEFT, ONLY_IN_RIGHT, IN_BOTH_NO_DIFFERENCE, IN_BOTH_BUT_DIFFERENCE };
    public enum Strength { WARN, ERROR };
    private final Where where;
    private final Object left;
    private final Object right;
    private final String path;
    private final Strength strength;
    
    public Result(Where where, Object left, Object right, String path)
    {
        this(where, left, right, path, null);
    }
    
    public Result(Where where, Object left, Object right, String path, Strength strength)
    {
        this.where = where;
        this.left = left;
        this.right = right;
        this.path = path;
        this.strength = (strength != null ? strength : Strength.ERROR);
    }


    /**
     * @return the where
     */
    public Where getWhere()
    {
        return this.where;
    }


    /**
     * @return the left
     */
    public Object getLeft()
    {
        return this.left;
    }


    /**
     * @return the right
     */
    public Object getRight()
    {
        return this.right;
    }

    
    
    /**
     * @return the path
     */
    public String getPath()
    {
        return this.path;
    }

    
    
    /**
     * @return the strength
     */
    public Strength getStrength()
    {
        return this.strength;
    }

    
    
    @Override
    public String toString()
    {
        return "Result [where=" + this.where + ", left=" + this.left + ", right=" + this.right
                    + ", path=" + this.path + ", strength=" + this.strength + "]";
    }
}
