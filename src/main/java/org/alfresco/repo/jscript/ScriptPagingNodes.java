/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.jscript;

import java.io.Serializable;

import org.mozilla.javascript.Scriptable;

/**
 * Response for page of ScriptNode results
 * 
 * @author janv
 * @version 4.0
 */
public class ScriptPagingNodes implements Serializable
{
    private static final long serialVersionUID = -3252996649397737176L;
    
    private Scriptable results;           // array of script nodes
    private Boolean hasMoreItems;         // true if has more items (past this page) - note: could also indicate cutoff/trimmed page
    private int totalResultCountLower;    // possible total count lower estimate (-1 => unknown)
    private int totalResultCountUpper;    // possible total count upper estimate (-1 => unknown)
    
    public ScriptPagingNodes(Scriptable results, Boolean hasMoreItems, int totalResultCountLower, int totalResultCountUpper)
    {
        this.results = results;
        this.hasMoreItems = hasMoreItems;
        this.totalResultCountLower = totalResultCountLower;
        this.totalResultCountUpper = totalResultCountUpper;
    }
    
    public Scriptable getPage()
    {
        return results;
    }
    
    public Boolean hasMoreItems()
    {
        return hasMoreItems;
    }
    
    public int getTotalResultCountLower()
    {
        return totalResultCountLower;
    }
    
    public int getTotalResultCountUpper()
    {
        return totalResultCountUpper;
    }
}
