/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
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
    String DOD_URI = "http://www.alfresco.org/model/dod5015/1.0";
    String DOD_PREFIX = "dod";

    // DOD Site
    QName TYPE_DOD_5015_SITE = QName.createQName(DOD_URI, "site");

    // DOD File Plan
    QName TYPE_DOD_5015_FILE_PLAN = QName.createQName(DOD_URI, "filePlan");

    // DOD Record
    QName ASPECT_DOD_5015_RECORD = QName.createQName(DOD_URI, "dod5015record");
    QName PROP_ORIGINATOR = QName.createQName(DOD_URI, "originator");
    QName PROP_ORIGINATING_ORGANIZATION = QName.createQName(DOD_URI, "originatingOrganization");
    QName PROP_PUBLICATION_DATE = QName.createQName(DOD_URI, "publicationDate");
    QName PROP_MEDIA_TYPE = QName.createQName(DOD_URI, "mediaType");
    QName PROP_FORMAT = QName.createQName(DOD_URI, "format");
    QName PROP_DATE_RECEIVED = QName.createQName(DOD_URI, "dateReceived");
    QName PROP_ADDRESS = QName.createQName(DOD_URI, "address");
    QName PROP_OTHER_ADDRESS = QName.createQName(DOD_URI, "otherAddress");

    // Scanned Record
    QName ASPECT_SCANNED_RECORD = QName.createQName(DOD_URI, "scannedRecord");
    QName PROP_SCANNED_FORMAT = QName.createQName(DOD_URI, "scannedFormat");
    QName PROP_SCANNED_FORMAT_VERSION = QName.createQName(DOD_URI, "scannedFormatVersion");
    QName PROP_RESOLUTION_X = QName.createQName(DOD_URI, "resolutionX");
    QName PROP_RESOLUTION_Y = QName.createQName(DOD_URI, "resolutionY");
    QName PROP_SCANNED_BIT_DEPTH = QName.createQName(DOD_URI, "scannedBitDepth");

    // PDF Record
    QName ASPECT_PDF_RECORD = QName.createQName(DOD_URI, "pdfRecord");
    QName PROP_PRODUCING_APPLICATION = QName.createQName(DOD_URI, "producingApplication");
    QName PROP_PRODUCING_APPLICATION_VERSION = QName.createQName(DOD_URI, "producingApplicationVersion");
    QName PROP_PDF_VERSION = QName.createQName(DOD_URI, "pdfVersion");
    QName PROP_CREATING_APPLICATION = QName.createQName(DOD_URI, "creatingApplication");
    QName PROP_DOCUMENT_SECURITY_SETTINGS = QName.createQName(DOD_URI, "documentSecuritySettings");

    // Digital Photograph Record
    QName ASPECT_DIGITAL_PHOTOGRAPH_RECORD = QName.createQName(DOD_URI, "digitalPhotographRecord");
    QName PROP_CAPTION = QName.createQName(DOD_URI, "caption");
    QName PROP_PHOTOGRAPHER = QName.createQName(DOD_URI, "photographer");
    QName PROP_COPYRIGHT = QName.createQName(DOD_URI, "copyright");
    QName PROP_BIT_DEPTH = QName.createQName(DOD_URI, "bitDepth");
    QName PROP_IMAGE_SIZE_X = QName.createQName(DOD_URI, "imageSizeX");
    QName PROP_IMAGE_SIZE_Y = QName.createQName(DOD_URI, "imageSizeY");
    QName PROP_IMAGE_SOURCE = QName.createQName(DOD_URI, "imageSource");
    QName PROP_COMPRESSION = QName.createQName(DOD_URI, "compression");
    QName PROP_ICC_ICM_PROFILE = QName.createQName(DOD_URI, "iccIcmProfile");
    QName PROP_EXIF_INFORMATION = QName.createQName(DOD_URI, "exifInformation");

    // Web Record
    QName ASPECT_WEB_RECORD = QName.createQName(DOD_URI, "webRecord");
    QName PROP_WEB_FILE_NAME = QName.createQName(DOD_URI, "webFileName");
    QName PROP_WEB_PLATFORM = QName.createQName(DOD_URI, "webPlatform");
    QName PROP_WEBSITE_NAME = QName.createQName(DOD_URI, "webSiteName");
    QName PROP_WEB_SITE_URL = QName.createQName(DOD_URI, "webSiteURL");
    QName PROP_CAPTURE_METHOD = QName.createQName(DOD_URI, "captureMethod");
    QName PROP_CAPTURE_DATE = QName.createQName(DOD_URI, "captureDate");
    QName PROP_CONTACT = QName.createQName(DOD_URI, "contact");
    QName PROP_CONTENT_MANAGEMENT_SYSTEM = QName.createQName(DOD_URI, "contentManagementSystem");
}
