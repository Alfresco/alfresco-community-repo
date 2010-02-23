/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
package org.alfresco.cmis.acl;

import org.alfresco.cmis.EnumFactory;
import org.alfresco.cmis.EnumLabel;

/**
 * Part two of the allowable action key for the permission mappings
 * @author andyh
 *
 */
public enum CMISAllowedActionKeyTypeEnum implements EnumLabel
{
    /**
     * Folder
     */
    FOLDER("Folder"),
    /**
     * Object
     */
    OBJECT("Object"),
    /**
     * Source
     */
    SOURCE("Source"),
    /**
     * Target
     */
    TARGET("Target"),
    /**
     * Document
     */
    DOCUMENT("Document"),
    /**
     * Policy
     */
    POLICY("Policy");
    
    private String label;

    /**
     * Construct
     * 
     * @param label
     */
    CMISAllowedActionKeyTypeEnum(String label)
    {
        this.label = label;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.cmis.EnumLabel#label()
     */
    public String getLabel()
    {
        return label;
    }

    /**
     * Factory for CMISAclCapabilityEnum
     */
    public static EnumFactory<CMISAllowedActionKeyTypeEnum> FACTORY = new EnumFactory<CMISAllowedActionKeyTypeEnum>(CMISAllowedActionKeyTypeEnum.class, null, true);

    
    
}
