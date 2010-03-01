/*-----------------------------------------------------------------------------
*  Copyright 2007-2010 Alfresco Software Limited.
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
*  
*  
*  Author  Jon Cox  <jcox@alfresco.com>
*  File    AVMRemoveAllSrcWebappsHandler.java
*----------------------------------------------------------------------------*/

package org.alfresco.repo.avm.wf;

import java.util.Map;
import org.alfresco.config.JNDIConstants;
import org.alfresco.mbeans.VirtServerRegistry;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.avm.util.RawServices;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.repo.workflow.jbpm.JBPMNode;
import org.alfresco.repo.workflow.jbpm.JBPMSpringActionHandler;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.surf.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jbpm.graph.exe.ExecutionContext;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;

/**
 * Remove all webapps in a sandbox
 * 
 * @author Jon Cox
 */
public class AVMRemoveAllSrcWebappsHandler extends JBPMSpringActionHandler 
{
    static final long serialVersionUID = 3004374776252613278L;

    private static Log    log = 
        LogFactory.getLog(AVMRemoveAllSrcWebappsHandler.class);

    /**
     * The AVMService instance.
     */
    private AVMService fAVMService;    

    
    /**
     * Initialize service references.
     * @param factory The BeanFactory to get references from.
     */
    @Override
    protected void initialiseHandler(BeanFactory factory) 
    {
        fAVMService = (AVMService)factory.getBean("AVMService");
    }

    /**
     * Do the actual work.
     * @param executionContext The context to get stuff from.
     */
    public void execute(ExecutionContext executionContext) throws Exception 
    {
        if (log.isDebugEnabled())
            log.debug("AVMRemoveAllSrcWebappsHandler.execute()");

        // retrieve submitted package
        NodeRef pkg = ((JBPMNode)executionContext.getContextInstance().
                                 getVariable("bpm_package")).getNodeRef();

        Pair<Integer, String> pkgPath = AVMNodeConverter.ToAVMVersionPath(pkg);

        Integer version     =  pkgPath.getFirst();
        String  www_dir     =  pkgPath.getSecond();
        String  appbase_dir =  www_dir + "/" + JNDIConstants.DIR_DEFAULT_APPBASE;

        if (log.isDebugEnabled())
        {
            log.debug("version:     " +  version );
            log.debug("appbase_dir: " +  appbase_dir );
        }

        ApplicationContext springContext   = RawServices.Instance().getContext();
        VirtServerRegistry vServerRegistry = (VirtServerRegistry) 
                                             springContext.getBean("VirtServerRegistry");

        if (log.isDebugEnabled())
            log.debug("Sending JMX message to shut down workflow webapps");

        vServerRegistry.removeAllWebapps( version,  appbase_dir, true );

        if (log.isDebugEnabled())
            log.debug("Sent JMX message to shut down workflow webapps");
    }
}
