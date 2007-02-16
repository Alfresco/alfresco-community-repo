/*
 * Copyright (C) 2006 Alfresco, Inc.
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
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * http://www.alfresco.com/legal/licensing" */

package org.alfresco.repo.avm.wf;

import java.util.List;

import org.alfresco.repo.workflow.jbpm.JBPMSpringActionHandler;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avmsync.AVMDifference;
import org.alfresco.service.cmr.avmsync.AVMSyncService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;
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

    private static Logger fgLogger = Logger.getLogger(AVMSubmitHandler.class);
    
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
            fAVMService.getStoreProperty(storePath[0], QName.createQName(null, ".website.name")).
            getStringValue();
        String avmDest = webSiteName + "-staging:" + storePath[1];
        List<AVMDifference> diffs = 
            fAVMSyncService.compare(-1, avmSource, -1, avmDest, null);
        // TODO fix update comments if needed.
        // Ignore conflicts and older nodes for now.
        fAVMSyncService.update(diffs, null, true, true, false, false, null, null);
        // Now flatten out the source.
        fAVMSyncService.flatten(avmSource, avmDest);
    }
}
