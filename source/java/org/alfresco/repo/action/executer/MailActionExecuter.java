/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.repo.action.executer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.repo.template.DateCompareMethod;
import org.alfresco.repo.template.HasAspectMethod;
import org.alfresco.repo.template.I18NMessageMethod;
import org.alfresco.repo.template.TemplateNode;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.util.UrlUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.mail.MailException;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;

/**
 * Mail action executor implementation.
 * 
 * @author Roy Wetherall
 */
public class MailActionExecuter extends ActionExecuterAbstractBase
                                implements InitializingBean, TestModeable
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
    public static final String PARAM_HTML = "html";
    public static final String PARAM_FROM = "from";
    public static final String PARAM_TEMPLATE = "template";
    public static final String PARAM_TEMPLATE_MODEL = "template_model";
    public static final String PARAM_IGNORE_SEND_FAILURE = "ignore_send_failure";
    public static final String PARAM_SEND_AFTER_COMMIT = "send_after_commit";
       
    /**
     * From address
     */
    private static final String FROM_ADDRESS = "alfresco@alfresco.org";
    
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
     * System Administration parameters, including URL information
     */
    private SysAdminParams sysAdminParams;
    
    /**
     * Mail header encoding scheme
     */
    private String headerEncoding = null;
    
    /**
     * Default from address
     */
    private String fromDefaultAddress = null;
    
    /**
     * Is the from field enabled? Or must we always use the default address.
     */
    private boolean fromEnabled = true;
    
    
    private boolean sendTestMessage = false;
    private String testMessageTo = null;
    private String testMessageSubject = "Test message";
    private String testMessageText = "This is a test message.";

    /**
     * Test mode prevents email messages from being sent.
     * It is used when unit testing when we don't actually want to send out email messages.
     * 
     * MER 20/11/2009 This is a quick and dirty fix. It should be replaced by being 
     * "mocked out" or some other better way of running the unit tests. 
     */
    private boolean testMode = false;
    private MimeMessage lastTestMessage;
    
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
     * @param headerEncoding     The mail header encoding to set.
     */
    public void setHeaderEncoding(String headerEncoding)
    {
        this.headerEncoding = headerEncoding;
    }

    /**
     * @param fromAddress   The default mail address.
     */
    public void setFromAddress(String fromAddress)
    {
        this.fromDefaultAddress = fromAddress;
    }

    
    public void setSysAdminParams(SysAdminParams sysAdminParams)
    {
        this.sysAdminParams = sysAdminParams;
    }
    
    public void setTestMessageTo(String testMessageTo)
    {
        this.testMessageTo = testMessageTo;
    }
    
    public void setTestMessageSubject(String testMessageSubject)
    {
        this.testMessageSubject = testMessageSubject;
    }
    
    public void setTestMessageText(String testMessageText)
    {
        this.testMessageText = testMessageText;
    }

    public void setSendTestMessage(boolean sendTestMessage)
    {
        this.sendTestMessage = sendTestMessage;
    }

    
    @Override
    public void init()
    {
        super.init();
        if (sendTestMessage)
        {
            Map<String, Serializable> params = new HashMap<String, Serializable>();
            params.put(PARAM_TO, testMessageTo);
            params.put(PARAM_SUBJECT, testMessageSubject);
            params.put(PARAM_TEXT, testMessageText);
            
            Action ruleAction = serviceRegistry.getActionService().createAction(NAME, params);
            executeImpl(ruleAction, null);
        }
    }

    /**
     * Initialise bean
     */
    public void afterPropertiesSet() throws Exception
    {
        if (fromDefaultAddress == null || fromDefaultAddress.length() == 0)
        {
            fromDefaultAddress = FROM_ADDRESS;
        }
        
    }
    
    /**
     * Send an email message
     * 
     * @throws AlfrescoRuntimeExeption
     */
    @Override
    protected void executeImpl(
            final Action ruleAction,
            final NodeRef actionedUponNodeRef) 
    {
        MimeMessageHelper message = null;
        if (!testMode && validNodeRefIfPresent(actionedUponNodeRef))
        {
            message = prepareEmail(ruleAction, actionedUponNodeRef);
        }
        final MimeMessageHelper finalMessage = message;
        
        if (sendAfterCommit(ruleAction))
        {
            AlfrescoTransactionSupport.bindListener(new TransactionListenerAdapter()
            {
                @Override
                public void afterCommit()
                {                                    
                    RetryingTransactionHelper helper = serviceRegistry.getRetryingTransactionHelper();
                    helper.doInTransaction(new RetryingTransactionCallback<Void>()
                    {
                        @Override
                        public Void execute() throws Throwable
                        {
                        	if (finalMessage != null)
                            {
                                   sendEmail(ruleAction, actionedUponNodeRef, finalMessage);
                            }
                            return null;
                        }
                    }, false, true);          
                }
            });            
        }
        else
        {
            if (validNodeRefIfPresent(actionedUponNodeRef))
            {
                sendEmail(ruleAction, actionedUponNodeRef, finalMessage);
            }
        }
    }
    
    
    private boolean validNodeRefIfPresent(NodeRef actionedUponNodeRef)
    {
        if (actionedUponNodeRef == null)
        {
            // We must expect that null might be passed in (ALF-11625)
            // since the mail action might not relate to a specific nodeRef.
            return true;
        }
        else
        {
            // Only try and send the email if the actioned upon node reference still exists
            // (i.e. if one has been specified it must be valid)
            return nodeService.exists(actionedUponNodeRef);
        }
    }
    
    private boolean sendAfterCommit(Action action)
    {
        Boolean sendAfterCommit = (Boolean) action.getParameterValue(PARAM_SEND_AFTER_COMMIT);
        return sendAfterCommit == null ? false : sendAfterCommit.booleanValue();
    }
    
    public MimeMessageHelper prepareEmail(final Action ruleAction, final NodeRef actionedUponNodeRef)
    {
        // Create the mime mail message.
        // Hack: using an array here to get around the fact that inner classes aren't closures.
        // The MimeMessagePreparator.prepare() signature does not allow us to return a value and yet
        // we can't set a result on a bare, non-final object reference due to Java language restrictions.
        final MimeMessageHelper[] messageRef = new MimeMessageHelper[1];
        
        MimeMessagePreparator mailPreparer = new MimeMessagePreparator()
        {
            @SuppressWarnings("unchecked")
            public void prepare(MimeMessage mimeMessage) throws MessagingException
            {
                if (logger.isDebugEnabled())
                {
                   logger.debug(ruleAction.getParameterValues());
                }
                
                messageRef[0] = new MimeMessageHelper(mimeMessage);
                
                // set header encoding if one has been supplied
                if (headerEncoding != null && headerEncoding.length() != 0)
                {
                    mimeMessage.setHeader("Content-Transfer-Encoding", headerEncoding);
                }
                
                // set recipient
                String to = (String)ruleAction.getParameterValue(PARAM_TO);
                if (to != null && to.length() != 0)
                {
                    messageRef[0].setTo(to);
                }
                else
                {
                    // see if multiple recipients have been supplied - as a list of authorities
                    Serializable authoritiesValue = ruleAction.getParameterValue(PARAM_TO_MANY);
                    List<String> authorities = null;
                    if (authoritiesValue != null)
                    {
                        if (authoritiesValue instanceof String)
                        {
                            authorities = new ArrayList<String>(1);
                            authorities.add((String)authoritiesValue);
                        }
                        else
                        {
                            authorities = (List<String>)authoritiesValue;
                        }
                    }
                    
                    if (authorities != null && authorities.size() != 0)
                    {
                        List<String> recipients = new ArrayList<String>(authorities.size());
                        for (String authority : authorities)
                        {
                            AuthorityType authType = AuthorityType.getAuthorityType(authority);
                            if (authType.equals(AuthorityType.USER))
                            {
                                if (personService.personExists(authority) == true)
                                {
                                    NodeRef person = personService.getPerson(authority);
                                    String address = (String)nodeService.getProperty(person, ContentModel.PROP_EMAIL);
                                    if (address != null && address.length() != 0 && validateAddress(address))
                                    {
                                        recipients.add(address);
                                    }
                                }
                            }
                            else if (authType.equals(AuthorityType.GROUP) || authType.equals(AuthorityType.EVERYONE))
                            {
                                // Notify all members of the group
                                Set<String> users;
                                if (authType.equals(AuthorityType.GROUP))
                                {        
                                    users = authorityService.getContainedAuthorities(AuthorityType.USER, authority, false);
                                }
                                else
                                {
                                    users = authorityService.getAllAuthorities(AuthorityType.USER);
                                }
                                
                                for (String userAuth : users)
                                {
                                    if (personService.personExists(userAuth) == true)
                                    {
                                        NodeRef person = personService.getPerson(userAuth);
                                        String address = (String)nodeService.getProperty(person, ContentModel.PROP_EMAIL);
                                        if (address != null && address.length() != 0)
                                        {
                                            recipients.add(address);
                                        }
                                    }
                                }
                            }
                        }
                        
                        if(recipients.size() > 0)
                        {
                            messageRef[0].setTo(recipients.toArray(new String[recipients.size()]));
                        }
                        else
                        {
                            // All recipients were invalid
                            throw new MailPreparationException(
                                    "All recipients for the mail action were invalid"
                            );
                        }
                    }
                    else
                    {
                        // No recipients have been specified
                        throw new MailPreparationException(
                                "No recipient has been specified for the mail action"
                        );
                    }
                }
                
                // from person
                NodeRef fromPerson = null;
            
                // from is enabled
                if (! authService.isCurrentUserTheSystemUser())
                {
                    fromPerson = personService.getPerson(authService.getCurrentUserName());
                }
                
                if(isFromEnabled())
                {   
                    // Use the FROM parameter in preference to calculating values.
                    String from = (String)ruleAction.getParameterValue(PARAM_FROM);
                    if (from != null && from.length() > 0)
                    {
                        if(logger.isDebugEnabled())
                        {
                            logger.debug("from specified as a parameter, from:" + from);
                        }
                        messageRef[0].setFrom(from);
                    }
                    else
                    {
                        // set the from address from the current user
                        String fromActualUser = null;
                        if (fromPerson != null)
                        {
                            fromActualUser = (String) nodeService.getProperty(fromPerson, ContentModel.PROP_EMAIL);
                        }
                    
                        if (fromActualUser != null && fromActualUser.length() != 0)
                        {
                            if(logger.isDebugEnabled())
                            {
                                logger.debug("looked up email address for :" + fromPerson + " email from " + fromActualUser);
                            }
                            messageRef[0].setFrom(fromActualUser);
                        }
                        else
                        {
                            // from system or user does not have email address
                            messageRef[0].setFrom(fromDefaultAddress);
                        }
                    }

                }
                else
                {
                    if(logger.isDebugEnabled())
                    {
                        logger.debug("from not enabled - sending from default address:" + fromDefaultAddress);
                    }
                    // from is not enabled.
                    messageRef[0].setFrom(fromDefaultAddress);
                }
                


                
                // set subject line
                messageRef[0].setSubject((String)ruleAction.getParameterValue(PARAM_SUBJECT));
                
                // See if an email template has been specified
                String text = null;
                NodeRef templateRef = (NodeRef)ruleAction.getParameterValue(PARAM_TEMPLATE);
                if (templateRef != null)
                {
                    Map<String, Object> suppliedModel = null;
                    if(ruleAction.getParameterValue(PARAM_TEMPLATE_MODEL) != null)
                    {
                        Object m = ruleAction.getParameterValue(PARAM_TEMPLATE_MODEL);
                        if(m instanceof Map)
                        {
                            suppliedModel = (Map<String, Object>)m;
                        }
                        else
                        {
                            logger.warn("Skipping unsupported email template model parameters of type "
                                    + m.getClass().getName() + " : " + m.toString());
                        }
                    }
                    
                    // build the email template model
                    Map<String, Object> model = createEmailTemplateModel(actionedUponNodeRef, suppliedModel, fromPerson);
                    
                    // process the template against the model
                    text = templateService.processTemplate("freemarker", templateRef.toString(), model);
                }
                
                // set the text body of the message
                
                boolean isHTML = false;
                if (text == null)
                {
                    text = (String)ruleAction.getParameterValue(PARAM_TEXT);
                }
                
                if (text != null)
                {
                    // Note: only simplistic match here - expects <html tag at the start of the text
                    String htmlPrefix = "<html";
                    if (text.length() >= htmlPrefix.length() &&
                            text.substring(0, htmlPrefix.length()).equalsIgnoreCase(htmlPrefix))
                    {
                        isHTML = true;
                    }
                }
                else
                {
                    text = (String)ruleAction.getParameterValue(PARAM_HTML);
                    if (text != null)
                    {
                        // assume HTML
                        isHTML = true;
                    }
                }
                
                if (text != null)
                {
                    messageRef[0].setText(text, isHTML);
                }
                
            }
        };
        MimeMessage mimeMessage = javaMailSender.createMimeMessage(); 
        try
        {
            mailPreparer.prepare(mimeMessage);
        } catch (Exception e)
        {
            // We're forced to catch java.lang.Exception here. Urgh.
            if (logger.isInfoEnabled())
            {
                logger.warn("Unable to prepare mail message. Skipping.", e);
            }
        }
        
        return messageRef[0];
    }
    
    private void sendEmail(final Action ruleAction, final NodeRef actionedUponNodeRef, MimeMessageHelper preparedMessage)
    {
        try
        {
            // Send the message unless we are in "testMode"
            if(!testMode && preparedMessage != null)
            {
                javaMailSender.send(preparedMessage.getMimeMessage());
            }
            else if (validNodeRefIfPresent(actionedUponNodeRef))
            {
               try {
                  MimeMessage mimeMessage = javaMailSender.createMimeMessage(); 
                  prepareEmail(ruleAction, actionedUponNodeRef);
                  lastTestMessage = mimeMessage;
               } catch(Exception e) {
                  System.err.println(e);
               }
            }
        }
        catch (MailException e)
        {
            String to = (String)ruleAction.getParameterValue(PARAM_TO);
            if (to == null)
            {
               Object obj = ruleAction.getParameterValue(PARAM_TO_MANY);
               if (obj != null)
               {
                  to = obj.toString();
               }
            }
            
            // always log the failure
            logger.error("Failed to send email to " + to, e);
            
            // optionally ignore the throwing of the exception
            Boolean ignoreError = (Boolean)ruleAction.getParameterValue(PARAM_IGNORE_SEND_FAILURE);
            if (ignoreError == null || ignoreError.booleanValue() == false)
            {
                throw new AlfrescoRuntimeException("Failed to send email to:" + to, e);
            }   
        }
    }
    
    
    /**
     * Return true if address has valid format
     * @param address
     * @return
     */
    private boolean validateAddress(String address)
    {
        boolean result = false;
        
        // Validate the email, allowing for local email addresses
        EmailValidator emailValidator = EmailValidator.getInstance(true);
        if (emailValidator.isValid(address))
        {
            result = true;
        }
        else 
        {
            logger.error("Failed to send email to '" + address + "' as the address is incorrectly formatted" );
        }
      
        return result;
    }

   /**
    * @param ref    The node representing the current document ref (or null)
    * 
    * @return Model map for email templates
    */
   private Map<String, Object> createEmailTemplateModel(NodeRef ref, Map<String, Object> suppliedModel, NodeRef fromPerson)
   {
      Map<String, Object> model = new HashMap<String, Object>(8, 1.0f);
      
      if (fromPerson != null)
      {
          model.put("person", new TemplateNode(fromPerson, serviceRegistry, null));
      }      
      
      if (ref != null)
      {
          model.put("document", new TemplateNode(ref, serviceRegistry, null));
          NodeRef parent = serviceRegistry.getNodeService().getPrimaryParent(ref).getParentRef();
          model.put("space", new TemplateNode(parent, serviceRegistry, null));
      }
      
      // current date/time is useful to have and isn't supplied by FreeMarker by default
      model.put("date", new Date());
      
      // add custom method objects
      model.put("hasAspect", new HasAspectMethod());
      model.put("message", new I18NMessageMethod());
      model.put("dateCompare", new DateCompareMethod());
      
      // add URLs
      model.put("url", new URLHelper(sysAdminParams));
      model.put(TemplateService.KEY_SHARE_URL, UrlUtil.getShareUrl(this.serviceRegistry.getSysAdminParams()));
      
      // if the caller specified a model, use it without overriding
      if(suppliedModel != null && suppliedModel.size() > 0)
      {
          for(String key : suppliedModel.keySet())
          {
              if(model.containsKey(key))
              {
                  if(logger.isDebugEnabled())
                  {
                      logger.debug("Not allowing overwriting of built in model parameter " + key);
                  }
              }
              else
              {
                  model.put(key, suppliedModel.get(key));
              }
          }
      }
      
      // all done
      return model;
   }
    
    /**
     * Add the parameter definitions
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) 
    {
        paramList.add(new ParameterDefinitionImpl(PARAM_TO, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_TO)));
        paramList.add(new ParameterDefinitionImpl(PARAM_TO_MANY, DataTypeDefinition.ANY, false, getParamDisplayLabel(PARAM_TO_MANY), true));
        paramList.add(new ParameterDefinitionImpl(PARAM_SUBJECT, DataTypeDefinition.TEXT, true, getParamDisplayLabel(PARAM_SUBJECT)));
        paramList.add(new ParameterDefinitionImpl(PARAM_TEXT, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_TEXT)));
        paramList.add(new ParameterDefinitionImpl(PARAM_FROM, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_FROM)));
        paramList.add(new ParameterDefinitionImpl(PARAM_TEMPLATE, DataTypeDefinition.NODE_REF, false, getParamDisplayLabel(PARAM_TEMPLATE), false, "ac-email-templates"));
        paramList.add(new ParameterDefinitionImpl(PARAM_TEMPLATE_MODEL, DataTypeDefinition.ANY, false, getParamDisplayLabel(PARAM_TEMPLATE_MODEL), true));
        paramList.add(new ParameterDefinitionImpl(PARAM_IGNORE_SEND_FAILURE, DataTypeDefinition.BOOLEAN, false, getParamDisplayLabel(PARAM_IGNORE_SEND_FAILURE)));
    }

    public void setTestMode(boolean testMode)
    {
        this.testMode = testMode;
    }

    public boolean isTestMode()
    {
        return testMode;
    }

    /**
     * Returns the most recent message that wasn't sent
     *  because TestMode had been enabled.
     */
    public MimeMessage retrieveLastTestMessage()
    {
        return lastTestMessage; 
    }
    
    /**
     * Used when test mode is enabled.
     * Clears the record of the last message that was sent. 
     */
    public void clearLastTestMessage()
    {
        lastTestMessage = null;
    }

    public void setFromEnabled(boolean fromEnabled)
    {
        this.fromEnabled = fromEnabled;
    }

    public boolean isFromEnabled()
    {
        return fromEnabled;
    }

    public static class URLHelper
    {
        private final SysAdminParams sysAdminParams;
        
        public URLHelper(SysAdminParams sysAdminParams)
        {
            this.sysAdminParams = sysAdminParams;
        }
        
        public String getContext()
        {
           return "/" + sysAdminParams.getAlfrescoContext();
        }

        public String getServerPath()
        {
            return sysAdminParams.getAlfrescoProtocol() + "://" + sysAdminParams.getAlfrescoHost() + ":"
                    + sysAdminParams.getAlfrescoPort();
        }
    }
}
