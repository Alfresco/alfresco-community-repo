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
package org.alfresco.service.cmr.search;

/**
 * Meta Data associated with a result set.
 * 
 * @author Andy Hind
 */
public interface ResultSetMetaData
{
    
    /**
     * Return how, <b>in fact</b>, the result set was limited.
     * This may not be how it was requested.
     * 
     * If a limit of 100 were requested and there were 100 or less actual results
     * this will report LimitBy.UNLIMITED.
     * 
     * @return
     */
    public LimitBy getLimitedBy();
    
    /**
     * Return how permission evaluations are being made.
     * 
     * @return
     */
    public PermissionEvaluationMode getPermissionEvaluationMode();
    
    /**
     * Get the parameters that were specified to define this search.
     * 
     * @return
     */
    public SearchParameters getSearchParameters();
    
}
