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

import org.apache.abdera.ext.cmis.CMISProperty.CMISPropertyBoolean;
import org.apache.abdera.ext.cmis.CMISProperty.CMISPropertyDateTime;
import org.apache.abdera.ext.cmis.CMISProperty.CMISPropertyDecimal;
import org.apache.abdera.ext.cmis.CMISProperty.CMISPropertyHtml;
import org.apache.abdera.ext.cmis.CMISProperty.CMISPropertyId;
import org.apache.abdera.ext.cmis.CMISProperty.CMISPropertyInteger;
import org.apache.abdera.ext.cmis.CMISProperty.CMISPropertyString;
import org.apache.abdera.ext.cmis.CMISProperty.CMISPropertyUri;
import org.apache.abdera.ext.cmis.CMISProperty.CMISPropertyXml;
import org.apache.abdera.util.AbstractExtensionFactory;


/**
 * CMIS Extension Factory for the Abdera ATOM Library.
 * 
 * Encapsulates access and modification of CMIS extension values to ATOM.
 * 
 * NOTE: Potentially, this extension can be contributed to Abdera upon
 *       publication of CMIS.  This is why it is organised under a
 *       non-Alfresco Java package.  It follows the conventions of all
 *       other Abdera extensions.
 * 
 * @author davidc
 */
public class CMISExtensionFactory extends AbstractExtensionFactory
    implements CMISConstants
{
    
    public CMISExtensionFactory()
    {
        super(CMIS_200805_NS);
        addImpl(REPOSITORY_INFO, CMISRepositoryInfo.class);
        addImpl(CAPABILITIES, CMISCapabilities.class);
        addImpl(OBJECT, CMISObject.class);
        addImpl(PROPERTIES, CMISProperties.class);
        addImpl(PROPERTY_STRING, CMISPropertyString.class);
        addImpl(PROPERTY_DECIMAL, CMISPropertyDecimal.class);
        addImpl(PROPERTY_INTEGER, CMISPropertyInteger.class);
        addImpl(PROPERTY_BOOLEAN, CMISPropertyBoolean.class);
        addImpl(PROPERTY_DATETIME, CMISPropertyDateTime.class);
        addImpl(PROPERTY_URI, CMISPropertyUri.class);
        addImpl(PROPERTY_ID, CMISPropertyId.class);
        addImpl(PROPERTY_XML, CMISPropertyXml.class);
        addImpl(PROPERTY_HTML, CMISPropertyHtml.class);
    }

}
