/*
 * Copyright (C) 2006 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */

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
            fAVMSyncService.compare(-1, avmSource, -1, avmDest);
        // TODO fix update comments if needed.
        // Ignore conflicts and older nodes for now.
        fAVMSyncService.update(diffs, true, true, false, false, null, null);
        // Now flatten out the source.
        fAVMSyncService.flatten(avmSource, avmDest);
    }
}
