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
package org.alfresco.repo.admin.patch;

import java.util.Date;

/**
 * Data on applied patches
 * 
 * @author Derek Hulley
 */
public interface PatchInfo
{
    public String getId();

    public String getDescription();
    
    public int getFixesFromSchema();
    
    public int getFixesToSchema();
    
    public int getTargetSchema();
    
    public int getAppliedToSchema();
    
    public String getAppliedToServer();
    
    public Date getAppliedOnDate();
    
    public boolean getWasExecuted();
    
    public boolean getSucceeded();
    
    public String getReport();
}
