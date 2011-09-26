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

package org.alfresco.repo.web.scripts.transfer;

import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;

import org.alfresco.repo.transfer.TransferCommons;
import org.alfresco.service.cmr.transfer.TransferException;
import org.alfresco.service.cmr.transfer.TransferReceiver;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.WrappingWebScriptRequest;
import org.springframework.extensions.webscripts.servlet.WebScriptServletRequest;
import org.springframework.extensions.webscripts.servlet.FormData.FormField;

/**
 * This command processor is used to receive the snapshot for a given transfer.
 * 
 * @author brian
 *
 */
public class PostSnapshotCommandProcessor implements CommandProcessor
{
    private TransferReceiver receiver;
    
    private static Log logger = LogFactory.getLog(PostSnapshotCommandProcessor.class);

    private static final String MSG_CAUGHT_UNEXPECTED_EXCEPTION = "transfer_service.receiver.caught_unexpected_exception";

    /* (non-Javadoc)
     * @see org.alfresco.repo.web.scripts.transfer.CommandProcessor#process(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.WebScriptResponse)
     */
    public int process(WebScriptRequest req, WebScriptResponse resp)
    {
        
        int result = Status.STATUS_OK;
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
        if (webScriptServletRequest == null) 
        {
            logger.debug("bad request, not assignable from");
            resp.setStatus(Status.STATUS_BAD_REQUEST);
            return Status.STATUS_BAD_REQUEST;
        }
                
        //We can't use the WebScriptRequest version of getParameter, since that may cause the content stream 
        //to be parsed. Get hold of the raw HttpServletRequest and work with that.
        HttpServletRequest servletRequest = webScriptServletRequest.getHttpServletRequest();
        
        //Read the transfer id from the request
        String transferId = servletRequest.getParameter("transferId");
        
        if ((transferId == null) || !ServletFileUpload.isMultipartContent(servletRequest)) 
        {
            logger.debug("bad request, not multipart");
            resp.setStatus(Status.STATUS_BAD_REQUEST);
            return Status.STATUS_BAD_REQUEST;
        }
        
        try 
        {
            logger.debug("about to upload manifest file");

            ServletFileUpload upload = new ServletFileUpload();
            FileItemIterator iter = upload.getItemIterator(servletRequest);
            while (iter.hasNext()) 
            {
                FileItemStream item = iter.next();
                if (!item.isFormField() && TransferCommons.PART_NAME_MANIFEST.equals(item.getFieldName())) 
                {
                    logger.debug("got manifest file");
                    receiver.saveSnapshot(transferId, item.openStream());
                }
            }
          
            logger.debug("success");
            resp.setStatus(Status.STATUS_OK);
            
            OutputStream out = resp.getOutputStream();
            resp.setContentType("text/xml");
            resp.setContentEncoding("utf-8");
   
            receiver.generateRequsite(transferId, out);
            
            out.close();
                        
        } 
        catch (Exception ex) 
        {
            logger.debug("exception caught", ex);
            if(transferId != null)
            {
                logger.debug("ending transfer", ex);
                receiver.end(transferId);
            }
            if (ex instanceof TransferException)
            {
                throw (TransferException) ex;
            }
            throw new TransferException(MSG_CAUGHT_UNEXPECTED_EXCEPTION, ex);
        }
        return result;
    }

    /**
     * @param receiver the receiver to set
     */
    public void setReceiver(TransferReceiver receiver)
    {
        this.receiver = receiver;
    }

}
