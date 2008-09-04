/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
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
package org.apache.abdera.ext.cmis;

import org.apache.abdera.factory.Factory;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.ElementWrapper;


/**
 * CMIS Repository Info for the Abdera ATOM library.
 * 
 * Encapsulates access and modification of CMIS extension values to ATOM
 * Service Document.
 * 
 * NOTE: Potentially, this extension can be contributed to Abdera upon
 *       publication of CMIS.  This is why it is organised under a
 *       non-Alfresco Java package.  It follows the conventions of all
 *       other Abdera extensions.
 * 
 * @author davidc
 */
public class CMISCapabilities extends ElementWrapper
{
    public CMISCapabilities(Element internal)
    {
        super(internal);
    }

    public CMISCapabilities(Factory factory)
    {
        super(factory, CMISConstants.REPOSITORY_INFO);
    }
    
    public boolean hasMultifiling()
    {
        Element child = getFirstChild(CMISConstants.CAPABILITY_MULTIFILING);
        return Boolean.valueOf(child.getText());
    }

    public boolean hasUnfiling()
    {
        Element child = getFirstChild(CMISConstants.CAPABILITY_UNFILING);
        return Boolean.valueOf(child.getText());
    }
    
    public boolean hasVersionSpecificFiling()
    {
        Element child = getFirstChild(CMISConstants.CAPABILITY_VERSION_SPECIFIC_FILING);
        return Boolean.valueOf(child.getText());
    }

    public boolean isPWCUpdatable()
    {
        Element child = getFirstChild(CMISConstants.CAPABILITY_PWC_UPDATEABLE);
        return Boolean.valueOf(child.getText());
    }

    public boolean isAllVersionsSearchable()
    {
        Element child = getFirstChild(CMISConstants.CAPABILITY_ALL_VERIONS_SEARCHABLE);
        return Boolean.valueOf(child.getText());
    }

    public String getJoin()
    {
        Element child = getFirstChild(CMISConstants.CAPABILITY_JOIN);
        return child.getText();
    }

    public String getFullText()
    {
        Element child = getFirstChild(CMISConstants.CAPABILITY_FULLTEXT);
        return child.getText();
    }

}
