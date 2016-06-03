
package org.alfresco.repo.web.scripts.transfer;

import javax.servlet.http.HttpServletRequest;

import org.alfresco.service.cmr.transfer.TransferException;
import org.alfresco.service.cmr.transfer.TransferProgress;
import org.alfresco.service.cmr.transfer.TransferReceiver;
import org.alfresco.util.json.ExceptionJsonSerializer;
import org.alfresco.util.json.JsonSerializer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.WrappingWebScriptRequest;
import org.springframework.extensions.webscripts.servlet.WebScriptServletRequest;

/**
 * This command processor is used to record the start a transfer. No other transfer can be started after this command
 * has executed until the started transfer terminates.
 * 
 * @author brian
 * 
 */
public class StatusCommandProcessor implements CommandProcessor
{
    private static final String MSG_CAUGHT_UNEXPECTED_EXCEPTION = "transfer_service.receiver.caught_unexpected_exception";

    private TransferReceiver receiver;
    private JsonSerializer<Throwable, JSONObject> errorSerializer = new ExceptionJsonSerializer();

    private final static Log logger = LogFactory.getLog(StatusCommandProcessor.class);

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.web.scripts.transfer.CommandProcessor#process(org.alfresco .web.scripts.WebScriptRequest,
     * org.alfresco.web.scripts.WebScriptResponse)
     */
    public int process(WebScriptRequest req, WebScriptResponse resp)
    {   
        //Read the transfer id from the request
        // Unwrap to a WebScriptServletRequest if we have one
        WebScriptServletRequest webScriptServletRequest = null;
        WebScriptRequest current = req;
        do
        {
            if (current instanceof WebScriptServletRequest)
            {
                webScriptServletRequest = (WebScriptServletRequest) current;
                current = null;
            }
            else if (current instanceof WrappingWebScriptRequest)
            {
                current = ((WrappingWebScriptRequest) req).getNext();
            }
            else
            {
                current = null;
            }
        }
        while (current != null);
        HttpServletRequest servletRequest = webScriptServletRequest.getHttpServletRequest();
        String transferId = servletRequest.getParameter("transferId");

        if (transferId == null) 
        {
            logger.debug("transferId is missing");
            resp.setStatus(Status.STATUS_BAD_REQUEST);
            return Status.STATUS_BAD_REQUEST;
        }
        
        try
        {
            TransferProgress progress = receiver.getProgressMonitor().getProgress(transferId);

            if (logger.isDebugEnabled())
            {
                logger.debug(progress);
            }
            
            JSONObject progressObject = new JSONObject();
            progressObject.put("transferId", transferId);
            progressObject.put("status", progress.getStatus().toString());
            progressObject.put("currentPosition", progress.getCurrentPosition());
            progressObject.put("endPosition", progress.getEndPosition());
            if (progress.getError() != null)
            {
                JSONObject errorObject = errorSerializer.serialize(progress.getError());
                progressObject.put("error", errorObject);
            }
            String response = progressObject.toString();

            resp.setContentType("application/json");
            resp.setContentEncoding("UTF-8");
            int length = response.getBytes("UTF-8").length;
            resp.addHeader("Content-Length", "" + length);
            resp.setStatus(Status.STATUS_OK);
            resp.getWriter().write(response);

            return Status.STATUS_OK;

        }
        catch (TransferException ex)
        {
            throw ex;
        }
        catch (Exception ex)
        {
            throw new TransferException(MSG_CAUGHT_UNEXPECTED_EXCEPTION, ex);
        }
    }

    /**
     * @param receiver
     *            the receiver to set
     */
    public void setReceiver(TransferReceiver receiver)
    {
        this.receiver = receiver;
    }

    public void setErrorSerializer(JsonSerializer<Throwable, JSONObject> errorSerializer)
    {
        this.errorSerializer = errorSerializer;
    }

}
