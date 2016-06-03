package org.alfresco.repo.transfer.report;

import org.alfresco.repo.transfer.TransferModel;

/**
 * The transfer report model - extended for XML Manifest Model
 */
public interface TransferReportModel extends TransferModel
{
    static final String LOCALNAME_TRANSFER_REPORT = "transferReport";
    static final String LOCALNAME_TRANSFER_TARGET = "target";
    static final String LOCALNAME_TRANSFER_DEFINITION = "definition";
    static final String LOCALNAME_EXCEPTION = "exception";
    static final String LOCALNAME_TRANSFER_EVENTS = "events";
    static final String LOCALNAME_TRANSFER_EVENT = "event";
    static final String LOCALNAME_TRANSFER_NODE = "node";
    static final String LOCALNAME_TRANSFER_PRIMARY_PATH = "primaryPath";
    static final String LOCALNAME_TRANSFER_PRIMARY_PARENT = "primaryParent";
    static final String REPORT_PREFIX = "report";
    
    static final String TRANSFER_REPORT_MODEL_1_0_URI = "http://www.alfresco.org/model/transferReport/1.0";
}
