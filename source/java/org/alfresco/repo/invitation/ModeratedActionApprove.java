package org.alfresco.repo.invitation;


import org.alfresco.repo.invitation.WorkflowModelNominatedInvitation;
import org.alfresco.repo.invitation.site.AcceptInviteAction;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.MutableAuthenticationDao;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.workflow.jbpm.JBPMSpringActionHandler;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.jbpm.graph.exe.ExecutionContext;
import org.springframework.beans.factory.BeanFactory;

/**
 *
 */
public class ModeratedActionApprove extends JBPMSpringActionHandler
{
    private static final long serialVersionUID = 4377660284993206875L;
    
    private MutableAuthenticationDao mutableAuthenticationDao;
    private PersonService personService;
    private WorkflowService workflowService;
    private SiteService siteService;

    /* (non-Javadoc)
     * @see org.alfresco.repo.workflow.jbpm.JBPMSpringActionHandler#initialiseHandler(org.springframework.beans.factory.BeanFactory)
     */
    @Override
    protected void initialiseHandler(BeanFactory factory)
    {
        ServiceRegistry services = (ServiceRegistry)factory.getBean(ServiceRegistry.SERVICE_REGISTRY);
        mutableAuthenticationDao = (MutableAuthenticationDao) factory.getBean("authenticationDao");
        personService = (PersonService) services.getPersonService();
        workflowService = (WorkflowService) services.getWorkflowService();
        siteService = services.getSiteService();
    }

    /* (non-Javadoc)
     * @see org.jbpm.graph.def.ActionHandler#execute(org.jbpm.graph.exe.ExecutionContext)
     * Approve Moderated
     **/
    @SuppressWarnings("unchecked")
    public void execute(final ExecutionContext executionContext) throws Exception
    {
        final String resourceType = (String)executionContext.getVariable(WorkflowModelModeratedInvitation.wfVarResourceType);
        final String resourceName = (String)executionContext.getVariable(WorkflowModelModeratedInvitation.wfVarResourceName);
        final String inviteeUserName = (String)executionContext.getVariable(WorkflowModelModeratedInvitation.wfVarInviteeUserName);
        final String inviteeRole = (String)executionContext.getVariable(WorkflowModelModeratedInvitation.wfVarInviteeRole);
        final String reviewer = (String)executionContext.getVariable(WorkflowModelModeratedInvitation.wfVarReviewer);
        final String reviewComments = (String)executionContext.getVariable(WorkflowModelModeratedInvitation.wfVarReviewComments);
    	
    	System.out.println("resourceType=" + resourceType);
    	System.out.println("resourceName=" + resourceName);
    	System.out.println("role=" + inviteeRole);
    	System.out.println("inviteeUserName=" + inviteeUserName);
    	System.out.println("reviewer=" + reviewer);
    	System.out.println("reviewComments=" + reviewComments);
    	
        /**
         * Add invitee to the site
         */
        AuthenticationUtil.runAs(new RunAsWork<Object>()
        {
            public Object doWork() throws Exception
            {
                siteService.setMembership(resourceName, inviteeUserName, inviteeRole);
                return null;
            }
            
        }, reviewer);
        
    }
}