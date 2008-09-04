/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.cmis.search;

/**
 * The meta data associated with a result set
 * 
 * @author andyh
 *
 */
public interface CMISResultSetMetaData
{
    /**
     * The selector meta-data.
     * @return - the selector meta-data.
     */
    public CMISResultSetSelector[] getSelectors();
    
    /**
     * The column meta-data.
     * @return - the column meta-data.
     */
    public CMISResultSetColumn[] getColumns();
    
    /**
     * Get the query options used to create this result set
     * @return the query options
     */
    public CMISQueryOptions getQueryOptions();
    
    /**
     * Get the names of the selectors.
     * @return - the selector names.
     */
    public String[] getSelectorNames();
    
    /**
     * Get the column names.
     * @return - the names of the columns.
     */
    public String[] getColumnNames();
    
    /**
     * Get the selector meta-data by name.
     * @param name
     * @return - the selector meta-data.
     */
    public CMISResultSetSelector getSelector(String name);
    
    /**
     * Get the column meta-data by column name.
     * @param name
     * @return - the column meta-data.
     */
    public CMISResultSetColumn getCoumn(String name);
}
