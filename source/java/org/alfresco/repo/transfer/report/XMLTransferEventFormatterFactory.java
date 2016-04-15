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

import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.transfer.TransferEvent;
import org.alfresco.service.cmr.transfer.TransferEventBegin;
import org.alfresco.service.cmr.transfer.TransferEventEndState;
import org.alfresco.service.cmr.transfer.TransferEventEnterState;
import org.alfresco.service.cmr.transfer.TransferEventReport;
import org.alfresco.service.cmr.transfer.TransferEventSendingContent;
import org.alfresco.service.cmr.transfer.TransferEventCommittingStatus;
import org.alfresco.service.cmr.transfer.TransferEventCancelled;
import org.alfresco.service.cmr.transfer.TransferEventError;
import org.alfresco.service.cmr.transfer.TransferEventSentContent;
import org.alfresco.service.cmr.transfer.TransferEventSuccess;

import org.alfresco.service.cmr.transfer.TransferEventSendingSnapshot;
import org.springframework.extensions.surf.util.ISO8601DateFormat;
import org.xml.sax.helpers.AttributesImpl;

/**
 * The XMLTransferEventFormatterFactory returns formatters for the various client side TransferEvents. 
 * 
 * The main entry point for this class is the static method getFormatter();
 * @author mrogers
 *
 */
public class XMLTransferEventFormatterFactory
{
    
    private static XMLTransferEventFormatter defaultFormatter = new XMLTransferEventFormatter() {

        @Override
        public AttributesImpl getAttributes(TransferEvent event)
        {
            AttributesImpl attributes = new AttributesImpl();
            attributes.addAttribute(TransferReportModel.TRANSFER_REPORT_MODEL_1_0_URI, "date", "date", "dateTime", ISO8601DateFormat.format(event.getTime()));
            return attributes;
        }

        @Override
        public String getElementName(TransferEvent event)
        {
            return TransferReportModel.LOCALNAME_TRANSFER_EVENT;
        }

        @Override
        public String getMessage(TransferEvent event)
        {
            return event.getMessage();
        }
    };
    
    private static XMLTransferEventFormatter eventEnterStateFormatter = new XMLTransferEventFormatter() {

        @Override
        public AttributesImpl getAttributes(TransferEvent event)
        {
            TransferEventEnterState s = (TransferEventEnterState)event;
            
            AttributesImpl attributes = new AttributesImpl();
            attributes.addAttribute(TransferReportModel.TRANSFER_REPORT_MODEL_1_0_URI, "date", "date", "dateTime", ISO8601DateFormat.format(event.getTime()));
            attributes.addAttribute(TransferReportModel.TRANSFER_REPORT_MODEL_1_0_URI, "state", "state", "string", s.getTransferState().toString());
            return attributes;
        }

        @Override
        public String getElementName(TransferEvent event)
        {
            return TransferReportModel2.LOCALNAME_TRANSFER_EVENT_START_STATE;

        }

        @Override
        public String getMessage(TransferEvent event)
        {
            return event.getMessage();
        }
    };
    
    private static XMLTransferEventFormatter eventReportFormatter = new XMLTransferEventFormatter() {

        @Override
        public AttributesImpl getAttributes(TransferEvent event)
        {
            TransferEventReport s = (TransferEventReport)event;
            AttributesImpl attributes = new AttributesImpl();
            attributes.addAttribute(TransferReportModel.TRANSFER_REPORT_MODEL_1_0_URI, "date", "date", "dateTime", ISO8601DateFormat.format(event.getTime()));
            attributes.addAttribute(TransferReportModel.TRANSFER_REPORT_MODEL_1_0_URI, "nodeRef", "nodeRef", "string", s.getNodeRef().toString());
            attributes.addAttribute(TransferReportModel.TRANSFER_REPORT_MODEL_1_0_URI, "reportType", "reportType", "string", s.getReportType().toString());
            return attributes;
        }

        @Override
        public String getElementName(TransferEvent event)
        {
            return TransferReportModel2.LOCALNAME_TRANSFER_EVENT_REPORT;
        }

        @Override
        public String getMessage(TransferEvent event)
        {
            return null;
        }
    };
    
