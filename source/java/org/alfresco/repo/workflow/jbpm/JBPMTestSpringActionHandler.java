/*
 * Copyright (C) 2005 Alfresco, Inc.
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
