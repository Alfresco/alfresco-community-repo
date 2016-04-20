package org.alfresco.repo.transfer.report;

/**
 * The transfer report model - extended for XML Manifest Model
 */
public interface TransferReportModel2 extends TransferReportModel
{
  
    static final String TRANSFER_REPORT_MODEL_2_0_URI = "http://www.alfresco.org/model/transferReport/2.0";

    // New in 1.1
    static final String LOCALNAME_TRANSFER_EVENT_START_STATE = "eventStartState";
    static final String LOCALNAME_TRANSFER_EVENT_END_STATE = "eventEndState";
    static final String LOCALNAME_TRANSFER_EVENT_REPORT = "eventReport";
    static final String LOCALNAME_TRANSFER_EVENT_SENDING_CONTENT = "eventSendContent";
    static final String LOCALNAME_TRANSFER_EVENT_SENDING_SNAPSHOT = "eventSendSnapshot";
    static final String LOCALNAME_TRANSFER_EVENT_BEGIN = "eventBegin";
    static final String LOCALNAME_TRANSFER_EVENT_COMMITTING_STATUS = "eventCommittingStatus";
    static final String LOCALNAME_TRANSFER_EVENT_SENT_CONTENT = "eventSentContent";
    static final String LOCALNAME_TRANSFER_EVENT_CANCELLED = "eventCancelled";
    static final String LOCALNAME_TRANSFER_EVENT_ERROR = "eventError";
    static final String LOCALNAME_TRANSFER_EVENT_SUCCESS = "eventSuccess";

}
