/*
 * Copyright (C) 2009-2010 Alfresco Software Limited.
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
package org.alfresco.repo.transfer.reportd;

import org.alfresco.repo.transfer.TransferModel;

/**
 * The transfer report model - extended for XML Manifest Model
 */
public interface TransferDestinationReportModel extends TransferModel
{
    static final String LOCALNAME_TRANSFER_DEST_REPORT = "transferDestinationReport";
    //static final String LOCALNAME_TRANSFER_DEFINITION = "definition";
    static final String LOCALNAME_EXCEPTION = "exception";

    static final String LOCALNAME_TRANSFER_NODE = "node";
    
    static final String LOCALNAME_TRANSFER_STATE = "state";
    static final String LOCALNAME_TRANSFER_COMMENT = "comment";
    static final String LOCALNAME_TRANSFER_CREATED = "created";
    static final String LOCALNAME_TRANSFER_UPDATED = "updated";
    static final String LOCALNAME_TRANSFER_MOVED = "moved";
    static final String LOCALNAME_TRANSFER_DELETED = "deleted";
    
    static final String LOCALNAME_TRANSFER_OLD_PATH = "oldPath";
    static final String LOCALNAME_TRANSFER_DEST_PATH = "destinationPath";
    
    static final String LOCALNAME_TRANSFER_PRIMARY_PATH = "primaryPath";
    static final String LOCALNAME_TRANSFER_PRIMARY_PARENT = "primaryParent";
    
    static final String REPORT_PREFIX = "report";
    
    static final String TRANSFER_REPORT_MODEL_1_0_URI = "http://www.alfresco.org/model/transferDestinationReport/1.0";
}
