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
package org.alfresco.repo.search;

import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.PermissionEvaluationMode;
import org.alfresco.service.cmr.search.ResultSetMetaData;
import org.alfresco.service.cmr.search.SearchParameters;

/**
 * Simple implementatio of result set meta data.
 * 
 * @author Andy Hind
 */
public class SimpleResultSetMetaData implements ResultSetMetaData
{
    private LimitBy limitedBy; 
    
    private PermissionEvaluationMode permissoinEvaluationMode;
    
    private SearchParameters searchParameters;
    
    
    public SimpleResultSetMetaData(LimitBy limitedBy, PermissionEvaluationMode permissoinEvaluationMode, SearchParameters searchParameters)
    {
        super();
        this.limitedBy = limitedBy;
        this.permissoinEvaluationMode = permissoinEvaluationMode;
        this.searchParameters = searchParameters;
    }

    public LimitBy getLimitedBy()
    {
        return limitedBy;
    }

    public PermissionEvaluationMode getPermissionEvaluationMode()
    {
        return permissoinEvaluationMode;
    }

    public SearchParameters getSearchParameters()
    {
        return searchParameters;
    }

}
