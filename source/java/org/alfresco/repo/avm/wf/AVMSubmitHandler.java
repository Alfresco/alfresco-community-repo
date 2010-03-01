/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>. */

package org.alfresco.repo.avm.wf;

import java.util.List;

import org.alfresco.repo.workflow.jbpm.JBPMSpringActionHandler;
import org.alfresco.wcm.sandbox.SandboxConstants;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avmsync.AVMDifference;
import org.alfresco.service.cmr.avmsync.AVMSyncService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jbpm.graph.exe.ExecutionContext;
import org.springframework.beans.factory.BeanFactory;

/**
 * Performs a 'submit' operation: update from one sandbox layer to
 * its corresponding staging sandbox.
 * @author britt
 */
public class AVMSubmitHandler extends JBPMSpringActionHandler
{
    private static final long serialVersionUID = 7561005904505181493L;

    private static Log    fgLogger = LogFactory.getLog(AVMSubmitHandler.class);
    
    /**
     * The AVMSyncService.
     */ 
    private AVMSyncService fAVMSyncService;
    
    /**
     * The AVMService.
     */
    private AVMService fAVMService;
    
    /**
     * Set any bean references necessary.
     * @param factory The BeanFactory from which to get beans.
     */
    @Override
    protected void initialiseHandler(BeanFactory factory)
    {
        fAVMSyncService = (AVMSyncService)factory.getBean("AVMSyncService");
        fAVMService = (AVMService)factory.getBean("AVMService");
    }

    /**
     * Do the actual submit work.
     * @param executionContext The jBPM context.
     */
    public void execute(ExecutionContext executionContext) throws Exception
    {
        String avmSource = (String)executionContext.getContextInstance().getVariable("sourcePath");
        String [] storePath = avmSource.split(":");
        if (storePath.length != 2)
        {
            fgLogger.error("Malformed path: " + avmSource);
            return;
        }
        String webSiteName = 
            fAVMService.getStoreProperty(storePath[0], SandboxConstants.PROP_WEBSITE_NAME).
            getStringValue();
        String avmDest = webSiteName + ":" + storePath[1]; // note: it is implied that the website name is the same as staging name
        List<AVMDifference> diffs = 
            fAVMSyncService.compare(-1, avmSource, -1, avmDest, null);
        // TODO fix update comments if needed.
        // Ignore conflicts and older nodes for now.
        fAVMSyncService.update(diffs, null, true, true, false, false, null, null);
        // Now flatten out the source.
        fAVMSyncService.flatten(avmSource, avmDest);
    }
}
