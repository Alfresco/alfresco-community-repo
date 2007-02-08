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
package org.alfresco.repo.node.integrity;

import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Thrown when an integrity check fails
 * 
 * @author Derek Hulley
 */
public class IntegrityException extends AlfrescoRuntimeException
{
    private static final long serialVersionUID = -5036557255854195669L;

    private List<IntegrityRecord> records;
    
    public IntegrityException(List<IntegrityRecord> records)
    {
        super("Integrity failure");
        this.records = records;
    }

    /**
     * @return Returns a list of all the integrity violations
     */
    public List<IntegrityRecord> getRecords()
    {
        return records;
    }
}
