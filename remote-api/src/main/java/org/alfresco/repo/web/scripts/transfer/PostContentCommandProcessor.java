/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
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

package org.alfresco.repo.web.scripts.transfer;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload2.core.FileItemInput;
import org.apache.commons.fileupload2.core.FileItemInputIterator;
import org.apache.commons.fileupload2.jakarta.JakartaServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.WrappingWebScriptRequest;
import org.springframework.extensions.webscripts.servlet.WebScriptServletRequest;

import org.alfresco.service.cmr.transfer.TransferException;
import org.alfresco.service.cmr.transfer.TransferReceiver;

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

    /* (non-Javadoc)
     * 
     * @see org.alfresco.repo.web.scripts.transfer.CommandProcessor#process(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.WebScriptResponse) */
    public int process(WebScriptRequest req, WebScriptResponse resp)
    {
        logger.debug("post content start");
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
        } while (current != null);
        if (webScriptServletRequest == null)
        {
            resp.setStatus(Status.STATUS_BAD_REQUEST);
            return Status.STATUS_BAD_REQUEST;
        }

        HttpServletRequest servletRequest = webScriptServletRequest.getHttpServletRequest();

        // Read the transfer id from the request
        String transferId = servletRequest.getParameter("transferId");

        if ((transferId == null) || !JakartaServletFileUpload.isMultipartContent(servletRequest))
        {
            resp.setStatus(Status.STATUS_BAD_REQUEST);
            return Status.STATUS_BAD_REQUEST;
        }

        try
        {

            JakartaServletFileUpload upload = new JakartaServletFileUpload();
            FileItemInputIterator iter = upload.getItemIterator(servletRequest);
            while (iter.hasNext())
            {
                FileItemInput item = iter.next();
                String name = item.getFieldName();
                if (!item.isFormField())
                {
                    logger.debug("got content Mime Part : " + name);
                    receiver.saveContent(transferId, item.getName(), item.getInputStream());
                }
            }

            // WebScriptServletRequest alfRequest = (WebScriptServletRequest)req;
            // String[] names = alfRequest.getParameterNames();
            // for(String name : names)
            // {
            // FormField item = alfRequest.getFileField(name);
            //
            // if(item != null)
            // {
            // logger.debug("got content Mime Part : " + name);
            // receiver.saveContent(transferId, item.getName(), item.getInputStream());
            // }
            // else
            // {
            // //TODO - should this be an exception?
            // logger.debug("Unable to get content for Mime Part : " + name);
            // }
            // }

            logger.debug("success");

            resp.setStatus(Status.STATUS_OK);
        }
        catch (Exception ex)
        {
            logger.debug("exception caught", ex);
            if (transferId != null)
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
