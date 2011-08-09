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
package org.alfresco.repo.transfer;

import org.alfresco.service.namespace.QName;

/**
 * Transfer Model Constants
 *
 * @author Mark Rogers
 * @author Brian Remmington
 */
public interface TransferModel
{
    static final String TRANSFER_MODEL_1_0_URI = "http://www.alfresco.org/model/transfer/1.0";

    static final QName ASPECT_ENABLEABLE = QName.createQName(TRANSFER_MODEL_1_0_URI, "enableable");
//  static final QName ASSOC_IMAP_ATTACHMENTS_FOLDER = QName.createQName(IMAP_MODEL_1_0_URI, "attachmentsFolder");

    /**
     * Aspect : transferred
     */
    static final QName ASPECT_TRANSFERRED = QName.createQName(TRANSFER_MODEL_1_0_URI, "transferred");
    static final QName PROP_REPOSITORY_ID = QName.createQName(TRANSFER_MODEL_1_0_URI, "repositoryId");
    static final QName PROP_FROM_REPOSITORY_ID = QName.createQName(TRANSFER_MODEL_1_0_URI, "fromRepositoryId");
    static final QName PROP_FROM_CONTENT = QName.createQName(TRANSFER_MODEL_1_0_URI, "fromContent");

    /**
     * Aspect : alien
     */
    static final QName ASPECT_ALIEN = QName.createQName(TRANSFER_MODEL_1_0_URI, "alien");
    static final QName PROP_INVADED_BY = QName.createQName(TRANSFER_MODEL_1_0_URI, "invadedBy");

    /**
     * Aspect : fileTransferTarget
     */
    static final QName ASPECT_FILE_TRANSFER_TARGET = QName.createQName(TRANSFER_MODEL_1_0_URI, "fileTransferTarget");
    static final QName ASSOC_ROOT_FILE_TRANSFER = QName.createQName(TRANSFER_MODEL_1_0_URI, "rootFileTransfer");

    /*
     * Type : Transfer Group
     */
    static final QName TYPE_TRANSFER_GROUP = QName.createQName(TRANSFER_MODEL_1_0_URI, "transferGroup");

    /*
     * Type : Transfer Target
     */
    static final QName TYPE_TRANSFER_TARGET = QName.createQName(TRANSFER_MODEL_1_0_URI, "transferTarget");
    static final QName PROP_ENDPOINT_PROTOCOL = QName.createQName(TRANSFER_MODEL_1_0_URI, "endpointprotocol");
    static final QName PROP_ENDPOINT_HOST = QName.createQName(TRANSFER_MODEL_1_0_URI, "endpointhost");
    static final QName PROP_ENDPOINT_PORT = QName.createQName(TRANSFER_MODEL_1_0_URI, "endpointport");
    static final QName PROP_ENDPOINT_PATH = QName.createQName(TRANSFER_MODEL_1_0_URI, "endpointpath");
    static final QName PROP_USERNAME = QName.createQName(TRANSFER_MODEL_1_0_URI, "username");
    static final QName PROP_PASSWORD = QName.createQName(TRANSFER_MODEL_1_0_URI, "password");

    static final QName PROP_ENABLED = QName.createQName(TRANSFER_MODEL_1_0_URI, "enabled");

    /*
     * Type : Transfer Lock
     */
    static final QName TYPE_TRANSFER_LOCK = QName.createQName(TRANSFER_MODEL_1_0_URI, "transferLock");
    static final QName PROP_TRANSFER_ID = QName.createQName(TRANSFER_MODEL_1_0_URI, "transferId");

    /*
     * Type : Transfer Record
     */
    static final QName TYPE_TRANSFER_RECORD = QName.createQName(TRANSFER_MODEL_1_0_URI, "transferRecord");
    static final QName PROP_PROGRESS_POSITION = QName.createQName(TRANSFER_MODEL_1_0_URI, "progressPosition");
    static final QName PROP_PROGRESS_ENDPOINT = QName.createQName(TRANSFER_MODEL_1_0_URI, "progressEndpoint");
    static final QName PROP_TRANSFER_STATUS = QName.createQName(TRANSFER_MODEL_1_0_URI, "transferStatus");
    static final QName PROP_TRANSFER_ERROR = QName.createQName(TRANSFER_MODEL_1_0_URI, "transferError");

    /*
     * Type : Transfer report
     */
    static final QName TYPE_TRANSFER_REPORT = QName.createQName(TRANSFER_MODEL_1_0_URI, "transferReport");
    static final QName TYPE_TRANSFER_REPORT_DEST = QName.createQName(TRANSFER_MODEL_1_0_URI, "transferReportDest");

    /*
     * Type : Temp Transfer Storage
     */
    static final QName TYPE_TEMP_TRANSFER_STORE = QName.createQName(TRANSFER_MODEL_1_0_URI, "tempTransferStore");
    static final QName ASSOC_TRANSFER_ORPHAN = QName.createQName(TRANSFER_MODEL_1_0_URI, "orphan");


}
