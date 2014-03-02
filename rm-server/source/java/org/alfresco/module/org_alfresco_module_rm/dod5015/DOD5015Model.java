/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.dod5015;

import org.alfresco.service.namespace.QName;


/**
 * Helper class containing DOD 5015 model qualified names
 *
 * @author Roy Wetherall
 */
public interface DOD5015Model
{
    // Namespace details
    static final String DOD_URI = "http://www.alfresco.org/model/dod5015/1.0";
    static final String DOD_PREFIX = "dod";

    // DOD Site
    static final QName TYPE_DOD_5015_SITE = QName.createQName(DOD_URI, "site");

    // DOD File Plan
    static final QName TYPE_DOD_5015_FILE_PLAN = QName.createQName(DOD_URI, "filePlan");

    // DOD Record
    static final QName ASPECT_DOD_5015_RECORD = QName.createQName(DOD_URI, "dod5015record");
    static final QName PROP_ORIGINATOR = QName.createQName(DOD_URI, "originator");
    static final QName PROP_ORIGINATING_ORGANIZATION = QName.createQName(DOD_URI, "originatingOrganization");
    static final QName PROP_PUBLICATION_DATE = QName.createQName(DOD_URI, "publicationDate");
    static final QName PROP_MEDIA_TYPE = QName.createQName(DOD_URI, "mediaType");
    static final QName PROP_FORMAT = QName.createQName(DOD_URI, "format");
    static final QName PROP_DATE_RECEIVED = QName.createQName(DOD_URI, "dateReceived");

    // Scanned Record
    static final QName ASPECT_SCANNED_RECORD = QName.createQName(DOD_URI, "scannedRecord");
    static final QName PROP_SCANNED_FORMAT = QName.createQName(DOD_URI, "scannedFormat");
    static final QName PROP_SCANNED_FORMAT_VERSION = QName.createQName(DOD_URI, "scannedFormatVersion");
    static final QName PROP_RESOLUTION_X = QName.createQName(DOD_URI, "resolutionX");
    static final QName PROP_RESOLUTION_Y = QName.createQName(DOD_URI, "resolutionY");
    static final QName PROP_SCANNED_BIT_DEPTH = QName.createQName(DOD_URI, "scannedBitDepth");

    // PDF Record
    static final QName ASPECT_PDF_RECORD = QName.createQName(DOD_URI, "pdfRecord");
    static final QName PROP_PRODUCING_APPLICATION = QName.createQName(DOD_URI, "producingApplication");
    static final QName PROP_PRODUCING_APPLICATION_VERSION = QName.createQName(DOD_URI, "producingApplicationVersion");
    static final QName PROP_PDF_VERSION = QName.createQName(DOD_URI, "pdfVersion");
    static final QName PROP_CREATING_APPLICATION = QName.createQName(DOD_URI, "creatingApplication");
    static final QName PROP_DOCUMENT_SECURITY_SETTINGS = QName.createQName(DOD_URI, "documentSecuritySettings");

    // Digital Photograph Record
    static final QName ASPECT_DIGITAL_PHOTOGRAPH_RECORD = QName.createQName(DOD_URI, "digitalPhotographRecord");
    static final QName PROP_CAPTION = QName.createQName(DOD_URI, "caption");
    static final QName PROP_PHOTOGRAPHER = QName.createQName(DOD_URI, "photographer");
    static final QName PROP_COPYRIGHT = QName.createQName(DOD_URI, "copyright");
    static final QName PROP_BIT_DEPTH = QName.createQName(DOD_URI, "bitDepth");
    static final QName PROP_IMAGE_SIZE_X = QName.createQName(DOD_URI, "imageSizeX");
    static final QName PROP_IMAGE_SIZE_Y = QName.createQName(DOD_URI, "imageSizeY");
    static final QName PROP_IMAGE_SOURCE = QName.createQName(DOD_URI, "imageSource");
    static final QName PROP_COMPRESSION = QName.createQName(DOD_URI, "compression");
    static final QName PROP_ICC_ICM_PROFILE = QName.createQName(DOD_URI, "iccIcmProfile");
    static final QName PROP_EXIF_INFORMATION = QName.createQName(DOD_URI, "exifInformation");

    // Web Record
    static final QName ASPECT_WEB_RECORD = QName.createQName(DOD_URI, "webRecord");
    static final QName PROP_WEB_FILE_NAME = QName.createQName(DOD_URI, "webFileName");
    static final QName PROP_WEB_PLATFORM = QName.createQName(DOD_URI, "webPlatform");
    static final QName PROP_WEBSITE_NAME = QName.createQName(DOD_URI, "webSiteName");
    static final QName PROP_WEB_SITE_URL = QName.createQName(DOD_URI, "webSiteURL");
    static final QName PROP_CAPTURE_METHOD = QName.createQName(DOD_URI, "captureMethod");
    static final QName PROP_CAPTURE_DATE = QName.createQName(DOD_URI, "captureDate");
    static final QName PROP_CONTACT = QName.createQName(DOD_URI, "contact");
    static final QName PROP_CONTENT_MANAGEMENT_SYSTEM = QName.createQName(DOD_URI, "contentManagementSystem");
}
