package org.alfresco.repo.transfer.report;

import org.alfresco.service.cmr.transfer.TransferEvent;
import org.xml.sax.helpers.AttributesImpl;

public interface XMLTransferEventFormatter
{
    AttributesImpl getAttributes(TransferEvent event);
    String getElementName(TransferEvent event);
    String getMessage(TransferEvent event);
}
