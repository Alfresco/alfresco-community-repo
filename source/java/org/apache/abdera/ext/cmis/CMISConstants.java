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

import org.apache.abdera.util.Constants;


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
    // Namespace
    public static final String CMIS_200805_NS = "http://www.cmis.org/2008/05";
    
    // Mimetypes
    public static final String MIMETYPE_QUERY = "application/cmisquery+xml";
    public static final String MIMETYPE_ALLOWABLEACTIONS = "application/cmisallowableactions+xml";

    // CMIS Service Document
    public static final QName COLLECTION_TYPE = new QName(CMIS_200805_NS, "collectionType");
    public static final String COLLECTION_ROOT_CHILDREN = "root-children";
    public static final String COLLECTION_ROOT_DESCENDANTS = "root-descendants";
    public static final String COLLECTION_CHECKEDOUT = "checkedout";
    public static final String COLLECTION_UNFILED = "unfiled";
    public static final String COLLECTION_TYPES_CHILDREN = "types-children";
    public static final String COLLECTION_TYPES_DESCENDANTS = "types-descendants";
    public static final String COLLECTION_QUERY = "query";
    
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

    // CMIS Object
    public static final QName OBJECT = new QName(CMIS_200805_NS, "object");
    public static final QName PROPERTIES = new QName(CMIS_200805_NS, "properties");
    public static final QName PROPERTY_NAME = new QName(CMIS_200805_NS, "name");
    public static final QName PROPERTY_VALUE = new QName(CMIS_200805_NS, "value");
    public static final QName PROPERTY_STRING = new QName(CMIS_200805_NS, "propertyString");
    public static final QName PROPERTY_DECIMAL = new QName(CMIS_200805_NS, "propertyDecimal");
    public static final QName PROPERTY_INTEGER = new QName(CMIS_200805_NS, "propertyInteger");
    public static final QName PROPERTY_BOOLEAN = new QName(CMIS_200805_NS, "propertyBoolean");
    public static final QName PROPERTY_DATETIME = new QName(CMIS_200805_NS, "propertyDateTime");
    public static final QName PROPERTY_URI = new QName(CMIS_200805_NS, "propertyUri");
    public static final QName PROPERTY_ID = new QName(CMIS_200805_NS, "propertyId");
    public static final QName PROPERTY_XML = new QName(CMIS_200805_NS, "propertyXml");
    public static final QName PROPERTY_HTML = new QName(CMIS_200805_NS, "propertyHtml");
        
    // CMIS Relationships
    public static final String REL_CHILDREN = "cmis-children";
    public static final String REL_DESCENDANTS = "cmis-descendants";
    public static final String REL_PARENT = "cmis-parent";
    public static final String REL_FOLDERPARENT = "cmis-folderparent";
    public static final String REL_PARENTS = "cmis-parents";
    public static final String REL_ALLVERSIONS = "cmis-allversions";
    public static final String REL_TYPE = "cmis-type";
    public static final String REL_SOURCE = "cmis-source";
    
    // CMIS Nested Entry
    public static final QName NESTED_ENTRY = Constants.ENTRY;
    
    
    // CMIS Properties Names
    public static final String PROP_NAME = "Name";
    public static final String PROP_OBJECT_ID  = "ObjectId";
    public static final String PROP_BASETYPE = "BaseType";
    public static final String PROP_OBJECT_TYPE_ID = "ObjectTypeId";
    public static final String PROP_IS_IMMUTABLE = "IsImmutable";
    public static final String PROP_IS_LATEST_VERSION = "IsLatestVersion";
    public static final String PROP_IS_MAJOR_VERSION = "IsMajorVersion";
    public static final String PROP_IS_LATEST_MAJOR_VERSION = "IsLatestMajorVersion";
    public static final String PROP_VERSION_LABEL = "VersionLabel";
    public static final String PROP_VERSION_SERIES_ID = "VersionSeriesId";
    public static final String PROP_IS_VERSION_SERIES_CHECKED_OUT = "IsVersionSeriesCheckedOut";
    public static final String PROP_VERSION_SERIES_CHECKED_OUT_BY = "VersionSeriesCheckedOutBy";
    public static final String PROP_VERSION_SERIES_CHECKED_OUT_ID = "VersionSeriesCheckedOutId";
    public static final String PROP_CHECKIN_COMMENT = "CheckinComment";

    // CMIS Property Types
    public static final String PROP_TYPE_STRING = "string";
    public static final String PROP_TYPE_DECIMAL = "decimal";
    public static final String PROP_TYPE_INTEGER = "integer";
    public static final String PROP_TYPE_BOOLEAN = "boolean";
    public static final String PROP_TYPE_DATETIME = "datetime";
    public static final String PROP_TYPE_URI = "uri";
    public static final String PROP_TYPE_ID = "id";
    public static final String PROP_TYPE_XML = "xml";
    public static final String PROP_TYPE_HTML = "html";
    
}
