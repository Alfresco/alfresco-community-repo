/*
 * Copyright (C) 2009-2010 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */

package org.alfresco.repo.web.scripts.transfer;

import javax.servlet.http.HttpServletRequest;

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
import org.springframework.extensions.webscripts.servlet.WebScriptServletRequest;
import org.springframework.extensions.webscripts.servlet.FormData.FormField;

/**
 * This command processor is used to receive one or more content files for a given transfer.
 * 
 * @author brian
 * 
 */
public class PostContentCommandProcessor implements CommandProcessor
{
    private TransferReceiver receiver;
    
    private static final String MSG_CAUGHT_UNEXPECTED_EXCEPTION = "transfer_service.receiver.caught_unexpected_exception";
    
    private static Log logger = LogFactory.getLog(PostContentCommandProcessor.class);

    /**
     * @param receiver
     *            the receiver to set
     */
    public void setReceiver(TransferReceiver receiver)
    {
        this.receiver = receiver;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.web.scripts.transfer.CommandProcessor#process(org.alfresco.web.scripts.WebScriptRequest,
     * org.alfresco.web.scripts.WebScriptResponse)
     */
    public int process(WebScriptRequest req, WebScriptResponse resp)
    {
        logger.debug("post content start");
        if (!WebScriptServletRequest.class.isAssignableFrom(req.getClass()))
        {
            resp.setStatus(Status.STATUS_BAD_REQUEST);
            return Status.STATUS_BAD_REQUEST;
        }

        HttpServletRequest servletRequest = ((WebScriptServletRequest) req).getHttpServletRequest();

        //Read the transfer id from the request
        String transferId = servletRequest.getParameter("transferId");

        if ((transferId == null) || !ServletFileUpload.isMultipartContent(servletRequest))
        {
            resp.setStatus(Status.STATUS_BAD_REQUEST);
            return Status.STATUS_BAD_REQUEST;
        }

        try
        {
            //TODO - See comments on PostSnapshotCommandProcessor RE WebScript framework
            // ServletFileUpload upload = new ServletFileUpload();
            //            FileItemIterator iter = upload.getItemIterator(servletRequest);
            //            while (iter.hasNext())
            //            {
            //                FileItemStream item = iter.next();
            //                if (!item.isFormField() && "content".equals(item.getFieldName()))
            //                {
            //                    receiver.saveContent(transferId, item.getName(), item.openStream());
            //                }
            //            }
            //            
            
            WebScriptServletRequest alfRequest = (WebScriptServletRequest)req;
            String[] names = alfRequest.getParameterNames();
            for(String name : names)
            {
                FormField item = alfRequest.getFileField(name);
                
                if(item != null)
                {
                    logger.debug("got content Mime Part : " + name);
                    receiver.saveContent(transferId, item.getName(), item.getInputStream());
                }
                else
                {
                    //TODO - should this be an exception?
                    logger.debug("Unable to get content for Mime Part : " + name);
                }
            }
            
            logger.debug("success");
            
            resp.setStatus(Status.STATUS_OK);
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

        resp.setStatus(Status.STATUS_OK);
        return Status.STATUS_OK;
    }

}
