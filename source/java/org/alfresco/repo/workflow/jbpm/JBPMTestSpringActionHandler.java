/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.repo.workflow.jbpm;

import org.alfresco.service.descriptor.DescriptorService;
import org.jbpm.graph.exe.ExecutionContext;
import org.springframework.beans.factory.BeanFactory;


/**
 * Test Spring based Jbpm Action Handler
 * 
 * @author davidc
 */
public class JBPMTestSpringActionHandler extends JBPMSpringActionHandler
{
    private static final long serialVersionUID = -7659883022289711381L;

    private DescriptorService descriptorService;
    private String value;
    

    /**
     * Setter accessible from jBPM jPDL
     * @param value
     */
    public void setValue(String value)
    {
        this.value = value; 
    }
    
    /*
     * (non-Javadoc)
     * @see org.jbpm.graph.def.ActionHandler#execute(org.jbpm.graph.exe.ExecutionContext)
     */
    public void execute(ExecutionContext arg0) throws Exception
    {
        String result = "Repo: " + descriptorService.getServerDescriptor().getVersion();
        result += ", Value: " + value + ", Node: " + arg0.getNode().getName() + ", Token: " + arg0.getToken().getFullName();
        arg0.getContextInstance().setVariable("jbpm.test.action.result", result);
    }

    @Override
    protected void initialiseHandler(BeanFactory factory)
    {
        descriptorService = (DescriptorService)factory.getBean("DescriptorService", DescriptorService.class);
    }

}
