/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.repo.download;

import org.alfresco.service.namespace.QName;

/**
 * Utility interface for the downloadModel.xml
 * 
 * @author Alex Miller
 */
public interface DownloadModel
{
    /** Download Model URI */
    static final String DOWNLOAD_MODEL_1_0_URI = "http://www.alfresco.org/model/download/1.0";
    
    /** Type QName */
    static final QName TYPE_DOWNLOAD            = QName.createQName(DOWNLOAD_MODEL_1_0_URI, "download");

    // Property QNames
    static final QName PROP_CANCELLED           = QName.createQName(DOWNLOAD_MODEL_1_0_URI, "cancelled");
    static final QName PROP_DONE                = QName.createQName(DOWNLOAD_MODEL_1_0_URI, "done");
    static final QName PROP_FILES_ADDED         = QName.createQName(DOWNLOAD_MODEL_1_0_URI, "filesAdded");
    static final QName PROP_RECURSIVE           = QName.createQName(DOWNLOAD_MODEL_1_0_URI, "recursive");
    static final QName PROP_SEQUENCE_NUMBER     = QName.createQName(DOWNLOAD_MODEL_1_0_URI, "sequenceNumber");
    static final QName PROP_STATUS              = QName.createQName(DOWNLOAD_MODEL_1_0_URI, "status");
    static final QName PROP_TOTAL               = QName.createQName(DOWNLOAD_MODEL_1_0_URI, "total");
    static final QName PROP_TOTAL_FILES         = QName.createQName(DOWNLOAD_MODEL_1_0_URI, "totalFiles");
    
    // Associations
    static final QName ASSOC_REQUESTED_NODES    = QName.createQName(DOWNLOAD_MODEL_1_0_URI, "requestedNodes");
}
