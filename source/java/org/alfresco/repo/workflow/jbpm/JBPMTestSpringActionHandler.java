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
     * @param value String
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
