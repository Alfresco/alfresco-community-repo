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
package org.alfresco.cmis;

import java.util.List;


/**
 * An Access control report.
 * This is an ACL.
 * 
 * @author andyh
 *
 */
public interface CMISAccessControlReport
{
    /**
     * Get the list of ACEs.
     * @return the list of ACEs.
     */
    public List<? extends CMISAccessControlEntry> getAccessControlEntries();
    
    /**
     * Is this report exact?
     * If <code>false</code> then there are other other security constraints that apply.
     * This will always be false as we have global permission and deny entries that are not reported.
     * We do not explicitly check these cases - and return false - as we have global permission defined by default. 
     * 
     * @return <code>true</code> means the report fully describes security access, <code>false</code> means other
     * security constraints <i>may</i> apply but are not reported. 
     */
    public boolean isExact();
    
    /**
     * Get ACEs grouped by principal id
     * @return ACEs grouped by principal id
     * @throws CMISConstraintException 
     */
    public List<? extends CMISAccessControlEntriesGroupedByPrincipalId> getAccessControlEntriesGroupedByPrincipalId() throws CMISConstraintException;
    
    
}
