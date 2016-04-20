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
