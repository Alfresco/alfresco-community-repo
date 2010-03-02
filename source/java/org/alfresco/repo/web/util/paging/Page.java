/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.web.util.paging;


/**
 * A Page within a Cursor.
 * 
 * @author davidc
 */
public class Page
{
    Paging.PageType pageType;
    boolean zeroBasedIdx;
    int startIdx;
    int pageSize;
    
    /**
     * Construct
     * 
     * @param pageType  Page or Window
     * @param zeroBasedIdx  true => start index from 0
     * @param startIdx  start index
     * @param pageSize  page size
     */
    /*package*/ Page(Paging.PageType pageType, boolean zeroBasedIdx, int startIdx, int pageSize)
    {
        this.pageType = pageType;
        this.zeroBasedIdx = zeroBasedIdx;
        this.startIdx = startIdx;
        this.pageSize = pageSize;
    }

    /**
     * Gets the Page Type
     * 
     * @return  page type
     */
    /*package*/ Paging.PageType getType()
    {
        return pageType;
    }
    
    /**
     * Gets the page number
     * 
     * @return  page number
     */
    public int getNumber()
    {
        return startIdx;
    }
    
    /**
     * Gets the page size
     * 
     * @return  page size
     */
    public int getSize()
    {
        return pageSize;
    }

    /**
     * Is zero based page index
     * 
     * @return  true => page number starts from zero
     */
    public boolean isZeroBasedIdx()
    {
        return zeroBasedIdx;
    }
    
}
