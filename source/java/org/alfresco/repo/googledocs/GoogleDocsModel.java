/*
* Copyright (C) 2005-2010 Alfresco Software Limited.
*
* This file is part of Alfresco
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
*/
package org.alfresco.repo.googledocs;

import org.alfresco.service.namespace.QName;

/**
 * Google docs model constants
 */
public interface GoogleDocsModel
{
    static final String GOOGLE_DOCS_PREFIX = "gd";
    static final String GOOGLE_DOCS_MODEL_1_0_URI = "http://www.alfresco.org/model/googledocs/1.0";

    static final QName ASPECT_GOOGLEEDITABLE = QName.createQName(GOOGLE_DOCS_MODEL_1_0_URI, "googleEditable");
    
    static final QName ASPECT_GOOGLERESOURCE = QName.createQName(GOOGLE_DOCS_MODEL_1_0_URI, "googleResource");
    static final QName PROP_URL = QName.createQName(GOOGLE_DOCS_MODEL_1_0_URI, "url");
    static final QName PROP_RESOURCE_ID = QName.createQName(GOOGLE_DOCS_MODEL_1_0_URI, "resourceId");
    static final QName PROP_RESOURCE_TYPE = QName.createQName(GOOGLE_DOCS_MODEL_1_0_URI, "resourceType");    
}
