/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.repo.domain;

import java.util.Date;

import org.alfresco.repo.admin.patch.PatchInfo;

/**
 * Interface for persistent patch application information.
 * 
 * @author Derek Hulley
 */
public interface AppliedPatch extends PatchInfo
{
    public void setId(String id);

    public void setDescription(String description);
    
    public void setFixesFromSchema(int version);
    
    public void setFixesToSchema(int version);
    
    public void setTargetSchema(int version);
    
    public void setAppliedToSchema(int version);
    
    public void setAppliedToServer(String server);
    
    public void setAppliedOnDate(Date date);
    
    public void setWasExecuted(boolean executed);
    
    public void setSucceeded(boolean succeeded);
    
    public void setReport(String report);
}
