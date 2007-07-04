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
package org.alfresco.model;

import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;


/**
 * Content Model Constants
 */
public interface ContentModel
{
    //
    // System Model Definitions
    //
    
    // base type constants
    static final QName TYPE_BASE = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "base");
    static final QName ASPECT_REFERENCEABLE = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "referenceable");
    static final QName PROP_STORE_PROTOCOL = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "store-protocol");
    static final QName PROP_STORE_IDENTIFIER = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "store-identifier");
    static final QName PROP_NODE_UUID = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "node-uuid");
    static final QName PROP_NODE_DBID = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "node-dbid");
    
    // tag for incomplete nodes
    static final QName ASPECT_INCOMPLETE = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "incomplete");
    
    // tag for temporary nodes
    static final QName ASPECT_TEMPORARY = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "temporary");
    
    // tag for localized nodes
    static final QName ASPECT_LOCALIZED = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "localized");
    static final QName PROP_LOCALE = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "locale");
    
    // archived nodes aspect constants
    static final QName ASPECT_ARCHIVED = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "archived");
    static final QName PROP_ARCHIVED_ORIGINAL_PARENT_ASSOC = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "archivedOriginalParentAssoc");
    static final QName PROP_ARCHIVED_BY = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "archivedBy");
    static final QName PROP_ARCHIVED_DATE = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "archivedDate");
    static final QName PROP_ARCHIVED_ORIGINAL_OWNER = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "archivedOriginalOwner");
    static final QName ASPECT_ARCHIVED_ASSOCS = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "archived-assocs");
    static final QName PROP_ARCHIVED_PARENT_ASSOCS = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "archivedParentAssocs");
    static final QName PROP_ARCHIVED_CHILD_ASSOCS = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "archivedChildAssocs");
    static final QName PROP_ARCHIVED_SOURCE_ASSOCS = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "archivedSourceAssocs");
    static final QName PROP_ARCHIVED_TARGET_ASSOCS = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "archivedTargetAssocs");
    
    // referenceable aspect constants
    static final QName TYPE_REFERENCE = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "reference");
    static final QName PROP_REFERENCE = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "reference");

    // container type constants
    static final QName TYPE_CONTAINER = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "container");
    /** child association type supported by {@link #TYPE_CONTAINER} */
    static final QName ASSOC_CHILDREN =QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "children");

    // roots
    static final QName ASPECT_ROOT = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "aspect_root");
    static final QName TYPE_STOREROOT = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "store_root");

    // descriptor properties
    static final QName PROP_SYS_VERSION_MAJOR = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "versionMajor");
    static final QName PROP_SYS_VERSION_MINOR = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "versionMinor");
    static final QName PROP_SYS_VERSION_REVISION = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "versionRevision");
    static final QName PROP_SYS_VERSION_LABEL = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "versionLabel");
    static final QName PROP_SYS_VERSION_BUILD = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "versionBuild");
    static final QName PROP_SYS_VERSION_SCHEMA = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "versionSchema");
    static final QName PROP_SYS_VERSION_EDITION = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "versionEdition"); 
    static final QName PROP_SYS_VERSION_PROPERTIES = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "versionProperties"); 
    
    //
    // Content Model Definitions
    //
    
    // content management type constants
    static final QName TYPE_CMOBJECT = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "cmobject");
    static final QName PROP_NAME = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "name");
    
    // copy aspect constants
    static final QName ASPECT_COPIEDFROM = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "copiedfrom");
    static final QName PROP_COPY_REFERENCE = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "source");
    
    // working copy aspect contants
    static final QName ASPECT_WORKING_COPY = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "workingcopy");
    static final QName PROP_WORKING_COPY_OWNER = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "workingCopyOwner");
    
    // content type and aspect constants
    static final QName TYPE_CONTENT = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "content");
    static final QName PROP_CONTENT = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "content");
    
    // title aspect
    static final QName ASPECT_TITLED = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "titled");
    static final QName PROP_TITLE = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "title");
    static final QName PROP_DESCRIPTION = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "description");
    
    // auditable aspect
    static final QName ASPECT_AUDITABLE = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "auditable");
    static final QName PROP_CREATED = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "created");
    static final QName PROP_CREATOR = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "creator");
    static final QName PROP_MODIFIED = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "modified");
    static final QName PROP_MODIFIER = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "modifier");
    static final QName PROP_ACCESSED = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "accessed");
    
    // author aspect
    static final QName ASPECT_AUTHOR = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "author");
    static final QName PROP_AUTHOR = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "author");
    
    // categories
    static final QName TYPE_CATEGORYROOT = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "category_root");  
    static final QName ASPECT_CLASSIFIABLE = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "classifiable");
    //static final QName ASPECT_CATEGORISATION = QName.createQName(NamespaceService.ALFRESCO_URI, "aspect_categorisation");
    static final QName ASPECT_GEN_CLASSIFIABLE = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "generalclassifiable");
    static final QName TYPE_CATEGORY = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "category");
    static final QName PROP_CATEGORIES = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "categories");
    static final QName ASSOC_CATEGORIES = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "categories");
    static final QName ASSOC_SUBCATEGORIES = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "subcategories");

    // lock aspect
    public final static QName ASPECT_LOCKABLE = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "lockable");
    public final static QName PROP_LOCK_OWNER = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "lockOwner");
    public final static QName PROP_LOCK_TYPE = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "lockType");
    public final static QName PROP_EXPIRY_DATE = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "expiryDate");
    
    // version aspect
    static final QName ASPECT_VERSIONABLE = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "versionable");
    static final QName PROP_VERSION_LABEL = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "versionLabel");
    static final QName PROP_INITIAL_VERSION = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "initialVersion");
    static final QName PROP_AUTO_VERSION = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "autoVersion");

    // folders
    static final QName TYPE_SYSTEM_FOLDER = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "systemfolder");
    static final QName TYPE_FOLDER = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "folder");
    /** child association type supported by {@link #TYPE_FOLDER} */
    static final QName ASSOC_CONTAINS = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "contains");
    
    // person
    static final QName TYPE_PERSON = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "person");
    static final QName PROP_USERNAME = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "userName");
    static final QName PROP_HOMEFOLDER = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "homeFolder");
    static final QName PROP_FIRSTNAME = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "firstName");
    static final QName PROP_LASTNAME = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "lastName");
    static final QName PROP_EMAIL = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "email");
    static final QName PROP_ORGID = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "organizationId");
    static final QName PROP_HOME_FOLDER_PROVIDER = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "homeFolderProvider");
    static final QName PROP_DEFAULT_HOME_FOLDER_PATH = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "defaultHomeFolderPath");
    
    // Ownable aspect  
    static final QName ASPECT_OWNABLE = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "ownable");
    static final QName PROP_OWNER = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "owner");
    
    // Templatable aspect
    static final QName ASPECT_TEMPLATABLE = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "templatable");
    static final QName PROP_TEMPLATE = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "template");
    
    // Webscriptable aspect
    static final QName ASPECT_WEBSCRIPTABLE = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "webscriptable");
    static final QName PROP_WEBSCRIPT = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "webscript");
    
    // Dictionary model
    static final QName TYPE_DICTIONARY_MODEL = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "dictionaryModel");
    static final QName PROP_MODEL_NAME = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "modelName");
    static final QName PROP_MODEL_DESCRIPTION = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "modelDescription");
    static final QName PROP_MODEL_AUTHOR = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "modelAuthor");
    static final QName PROP_MODEL_PUBLISHED_DATE = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "modelPublishedDate");
    static final QName PROP_MODEL_VERSION = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "modelVersion");
    static final QName PROP_MODEL_ACTIVE = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "modelActive");
    
    // referencing aspect
    static final QName ASPECT_REFERENCING = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "referencing");
    static final QName ASSOC_REFERENCES = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "references");
    
    // link object
    static final QName TYPE_LINK = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "link");
    static final QName PROP_LINK_DESTINATION = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "destination");
    
    // email aspect
    static final QName ASPECT_MAILED = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "emailed");
    static final QName PROP_SENTDATE = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "sentdate");
    static final QName PROP_ORIGINATOR = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "originator");
    static final QName PROP_ADDRESSEE = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "addressee");
    static final QName PROP_ADDRESSEES = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "addressees");
    static final QName PROP_SUBJECT = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "subjectline");
    
    // countable aspect
    static final QName ASPECT_COUNTABLE = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "countable");
    static final QName PROP_HITS = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "hits");
    static final QName PROP_COUNTER = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "counter");
    
    // References Node Aspect.
    static final QName ASPECT_REFERENCES_NODE = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "referencesnode");
    static final QName PROP_NODE_REF = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "noderef");
    
    // Multilingual Type
    static final QName TYPE_MULTILINGUAL_CONTAINER = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "mlContainer");
    static final QName ASSOC_MULTILINGUAL_CHILD = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "mlChild");
    static final QName ASPECT_MULTILINGUAL_DOCUMENT = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "mlDocument");
    static final QName ASPECT_MULTILINGUAL_EMPTY_TRANSLATION = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "mlEmptyTranslation");

    //
    // User Model Definitions
    //
    
    static final String USER_MODEL_URI = "http://www.alfresco.org/model/user/1.0";
    static final String USER_MODEL_PREFIX = "usr";
    
    static final QName TYPE_USER = QName.createQName(USER_MODEL_URI, "user");
    static final QName PROP_USER_USERNAME = QName.createQName(USER_MODEL_URI, "username");
    static final QName PROP_PASSWORD = QName.createQName(USER_MODEL_URI, "password");
    static final QName PROP_ENABLED = QName.createQName(USER_MODEL_URI, "enabled");
    static final QName PROP_ACCOUNT_EXPIRES = QName.createQName(USER_MODEL_URI, "accountExpires");
    static final QName PROP_ACCOUNT_EXPIRY_DATE = QName.createQName(USER_MODEL_URI, "accountExpiryDate");
    static final QName PROP_CREDENTIALS_EXPIRE = QName.createQName(USER_MODEL_URI, "credentialsExpire");
    static final QName PROP_CREDENTIALS_EXPIRY_DATE = QName.createQName(USER_MODEL_URI, "credentialsExpiryDate");
    static final QName PROP_ACCOUNT_LOCKED = QName.createQName(USER_MODEL_URI, "accountLocked");
    static final QName PROP_SALT = QName.createQName(USER_MODEL_URI, "salt");

    static final QName TYPE_AUTHORITY = QName.createQName(USER_MODEL_URI, "authority");
    
    static final QName TYPE_AUTHORITY_CONTAINER = QName.createQName(USER_MODEL_URI, "authorityContainer");
    static final QName PROP_AUTHORITY_NAME = QName.createQName(USER_MODEL_URI, "authorityName");
    static final QName ASSOC_MEMBER = QName.createQName(USER_MODEL_URI, "member");
    static final QName PROP_MEMBERS = QName.createQName(USER_MODEL_URI, "members");
}
