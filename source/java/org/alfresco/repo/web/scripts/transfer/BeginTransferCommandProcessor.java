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

import java.io.StringWriter;

import org.alfresco.repo.transfer.RepoTransferReceiverImpl;
import org.alfresco.repo.transfer.TransferCommons;
import org.alfresco.repo.transfer.TransferVersionImpl;
import org.alfresco.service.cmr.transfer.TransferException;
import org.alfresco.service.cmr.transfer.TransferReceiver;
import org.alfresco.service.cmr.transfer.TransferVersion;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
public class BeginTransferCommandProcessor implements CommandProcessor
{
    private static final String MSG_CAUGHT_UNEXPECTED_EXCEPTION = "transfer_service.receiver.caught_unexpected_exception";

    private TransferReceiver receiver;
    
    private final static Log logger = LogFactory.getLog(BeginTransferCommandProcessor.class);

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.web.scripts.transfer.CommandProcessor#process(org.alfresco .web.scripts.WebScriptRequest,
     * org.alfresco.web.scripts.WebScriptResponse)
     */
    public int process(WebScriptRequest req, WebScriptResponse resp)
    {
        String transferId = null;
        try
        {
            String [] fromRepositoryIdValues = req.getParameterValues(TransferCommons.PARAM_FROM_REPOSITORYID);
            String [] transferToSelfValues = req.getParameterValues(TransferCommons.PARAM_ALLOW_TRANSFER_TO_SELF);
            String [] editionValues = req.getParameterValues(TransferCommons.PARAM_VERSION_EDITION);
            String [] majorValues = req.getParameterValues(TransferCommons.PARAM_VERSION_MAJOR);
            String [] minorValues = req.getParameterValues(TransferCommons.PARAM_VERSION_MINOR); 
            String [] revisionValues = req.getParameterValues(TransferCommons.PARAM_VERSION_REVISION);
                          
            String fromRepositoryId = null;
            if(fromRepositoryIdValues != null && fromRepositoryIdValues.length > 0)
            {
                fromRepositoryId = fromRepositoryIdValues[0];
            }
            
            boolean transferToSelf = false;
            if(transferToSelfValues != null && transferToSelfValues.length > 0)
            {
                if(transferToSelfValues[0].equalsIgnoreCase("true"))
                {
                    transferToSelf = true;
                }
            }
            
            String edition = "Unknown";
            if(editionValues != null && editionValues.length > 0)
            {
                edition = editionValues[0];
            }
            String major = "0";
            if(majorValues != null && majorValues.length > 0)
            {
                major = majorValues[0];
            }
            String minor = "0";
            if(minorValues != null && minorValues.length > 0)
            {
                minor = minorValues[0];
            }
            String revision = "0";
            if(revisionValues != null && revisionValues.length > 0)
            {
                revision = revisionValues[0];
            }
            
            TransferVersion fromVersion = new TransferVersionImpl(major, minor, revision, edition);
            
            // attempt to start the transfer
            transferId = receiver.start(fromRepositoryId, transferToSelf, fromVersion);

            // Create a temporary folder into which we can place transferred files
            receiver.getStagingFolder(transferId);
            
            TransferVersion version = receiver.getVersion();

            // return the unique transfer id (the lock id)
            StringWriter stringWriter = new StringWriter(1000);
            JSONWriter jsonWriter = new JSONWriter(stringWriter);
            jsonWriter.startObject();
            
            jsonWriter.writeValue(TransferCommons.PARAM_TRANSFER_ID, transferId);
            
            if(version != null)
            {
                jsonWriter.writeValue(TransferCommons.PARAM_VERSION_EDITION, version.getEdition());
                jsonWriter.writeValue(TransferCommons.PARAM_VERSION_MAJOR, version.getVersionMajor());
                jsonWriter.writeValue(TransferCommons.PARAM_VERSION_MINOR, version.getVersionMinor()); 
                jsonWriter.writeValue(TransferCommons.PARAM_VERSION_REVISION, version.getVersionRevision());
            }
            jsonWriter.endObject();
            
            String response = stringWriter.toString();
            
            resp.setContentType("application/json");
            resp.setContentEncoding("UTF-8");
            int length = response.getBytes("UTF-8").length;
            resp.addHeader("Content-Length", "" + length);
            resp.setStatus(Status.STATUS_OK);
            resp.getWriter().write(response);
            
            logger.debug("transfer started" + transferId);
            
            return Status.STATUS_OK;

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
    }

    /**
     * @param receiver the receiver to set
     */
    public void setReceiver(TransferReceiver receiver)
    {
        this.receiver = receiver;
    }

    
}
