/*-----------------------------------------------------------------------------
*  Copyright 2007 Alfresco Inc.
*  
*  This program is free software; you can redistribute it and/or modify
*  it under the terms of the GNU General Public License as published by
*  the Free Software Foundation; either version 2 of the License, or
*  (at your option) any later version.
*  
*  This program is distributed in the hope that it will be useful, but
*  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
*  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
*  for more details.
*  
*  You should have received a copy of the GNU General Public License along
*  with this program; if not, write to the Free Software Foundation, Inc.,
*  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.  As a special
*  exception to the terms and conditions of version 2.0 of the GPL, you may
*  redistribute this Program in connection with Free/Libre and Open Source
*  Software ("FLOSS") applications as described in Alfresco's FLOSS exception.
*  You should have received a copy of the text describing the FLOSS exception,
*  and it is also available here:   http://www.alfresco.com/legal/licensing
*  
*  
*  Author  Jon Cox  <jcox@alfresco.com>
*  File    AVMRemoveAllWebappsHandler.java
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
import org.alfresco.util.Pair;
import org.apache.log4j.Logger;
import org.jbpm.graph.exe.ExecutionContext;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;

/**
 * Remove all webapps in a sandbox
 * 
 * @author Jon Cox
 */
public class AVMRemoveAllWebappsHandler extends JBPMSpringActionHandler 
{
    static final long serialVersionUID = 3004374776252613278L;

    private static Logger log = 
        Logger.getLogger(AVMRemoveAllWebappsHandler.class);

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
            log.debug("AVMRemoveAllWebappsHandler.execute()");

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
