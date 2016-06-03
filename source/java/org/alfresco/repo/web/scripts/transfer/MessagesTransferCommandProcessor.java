
package org.alfresco.repo.web.scripts.transfer;

import java.io.StringWriter;

import org.alfresco.service.cmr.transfer.TransferException;
import org.alfresco.service.cmr.transfer.TransferReceiver;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.json.JSONWriter;

/**
 * This command processor is used to record the start a transfer. No other transfer can be started after this command
 * has executed until the started transfer terminates.
 * 
 * @author brian
 * 
 */
public class MessagesTransferCommandProcessor implements CommandProcessor
{
    private static final String MSG_CAUGHT_UNEXPECTED_EXCEPTION = "transfer_service.receiver.caught_unexpected_exception";

    private TransferReceiver receiver;

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.web.scripts.transfer.CommandProcessor#process(org.alfresco .web.scripts.WebScriptRequest,
     * org.alfresco.web.scripts.WebScriptResponse)
     */
    public int process(WebScriptRequest req, WebScriptResponse resp)
    {
        String transferRecordId = null;
        try
        {
            // return the unique transfer id (the lock id)
            StringWriter stringWriter = new StringWriter(300);
            JSONWriter jsonWriter = new JSONWriter(stringWriter);
            
            jsonWriter.startValue("data");

            jsonWriter.startArray();
            jsonWriter.startObject();
            //TODO - clearly a dummy message for now.
            jsonWriter.writeValue("message", "hello world");
            jsonWriter.endObject();
            jsonWriter.startObject();
            //TODO - clearly a dummy message for now.
            jsonWriter.writeValue("message", "message2");
            jsonWriter.endObject();
            jsonWriter.endArray();
            jsonWriter.endValue();
            String response = stringWriter.toString();
            
            resp.setContentType("application/json");
            resp.setContentEncoding("UTF-8");
            int length = response.getBytes("UTF-8").length;
            resp.addHeader("Content-Length", "" + length);
            resp.setStatus(Status.STATUS_OK);
            resp.getWriter().write(response);
            
            return Status.STATUS_OK;

        } catch (Exception ex)
        {
            receiver.end(transferRecordId);
            if (ex instanceof TransferException)
            {
                throw (TransferException) ex;
            }
            throw new TransferException(MSG_CAUGHT_UNEXPECTED_EXCEPTION, ex);
        }
    }

    /**
     * @param receiver the receiver to set
     */
    public void setReceiver(TransferReceiver receiver)
    {
        this.receiver = receiver;
    }

    
}
