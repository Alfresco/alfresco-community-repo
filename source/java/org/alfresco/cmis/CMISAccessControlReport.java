/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
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
     * Is this report incomplete?
     * If so there are other other security constraints that apply
     * 
     * @return <code>false</code> means the report fully describes security access, <code>true</code> means other
     * security constraints apply but are not reported. 
     */
    public boolean isExtract();
    
    /**
     * Get ACEs grouped by principal id
     * @return
     * @throws CMISConstraintException 
     */
    public List<? extends CMISAccessControlEntriesGroupedByPrincipalId> getAccessControlEntriesGroupedByPrincipalId() throws CMISConstraintException;
    
    
}