    private static XMLTransferEventFormatter eventSendingContentFormatter = new XMLTransferEventFormatter() {

        @Override
        public AttributesImpl getAttributes(TransferEvent event)
        {
            TransferEventSendingContent s = (TransferEventSendingContent)event;
            AttributesImpl attributes = new AttributesImpl();
            attributes.addAttribute(TransferReportModel.TRANSFER_REPORT_MODEL_1_0_URI, "date", "date", "dateTime", ISO8601DateFormat.format(event.getTime()));
            attributes.addAttribute(TransferReportModel.TRANSFER_REPORT_MODEL_1_0_URI, "position", "position", "string", String.valueOf(s.getPosition()));
            attributes.addAttribute(TransferReportModel.TRANSFER_REPORT_MODEL_1_0_URI, "range", "range", "string", String.valueOf(s.getRange()));
            attributes.addAttribute(TransferReportModel.TRANSFER_REPORT_MODEL_1_0_URI, "size", "size", "string", String.valueOf(s.getSize()));
            return attributes;
        }

        @Override
        public String getElementName(TransferEvent event)
        {
            return TransferReportModel2.LOCALNAME_TRANSFER_EVENT_SENDING_CONTENT;
        }

        @Override
        public String getMessage(TransferEvent event)
        {
            return null;
        }
    };
    
    private static XMLTransferEventFormatter eventSendingSnapshotFormatter = new XMLTransferEventFormatter() {

        @Override
        public AttributesImpl getAttributes(TransferEvent event)
        {
            TransferEventSendingSnapshot s = (TransferEventSendingSnapshot)event;
            AttributesImpl attributes = new AttributesImpl();
            attributes.addAttribute(TransferReportModel.TRANSFER_REPORT_MODEL_1_0_URI, "date", "date", "dateTime", ISO8601DateFormat.format(event.getTime()));
            return attributes;
        }

        @Override
        public String getElementName(TransferEvent event)
        {
            return TransferReportModel2.LOCALNAME_TRANSFER_EVENT_SENDING_SNAPSHOT;
        }

        @Override
        public String getMessage(TransferEvent event)
        {
            return null;
        }
    };
    
    private static XMLTransferEventFormatter eventBeginFormatter = new XMLTransferEventFormatter() {

        @Override
        public AttributesImpl getAttributes(TransferEvent event)
        {
            TransferEventBegin s = (TransferEventBegin)event;
            AttributesImpl attributes = new AttributesImpl();
            attributes.addAttribute(TransferReportModel.TRANSFER_REPORT_MODEL_1_0_URI, "date", "date", "dateTime", ISO8601DateFormat.format(event.getTime()));
            attributes.addAttribute(TransferReportModel.TRANSFER_REPORT_MODEL_1_0_URI, "transferId", "transferId", "string", s.getTransferId());
            return attributes;
        }

        @Override
        public String getElementName(TransferEvent event)
        {
            return TransferReportModel2.LOCALNAME_TRANSFER_EVENT_BEGIN;
        }

        @Override
        public String getMessage(TransferEvent event)
        {
            return event.getMessage();
        }
    };
        
    private static XMLTransferEventFormatter eventEndStateFormatter = new XMLTransferEventFormatter() {

        @Override
        public AttributesImpl getAttributes(TransferEvent event)
        {
            TransferEventEndState s = (TransferEventEndState)event;
            AttributesImpl attributes = new AttributesImpl();
            attributes.addAttribute(TransferReportModel.TRANSFER_REPORT_MODEL_1_0_URI, "date", "date", "dateTime", ISO8601DateFormat.format(event.getTime()));
            attributes.addAttribute(TransferReportModel.TRANSFER_REPORT_MODEL_1_0_URI, "state", "state", "string", s.getTransferState().toString());
            return attributes;
        }

        @Override
        public String getElementName(TransferEvent event)
        {
            return TransferReportModel2.LOCALNAME_TRANSFER_EVENT_END_STATE;
        }

        @Override
        public String getMessage(TransferEvent event)
        {
            return null;
        }
    };
    
    private static XMLTransferEventFormatter eventCommittingStatus  = new XMLTransferEventFormatter() {

        @Override
        public AttributesImpl getAttributes(TransferEvent event)
        {
            TransferEventCommittingStatus s = (TransferEventCommittingStatus)event;
            AttributesImpl attributes = new AttributesImpl();
            attributes.addAttribute(TransferReportModel.TRANSFER_REPORT_MODEL_1_0_URI, "date", "date", "dateTime", ISO8601DateFormat.format(event.getTime()));
            attributes.addAttribute(TransferReportModel.TRANSFER_REPORT_MODEL_1_0_URI, "position", "position", "string", String.valueOf(s.getPosition()));
            attributes.addAttribute(TransferReportModel.TRANSFER_REPORT_MODEL_1_0_URI, "range", "range", "string", String.valueOf(s.getRange()));
            return attributes;
        }

        @Override
        public String getElementName(TransferEvent event)
        {
            return TransferReportModel2.LOCALNAME_TRANSFER_EVENT_COMMITTING_STATUS;
        }

        @Override
        public String getMessage(TransferEvent event)
        {
            return event.getMessage();
        }
    };
    
