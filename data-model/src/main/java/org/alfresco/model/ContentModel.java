/*
 * #%L
 * Alfresco Data model classes
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.model;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;


/**
 * Content Model Constants
 */
@AlfrescoPublicApi
public interface ContentModel
{
    //
    // System Model Definitions
    //
    
    // type for deleted nodes
    static final QName TYPE_DELETED = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "deleted");
    static final QName PROP_ORIGINAL_ID = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "originalId");
    
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
    
    // tag for nodes being formed (CIFS)
    static final QName ASPECT_NO_CONTENT = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "noContent");

    // tag for nodes being formed (WebDAV)
    static final QName ASPECT_WEBDAV_NO_CONTENT = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "webdavNoContent");
    
    static final QName ASPECT_WEBDAV_OBJECT = QName.createQName(NamespaceService.WEBDAV_MODEL_1_0_URI, "object");
    static final QName PROP_DEAD_PROPERTIES = QName.createQName(NamespaceService.WEBDAV_MODEL_1_0_URI, "deadproperties");
    
    // tag for localized nodes
    static final QName ASPECT_LOCALIZED = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "localized");
    static final QName PROP_LOCALE = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "locale");
    
    // tag for hidden nodes
    static final QName ASPECT_HIDDEN = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "hidden");
    static final QName PROP_VISIBILITY_MASK = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "clientVisibilityMask");
    static final QName PROP_HIDDEN_FLAG = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "hiddenFlag");
    static final QName PROP_CASCADE_HIDDEN = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "cascadeHidden");
    static final QName PROP_CASCADE_INDEX_CONTROL = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "cascadeIndexControl");
    static final QName PROP_CLIENT_CONTROLLED = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "clientControlled");

    // tag for soft delete (CIFS rename shuffle)
    static final QName ASPECT_SOFT_DELETE  = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "softDelete");
    
    // archived nodes aspect constants
    static final QName ASPECT_ARCHIVE_ROOT = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "archiveRoot");
    static final QName ASSOC_ARCHIVE_USER_LINK = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "archiveUserLink");
    static final QName TYPE_ARCHIVE_USER = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "archiveUser");
    static final QName ASSOC_ARCHIVED_LINK = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "archivedLink");
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
    static final QName ASPECT_ARCHIVE_LOCKABLE = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "archivedLockable");
    static final QName PROP_ARCHIVED_LOCK_OWNER = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "archivedLockOwner");
    static final QName PROP_ARCHIVED_LOCK_TYPE = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "archivedLockType");
    static final QName PROP_ARCHIVED_LOCK_LIFETIME = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "archivedLockLifetime");
    static final QName PROP_ARCHIVED_EXPIRY_DATE = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "archivedExpiryDate");
    static final QName PROP_ARCHIVED_LOCK_ADDITIONAL_INFO = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "archivedLockAdditionalInfo");

    
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

    // for internal use only: see ALF-13066 / ALF-12358
    static final QName TYPE_LOST_AND_FOUND = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "lost_found");
    static final QName ASSOC_LOST_AND_FOUND = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "lost_found");
    
    // descriptor properties
    static final QName PROP_SYS_NAME = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "name");
    static final QName PROP_SYS_VERSION_MAJOR = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "versionMajor");
    static final QName PROP_SYS_VERSION_MINOR = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "versionMinor");
    static final QName PROP_SYS_VERSION_REVISION = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "versionRevision");
    static final QName PROP_SYS_VERSION_LABEL = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "versionLabel");
    static final QName PROP_SYS_VERSION_BUILD = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "versionBuild");
    static final QName PROP_SYS_VERSION_SCHEMA = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "versionSchema");
    static final QName PROP_SYS_VERSION_EDITION = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "versionEdition"); 
    static final QName PROP_SYS_VERSION_PROPERTIES = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "versionProperties"); 
    static final QName PROP_SYS_LICENSE_MODE = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "licenseMode");
    
    /**
     * Aspect for nodes which are by default not deletable.
     * @since 3.5.0
     */
    static final QName ASPECT_UNDELETABLE = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "undeletable");
    
    /**
     * Aspect for nodes which are by default not movable.
     */
    static final QName ASPECT_UNMOVABLE = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "unmovable");

    /**
     * Aspects marking nodes that are pending deletion.
     * This aspect is applied to all nodes that are about to be deleted within a transaction.
     * The aspect survives only for the duration of calls to delete nodes and their children.
     */
    static final QName ASPECT_PENDING_DELETE = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "pendingDelete");
    
    /**
     * Aspect marking nodes for pending set fixed ACL operation and it's contants
     */
    static final QName ASPECT_PENDING_FIX_ACL = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "pendingFixAcl");
    static final QName PROP_SHARED_ACL_TO_REPLACE = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "sharedAclToReplace");
    static final QName PROP_INHERIT_FROM_ACL = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "inheritFromAcl");
    
    //
    // Content Model Definitions
    //
    
    // content management type constants
    static final QName TYPE_CMOBJECT = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "cmobject");
    static final QName PROP_NAME = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "name");
    
    // copy aspect constants
    static final QName ASPECT_COPIEDFROM = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "copiedfrom");
    static final QName ASSOC_ORIGINAL = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "original");
    
    // working copy aspect contants
    static final QName ASPECT_CHECKED_OUT = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "checkedOut");
    static final QName ASSOC_WORKING_COPY_LINK = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "workingcopylink");
    static final QName ASPECT_WORKING_COPY = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "workingcopy");
    static final QName PROP_WORKING_COPY_OWNER = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "workingCopyOwner");
    static final QName PROP_WORKING_COPY_MODE = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "workingCopyMode");
    static final QName PROP_WORKING_COPY_LABEL = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "workingCopyLabel");
    
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

    // tags - a subsection of categories
    static final QName ASPECT_TAGGABLE = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "taggable");
    static final QName PROP_TAGS = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "taggable");
    
    // tagscope aspect
    static final QName ASPECT_TAGSCOPE = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "tagscope");
    static final QName PROP_TAGSCOPE_CACHE = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "tagScopeCache");
    static final QName PROP_TAGSCOPE_SUMMARY = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "tagScopeSummary");
    
    // ratings
    static final QName ASPECT_RATEABLE = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "rateable");
    static final QName ASPECT_LIKES_RATING_SCHEME_ROLLUPS = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "likesRatingSchemeRollups");
    static final QName ASPECT_FIVESTAR_RATING_SCHEME_ROLLUPS = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "fiveStarRatingSchemeRollups");
    static final QName ASSOC_RATINGS = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "ratings");
    static final QName TYPE_RATING = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "rating");
    static final QName PROP_RATING_SCORE = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "ratingScore");
    static final QName PROP_RATING_SCHEME = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "ratingScheme");
    static final QName PROP_RATED_AT = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "ratedAt");
    
    // lock aspect
    public final static QName ASPECT_LOCKABLE = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "lockable");
    public final static QName PROP_LOCK_OWNER = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "lockOwner");
    public final static QName PROP_LOCK_TYPE = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "lockType");
    public final static QName PROP_LOCK_LIFETIME = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "lockLifetime");
    public final static QName PROP_EXPIRY_DATE = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "expiryDate");
    public final static QName PROP_LOCK_ADDITIONAL_INFO = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "lockAdditionalInfo");
    
    // version aspect
    static final QName ASPECT_VERSIONABLE = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "versionable");
    static final QName PROP_VERSION_LABEL = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "versionLabel");
    static final QName PROP_INITIAL_VERSION = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "initialVersion");
    static final QName PROP_AUTO_VERSION = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "autoVersion");
    static final QName PROP_AUTO_VERSION_PROPS = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "autoVersionOnUpdateProps");
    static final QName PROP_VERSION_TYPE = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "versionType"); 
    
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
    static final QName PROP_PRESENCEPROVIDER = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "presenceProvider");
    static final QName PROP_PRESENCEUSERNAME = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "presenceUsername");
    static final QName PROP_ORGANIZATION = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "organization");
    static final QName PROP_JOBTITLE = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "jobtitle");
    static final QName PROP_LOCATION = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "location");
    static final QName PROP_PERSONDESC = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "persondescription");
    static final QName PROP_TELEPHONE = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "telephone");
    static final QName PROP_MOBILE = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "mobile");
    static final QName PROP_COMPANYADDRESS1 = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "companyaddress1");
    static final QName PROP_COMPANYADDRESS2 = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "companyaddress2");
    static final QName PROP_COMPANYADDRESS3 = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "companyaddress3");
    static final QName PROP_COMPANYPOSTCODE = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "companypostcode");
    static final QName PROP_COMPANYTELEPHONE = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "companytelephone");
    static final QName PROP_COMPANYFAX = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "companyfax");
    static final QName PROP_COMPANYEMAIL = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "companyemail");
    static final QName PROP_SKYPE = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "skype");
    static final QName PROP_GOOGLEUSERNAME = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "googleusername");
    static final QName PROP_INSTANTMSG = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "instantmsg");
    static final QName PROP_USER_STATUS = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "userStatus");
    static final QName PROP_USER_STATUS_TIME = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "userStatusTime");
    
    static final QName PROP_SIZE_CURRENT = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "sizeCurrent"); // system-maintained
    static final QName PROP_SIZE_QUOTA = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "sizeQuota");
    
    static final QName PROP_EMAIL_FEED_ID = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "emailFeedId"); // system-maintained
    static final QName PROP_EMAIL_FEED_DISABLED = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "emailFeedDisabled");
    
    static final QName PROP_SUBSCRIPTIONS_PRIVATE = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "subscriptionsPrivate");
    
    static final QName ASPECT_PERSON_DISABLED = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "personDisabled");
    
    static final QName ASPECT_ANULLABLE = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "annullable");
        
    static final QName ASSOC_AVATAR = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "avatar");
    
    // Authority
    static final QName TYPE_AUTHORITY = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "authority");
    
    static final QName TYPE_AUTHORITY_CONTAINER = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "authorityContainer");
    static final QName PROP_AUTHORITY_NAME = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "authorityName");
    static final QName PROP_AUTHORITY_DISPLAY_NAME = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "authorityDisplayName");
    
    static final QName ASSOC_MEMBER = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "member");

    // Zone
    static final QName TYPE_ZONE = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "zone");
    static final QName ASSOC_IN_ZONE = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "inZone");

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
    
    // attachable aspect
    static final QName ASPECT_ATTACHABLE = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "attachable");
    static final QName ASSOC_ATTACHMENTS = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "attachments");

    // emailed aspect
    static final QName ASPECT_EMAILED = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "emailed");
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
    
    // Geographic Aspect.
    static final QName ASPECT_GEOGRAPHIC = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "geographic");
    static final QName PROP_LATITUDE = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "latitude");
    static final QName PROP_LONGITUDE = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "longitude");
    
    // Multilingual Type
    static final QName TYPE_MULTILINGUAL_CONTAINER = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "mlContainer");
    static final QName ASSOC_MULTILINGUAL_CHILD = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "mlChild");
    static final QName ASPECT_MULTILINGUAL_DOCUMENT = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "mlDocument");
    static final QName ASPECT_MULTILINGUAL_EMPTY_TRANSLATION = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "mlEmptyTranslation");

    // Thumbnail Type
    static final QName TYPE_THUMBNAIL = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "thumbnail");
    static final QName PROP_THUMBNAIL_NAME = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "thumbnailName");
    static final QName PROP_CONTENT_PROPERTY_NAME = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "contentPropertyName");
    static final QName PROP_AUTOMATIC_UPDATE = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "automaticUpdate");
    
    // Thumbnail modification handling
    public static final QName ASPECT_THUMBNAIL_MODIFICATION = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "thumbnailModification");
    public static final QName PROP_LAST_THUMBNAIL_MODIFICATION_DATA = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "lastThumbnailModification"); 
    
    // The below content entities can be used to manage 'failed' thumbnails. These are thumbnails that execute and fail with an
    // exception that likely means a reattempt will fail. The failedThumbnailSource aspect can be used to mark a node as
    // having tried and failed to use a particular thumbnail definition. This can then be checked and reattempts at that thumbnail
    // can be prevented or throttled.
    static final QName ASPECT_FAILED_THUMBNAIL_SOURCE = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "failedThumbnailSource");
    static final QName ASSOC_FAILED_THUMBNAIL= QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "failedThumbnail");
    static final QName TYPE_FAILED_THUMBNAIL = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "failedThumbnail");
    static final QName PROP_FAILED_THUMBNAIL_TIME = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "failedThumbnailTime");
    static final QName PROP_FAILURE_COUNT = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "failureCount");

    // Thumbnailed Aspect
    /**
     * This aspect type has been deprecated.
     * From Alfresco 3.3 the {@link RenditionModel#ASPECT_RENDITIONED rn:renditioned}
     * (which is a child of cm:thumbnailed) should be used instead.
     */
    @Deprecated
    static final QName ASPECT_THUMBNAILED = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "thumbnailed");
    /**
     * This association type has been deprecated.
     * From Alfresco 3.3 this association no longer exists and has been replaced with
     * {@link RenditionModel#ASSOC_RENDITION rn:rendition} association. From Alfresco
     * 3.3 onwards a patch is executed at startup which renames
     * the cm:thumbnails QName to rn:rendition in the database.
     * <P/>
     * This field has been updated to point to that association and references to this
     * field should be updated to use the new field.
     */
    @Deprecated
    static final QName ASSOC_THUMBNAILS = RenditionModel.ASSOC_RENDITION;
    
    // StoreSelector Aspect
    static final QName ASPECT_STORE_SELECTOR = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "storeSelector");
    static final QName PROP_STORE_NAME = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "storeName");
    
    // Preference Aspect
    static final QName ASPECT_PREFERENCES = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "preferences");
    static final QName PROP_PREFERENCE_VALUES = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "preferenceValues");
    static final QName ASSOC_PREFERENCE_IMAGE = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "preferenceImage");
    
    // Syndication Aspect
    static final QName ASPECT_SYNDICATION = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "syndication");
    static final QName PROP_PUBLISHED = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "published");
    static final QName PROP_UPDATED = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "updated");
    
    // Dublin core aspect
    static final QName ASPECT_DUBLINCORE = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "dublincore");
    
    //
    // User Model Definitions
    //
    
    static final String USER_MODEL_URI = "http://www.alfresco.org/model/user/1.0";
    static final String USER_MODEL_PREFIX = "usr";
    
    static final QName TYPE_USER = QName.createQName(USER_MODEL_URI, "user");
    static final QName PROP_USER_USERNAME = QName.createQName(USER_MODEL_URI, "username");
    static final QName PROP_PASSWORD = QName.createQName(USER_MODEL_URI, "password");
    static final QName PROP_PASSWORD_SHA256 = QName.createQName(USER_MODEL_URI, "password2");
    static final QName PROP_PASSWORD_HASH = QName.createQName(USER_MODEL_URI, "passwordHash");
    static final QName PROP_ENABLED = QName.createQName(USER_MODEL_URI, "enabled");
    static final QName PROP_ACCOUNT_EXPIRES = QName.createQName(USER_MODEL_URI, "accountExpires");
    static final QName PROP_ACCOUNT_EXPIRY_DATE = QName.createQName(USER_MODEL_URI, "accountExpiryDate");
    static final QName PROP_CREDENTIALS_EXPIRE = QName.createQName(USER_MODEL_URI, "credentialsExpire");
    static final QName PROP_CREDENTIALS_EXPIRY_DATE = QName.createQName(USER_MODEL_URI, "credentialsExpiryDate");
    static final QName PROP_ACCOUNT_LOCKED = QName.createQName(USER_MODEL_URI, "accountLocked");
    static final QName PROP_SALT = QName.createQName(USER_MODEL_URI, "salt");
    static final QName PROP_HASH_INDICATOR = QName.createQName(USER_MODEL_URI, "hashIndicator");
    
    // 
    // Indexing control
    //
    
    static final QName ASPECT_INDEX_CONTROL = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "indexControl");
    static final QName PROP_IS_INDEXED = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "isIndexed");
    static final QName PROP_IS_CONTENT_INDEXED = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "isContentIndexed");
    
    // CMIS aspects
    static final QName ASPECT_CMIS_UPDATE_CONTEXT = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "CMISUpdateContext");
    static final QName PROP_GOT_FIRST_CHUNK = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "gotFirstChunk");
    static final QName ASPECT_CMIS_CREATED_CHECKEDOUT = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "cmisCreatedCheckedOut");
    
    // Cascade Update
    static final QName ASPECT_CASCADE_UPDATE = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "cascadeUpdate");
    static final QName PROP_CASCADE_CRC = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "cascadeCRC");
    static final QName PROP_CASCADE_TX = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "cascadeTx");
    
}
