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
package org.alfresco.repo.action.executer;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.template.DateCompareMethod;
import org.alfresco.repo.template.HasAspectMethod;
import org.alfresco.repo.template.I18NMessageMethod;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TemplateNode;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PersonService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * Mail action executor implementation.
 * 
 * @author Roy Wetherall
 */
public class MailActionExecuter extends ActionExecuterAbstractBase 
{
    private static Log logger = LogFactory.getLog(MailActionExecuter.class);
    
    /**
     * Action executor constants
     */
    public static final String NAME = "mail";
    public static final String PARAM_TO = "to";
    public static final String PARAM_TO_MANY = "to_many";
    public static final String PARAM_SUBJECT = "subject";
    public static final String PARAM_TEXT = "text";
    public static final String PARAM_FROM = "from";
    public static final String PARAM_TEMPLATE = "template";
    
    /**
     * From address
     */
    public static final String FROM_ADDRESS = "alfresco_repository@alfresco.org";
    
    /**
     * The java mail sender
     */
    private JavaMailSender javaMailSender;
    
    /**
     * The Template service
     */
    private TemplateService templateService;
    
    /**
     * The Person service
     */
    private PersonService personService;
    
    /**
     * The Authentication service
     */
    private AuthenticationService authService;
    
    /**
     * The Node Service
     */
    private NodeService nodeService;
    
    /**
     * The Authority Service
     */
    private AuthorityService authorityService;
    
    /**
     * The Service registry
     */
    private ServiceRegistry serviceRegistry;
    
    /**
     * @param javaMailSender    the java mail sender
     */
    public void setMailService(JavaMailSender javaMailSender) 
    {
        this.javaMailSender = javaMailSender;
    }
    
    /**
     * @param templateService   the TemplateService
     */
    public void setTemplateService(TemplateService templateService)
    {
        this.templateService = templateService;
    }
    
    /**
     * @param personService     the PersonService
     */
    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }
    
    /**
     * @param authService       the AuthenticationService
     */
    public void setAuthenticationService(AuthenticationService authService)
    {
        this.authService = authService;
    }
    
    /**
     * @param serviceRegistry   the ServiceRegistry
     */
    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }
    
    /**
     * @param authorityService  the AuthorityService
     */
    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }

    /**
     * @param nodeService       the NodeService to set.
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * Execute the rule action
     */
    @Override
    protected void executeImpl(
            Action ruleAction,
            NodeRef actionedUponNodeRef) 
    {
        // Create the simple mail message
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        
        // set recipient
        String to = (String)ruleAction.getParameterValue(PARAM_TO);
        if (to != null && to.length() != 0)
        {
            simpleMailMessage.setTo(to);
        }
        else
        {
            // see if multiple recipients have been supplied - as a list of authorities
            List<String> authorities = (List<String>)ruleAction.getParameterValue(PARAM_TO_MANY);
            if (authorities != null && authorities.size() != 0)
            {
                List<String> recipients = new ArrayList<String>(authorities.size());
                for (String authority : authorities)
                {
                    AuthorityType authType = AuthorityType.getAuthorityType(authority);
                    if (authType.equals(AuthorityType.USER))
                    {
                        if (this.personService.personExists(authority) == true)
                        {
                            NodeRef person = this.personService.getPerson(authority);
                            String address = (String)this.nodeService.getProperty(person, ContentModel.PROP_EMAIL);
                            if (address != null && address.length() != 0)
                            {
                                recipients.add(address);
                            }
                        }
                    }
                    else if (authType.equals(AuthorityType.GROUP))
                    {
                        // else notify all members of the group
                        Set<String> users = this.authorityService.getContainedAuthorities(AuthorityType.USER, authority, false);
                        for (String userAuth : users)
                        {
                            if (this.personService.personExists(userAuth) == true)
                            {
                                NodeRef person = this.personService.getPerson(authority);
                                String address = (String)this.nodeService.getProperty(person, ContentModel.PROP_EMAIL);
                                if (address != null && address.length() != 0)
                                {
                                    recipients.add(address);
                                }
                            }
                        }
                    }
                }
                
                simpleMailMessage.setTo(recipients.toArray(new String[recipients.size()]));
            }
        }
        
        // set subject line
        simpleMailMessage.setSubject((String)ruleAction.getParameterValue(PARAM_SUBJECT));
        
        // See if an email template has been specified
        String text = null;
        NodeRef templateRef = (NodeRef)ruleAction.getParameterValue(PARAM_TEMPLATE);
        if (templateRef != null)
        {
            // build the email template model
            Map<String, Object> model = createEmailTemplateModel(actionedUponNodeRef);
            
            // process the template against the model
            text = templateService.processTemplate("freemarker", templateRef.toString(), model);
        }
        
        // set the text body of the message
        if (text == null)
        {
            text = (String)ruleAction.getParameterValue(PARAM_TEXT);
        }
        simpleMailMessage.setText(text);
        
        // set the from address - use the default if not set
        String from = (String)ruleAction.getParameterValue(PARAM_FROM);
        if (from != null)
        {
            simpleMailMessage.setFrom(from);
        }
        else
        {
            simpleMailMessage.setFrom(FROM_ADDRESS);
        }
        
        try
        {
            // Send the message
            javaMailSender.send(simpleMailMessage);
        }
        catch (Throwable e)
        {
            // don't stop the action but let admins know email is not getting sent
            logger.error("Failed to send email to " + (String)ruleAction.getParameterValue(PARAM_TO), e);
        }
    }

   /**
    * @param ref    The node representing the current document ref
    * 
    * @return Model map for email templates
    */
   private Map<String, Object> createEmailTemplateModel(NodeRef ref)
   {
      Map<String, Object> model = new HashMap<String, Object>(8, 1.0f);
      
      NodeRef person = personService.getPerson(authService.getCurrentUserName());
      model.put("person", new TemplateNode(person, serviceRegistry, null));
      model.put("document", new TemplateNode(ref, serviceRegistry, null));
      NodeRef parent = serviceRegistry.getNodeService().getPrimaryParent(ref).getParentRef();
      model.put("space", new TemplateNode(parent, serviceRegistry, null));
      
      // current date/time is useful to have and isn't supplied by FreeMarker by default
      model.put("date", new Date());
      
      // add custom method objects
      model.put("hasAspect", new HasAspectMethod());
      model.put("message", new I18NMessageMethod());
      model.put("dateCompare", new DateCompareMethod());
      
      return model;
   }
    
    /**
     * Add the parameter definitions
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) 
    {
        paramList.add(new ParameterDefinitionImpl(PARAM_TO, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_TO)));
        paramList.add(new ParameterDefinitionImpl(PARAM_TO_MANY, DataTypeDefinition.ANY, false, getParamDisplayLabel(PARAM_TO_MANY)));
        paramList.add(new ParameterDefinitionImpl(PARAM_SUBJECT, DataTypeDefinition.TEXT, true, getParamDisplayLabel(PARAM_SUBJECT)));
        paramList.add(new ParameterDefinitionImpl(PARAM_TEXT, DataTypeDefinition.TEXT, true, getParamDisplayLabel(PARAM_TEXT)));
        paramList.add(new ParameterDefinitionImpl(PARAM_FROM, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_FROM)));
        paramList.add(new ParameterDefinitionImpl(PARAM_TEMPLATE, DataTypeDefinition.NODE_REF, false, getParamDisplayLabel(PARAM_TEMPLATE)));
    }
}
