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

import org.alfresco.cmis.CMISDataTypeEnum;
import org.alfresco.cmis.CMISPropertyDefinition;
import org.alfresco.cmis.CMISResultSetColumn;
import org.alfresco.service.namespace.QName;

/**
 * @author andyh
 *
 */
public class CMISResultSetColumnImpl implements CMISResultSetColumn
{

    private String name;
    
    private CMISPropertyDefinition propertyDefinition;
    
    private CMISDataTypeEnum dataType;
    
    private QName alfrescoPropertyQName;
    
    private QName alfrescoDataTypeQName;
    
    CMISResultSetColumnImpl(String name, CMISPropertyDefinition propertyDefinition, CMISDataTypeEnum dataType, QName alfrescoPropertyQName, QName alfrescoDataTypeQName)
    {
        this.name = name;
        this.propertyDefinition = propertyDefinition;
        this.dataType = dataType;
        this.alfrescoPropertyQName = alfrescoPropertyQName;
        this.alfrescoDataTypeQName = alfrescoDataTypeQName;
    }
    
    
    /* (non-Javadoc)
     * @see org.alfresco.cmis.search.CMISResultSetColumn#getName()
     */
    public String getName()
    {
        return name;
    }

    /* (non-Javadoc)
     * @see org.alfresco.cmis.search.CMISResultSetColumn#getPropertyDefinition()
     */
    public CMISPropertyDefinition getCMISPropertyDefinition()
    {
        return propertyDefinition;
    }

    /* (non-Javadoc)
     * @see org.alfresco.cmis.search.CMISResultSetColumn#getPropertyType()
     */
    public CMISDataTypeEnum getCMISDataType()
    {
       return dataType;
    }


    public QName getDataType()
    {
        return alfrescoDataTypeQName;
    }


    public QName getPropertyType()
    {
        return alfrescoPropertyQName;
    }

}