    private static XMLTransferEventFormatter eventCancelled  = new XMLTransferEventFormatter() {

        @Override
        public AttributesImpl getAttributes(TransferEvent event)
        {
            TransferEventCancelled s = (TransferEventCancelled)event;
            AttributesImpl attributes = new AttributesImpl();
            attributes.addAttribute(TransferReportModel.TRANSFER_REPORT_MODEL_1_0_URI, "date", "date", "dateTime", ISO8601DateFormat.format(event.getTime()));
            return attributes;
        }

        @Override
        public String getElementName(TransferEvent event)
        {
            return TransferReportModel2.LOCALNAME_TRANSFER_EVENT_CANCELLED;
        }

        @Override
        public String getMessage(TransferEvent event)
        {
            return event.getMessage();
        }
    };
    
    private static XMLTransferEventFormatter eventSuccess  = new XMLTransferEventFormatter() {

        @Override
        public AttributesImpl getAttributes(TransferEvent event)
        {
            TransferEventSuccess s = (TransferEventSuccess)event;
            AttributesImpl attributes = new AttributesImpl();
            attributes.addAttribute(TransferReportModel.TRANSFER_REPORT_MODEL_1_0_URI, "date", "date", "dateTime", ISO8601DateFormat.format(event.getTime()));
            return attributes;
        }

        @Override
        public String getElementName(TransferEvent event)
        {
            return TransferReportModel2.LOCALNAME_TRANSFER_EVENT_SUCCESS;
        }

        @Override
        public String getMessage(TransferEvent event)
        {
            return event.getMessage();
        }
    };
    
    private static XMLTransferEventFormatter eventSentContent  = new XMLTransferEventFormatter() {

        @Override
        public AttributesImpl getAttributes(TransferEvent event)
        {
            TransferEventSentContent s = (TransferEventSentContent)event;
            AttributesImpl attributes = new AttributesImpl();
            attributes.addAttribute(TransferReportModel.TRANSFER_REPORT_MODEL_1_0_URI, "date", "date", "dateTime", ISO8601DateFormat.format(event.getTime()));
            return attributes;
        }

        @Override
        public String getElementName(TransferEvent event)
        {
            return TransferReportModel2.LOCALNAME_TRANSFER_EVENT_SENT_CONTENT;
        }

        @Override
        public String getMessage(TransferEvent event)
        {
            return event.getMessage();
        }
    };
    
    private static XMLTransferEventFormatter eventError  = new XMLTransferEventFormatter() {

        @Override
        public AttributesImpl getAttributes(TransferEvent event)
        {
            TransferEventError s = (TransferEventError)event;
            AttributesImpl attributes = new AttributesImpl();
            attributes.addAttribute(TransferReportModel.TRANSFER_REPORT_MODEL_1_0_URI, "date", "date", "dateTime", ISO8601DateFormat.format(event.getTime()));
            return attributes;
        }

        @Override
        public String getElementName(TransferEvent event)
        {
            return TransferReportModel2.LOCALNAME_TRANSFER_EVENT_ERROR;
        }

        @Override
        public String getMessage(TransferEvent event)
        {
            TransferEventError s = (TransferEventError)event;
            return s.getException().getMessage();
        }
    };

    
    protected static Map<Class<?>, XMLTransferEventFormatter> formatters;
    
    static
    {
        formatters = new HashMap<Class<?>, XMLTransferEventFormatter>(29);
        formatters.put(TransferEventEnterState.class, eventEnterStateFormatter);
        formatters.put(TransferEventEndState.class, eventEndStateFormatter);
        formatters.put(TransferEventReport.class, eventReportFormatter);
        formatters.put(TransferEventSendingContent.class, eventSendingContentFormatter);
        formatters.put(TransferEventSendingSnapshot.class, eventSendingSnapshotFormatter);
        formatters.put(TransferEventBegin.class, eventBeginFormatter);
        formatters.put(TransferEventCommittingStatus.class, eventCommittingStatus);
        formatters.put(TransferEventCancelled.class, eventCancelled);
        formatters.put(TransferEventError.class, eventError);
        formatters.put(TransferEventSuccess.class, eventSuccess);
        formatters.put(TransferEventSentContent.class, eventSentContent);
    }
    
    public static XMLTransferEventFormatter getFormatter(TransferEvent event)
    {
        
        XMLTransferEventFormatter formatter = formatters.get(event.getClass());
        
        if(formatter == null)
        {
            return defaultFormatter;
        }
        else
        {
            return formatter;
        }
    }
    

}