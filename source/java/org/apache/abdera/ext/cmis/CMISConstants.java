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

    // CMIS Service Document
    public static final QName COLLECTION_TYPE = new QName(CMIS_200805_NS, "collectionType");
    
    // CMIS Repository Info
    public static final QName REPOSITORY_INFO = new QName(CMIS_200805_NS, "repositoryInfo");
    public static final QName REPOSITORY_ID = new QName(CMIS_200805_NS, "repositoryId");
    public static final QName REPOSITORY_NAME = new QName(CMIS_200805_NS, "repositoryName");
    public static final QName REPOSITORY_DESCRIPTION = new QName(CMIS_200805_NS, "repositoryDescription");
    public static final QName REPOSITORY_SPECIFIC_INFO = new QName(CMIS_200805_NS, "repositorySpecificInformation");
    public static final QName VENDOR_NAME = new QName(CMIS_200805_NS, "vendorName");
    public static final QName PRODUCT_NAME = new QName(CMIS_200805_NS, "productName");
    public static final QName PRODUCT_VERSION = new QName(CMIS_200805_NS, "productVersion");
    public static final QName VERSIONS_SUPPORTED = new QName(CMIS_200805_NS, "cmisVersionsSupported");
    
    // CMIS Capabilities
    public static final QName CAPABILITIES = new QName(CMIS_200805_NS, "capabilities");
    public static final QName CAPABILITY_MULTIFILING = new QName(CMIS_200805_NS, "capabilityMultifiling");
    public static final QName CAPABILITY_UNFILING = new QName(CMIS_200805_NS, "capabilityUnfiling");
    public static final QName CAPABILITY_VERSION_SPECIFIC_FILING = new QName(CMIS_200805_NS, "capabilityVersionSpecificFiling");
    public static final QName CAPABILITY_PWC_UPDATEABLE = new QName(CMIS_200805_NS, "capabilityPWCUpdateable");
    public static final QName CAPABILITY_ALL_VERIONS_SEARCHABLE = new QName(CMIS_200805_NS, "capabilityAllVersionsSearchable");
    public static final QName CAPABILITY_JOIN = new QName(CMIS_200805_NS, "capabilityJoin");
    public static final QName CAPABILITY_FULLTEXT = new QName(CMIS_200805_NS, "capabilityFullText");

    // CMIS Schema
    public static final QName PROPERTIES = new QName(CMIS_200805_NS, "properties");
    public static final QName PROPERTY_STRING = new QName(CMIS_200805_NS, "propertyString");
    public static final QName PROPERTY_ID = new QName(CMIS_200805_NS, "propertyID");
    public static final QName PROPERTY_BOOLEAN = new QName(CMIS_200805_NS, "propertyBoolean");
    public static final QName PROPERTY_NAME = new QName(CMIS_200805_NS, "name");
    
    // CMIS Properties
    public static final String PROP_NAME = "name";
    public static final String PROP_OBJECT_ID  = "objectId";
    public static final String PROP_BASETYPE = "baseType";
    public static final String PROP_OBJECT_TYPE = "objectType";
    public static final String PROP_IS_IMMUTABLE = "isImmutable";
    public static final String PROP_IS_LATEST_VERSION = "isLatestVersion";
    public static final String PROP_IS_MAJOR_VERSION = "isMajorVersion";
    public static final String PROP_IS_LATEST_MAJOR_VERSION = "isLatestMajorVersion";
    public static final String PROP_VERSION_LABEL = "versionLabel";
    public static final String PROP_VERSION_SERIES_ID = "versionSeriesID";
    public static final String PROP_VERSION_SERIES_IS_CHECKED_OUT = "isVersionSeriesCheckedOut";
    public static final String PROP_VERSION_SERIES_CHECKED_OUT_BY = "versionSeriesCheckedOutBy";
    public static final String PROP_VERSION_SERIES_CHECKED_OUT_ID = "versionSeriesCheckedOutID";
    public static final String PROP_CHECKIN_COMMENT = "checkinComment";
 
    // CMIS Relationships
    public static final String REL_CHILDREN = "cmis-children";
    public static final String REL_PARENT = "cmis-parent";
    public static final String REL_FOLDERPARENT = "cmis-folderparent";
    public static final String REL_PARENTS = "cmis-parents";
    public static final String REL_ALLVERSIONS = "cmis-allversions";
    public static final String REL_TYPE = "cmis-type";
    
}
