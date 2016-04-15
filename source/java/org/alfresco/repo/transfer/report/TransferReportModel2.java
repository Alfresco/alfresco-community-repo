/*
 * #%L
 * Alfresco Repository
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
