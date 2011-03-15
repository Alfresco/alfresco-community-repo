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
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
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
        descriptorService = factory.getBean("DescriptorService", DescriptorService.class);
    }

}
