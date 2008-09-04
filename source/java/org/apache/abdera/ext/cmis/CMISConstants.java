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

import javax.xml.namespace.QName;


/**
 * CMIS Namespace definitions for the Abdera ATOM library.
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
public interface CMISConstants
{
    public static final String CMIS_200805_NS = "http://www.cmis.org/2008/05";

    public static final QName PROPERTIES  = new QName(CMIS_200805_NS, "properties");
    public static final QName OBJECTID  = new QName(CMIS_200805_NS, "objectId");
    public static final QName BASETYPE = new QName(CMIS_200805_NS, "baseType");
 
    public static final String REL_CHILDREN = "cmis-children";
    public static final String REL_PARENT = "cmis-parent";
    public static final String REL_PARENTS = "cmis-parents";
    public static final String REL_ALLVERSIONS = "cmis-allversions";
}
