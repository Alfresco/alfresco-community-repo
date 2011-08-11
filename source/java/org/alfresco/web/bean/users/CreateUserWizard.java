/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.web.bean.users;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.validator.ValidatorException;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.person.PersonServiceImpl;
import org.alfresco.repo.tenant.TenantDomainMismatchException;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.usage.ContentUsageService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.context.UIContextService;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.LoginBean;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.spaces.CreateSpaceWizard;
import org.alfresco.web.bean.wizard.BaseWizardBean;
import org.alfresco.web.config.ClientConfigElement;
import org.alfresco.web.ui.common.ReportedException;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Kevin Roast
 */
public class CreateUserWizard extends BaseWizardBean
{
    private static final long serialVersionUID = 8008464794715380019L;
    
    private static Log logger = LogFactory.getLog(BaseWizardBean.class);
    protected static final String ERROR = "error_person";
    protected static final String ERROR_DOMAIN_MISMATCH = "error_domain_mismatch";
    
    private static final String MSG_ERROR_NEWUSER_HOME_SPACE = "error_newuser_home_space";
    
    protected static final String QUOTA_UNITS_KB = "kilobyte";
    protected static final String QUOTA_UNITS_MB = "megabyte";
    protected static final String QUOTA_UNITS_GB = "gigabyte";
    
    /** form variables */
    protected String firstName = null;
    protected String lastName = null;
    protected String userName = null;
    protected String password = null;
    protected String confirm = null;
    protected String email = null;
    protected String companyId = null;
    protected String homeSpaceName = "";
    protected NodeRef homeSpaceLocation = null;
    protected String presenceProvider = null;
    protected String presenceUsername = null;
    protected String organisation = null;
    protected String jobtitle = null;
    protected String location = null;
    
    protected Long sizeQuota = null; // null is also equivalent to -1 (ie. no quota limit set)
    protected String sizeQuotaUnits = null;

    /** AuthenticationService bean reference */
    transient private MutableAuthenticationService authenticationService;

    /** PersonService bean reference */
    transient private PersonService personService;
    
    /** TenantService bean reference */
    transient private TenantService tenantService;

    /** PermissionService bean reference */
    transient private PermissionService permissionService;

    /** OwnableService bean reference */
    transient private OwnableService ownableService;

    /** ContentUsageService bean reference */
    transient private ContentUsageService contentUsageService;
    
    /** ref to the company home space folder */
    private NodeRef companyHomeSpaceRef = null;

    /** ref to the default home location */
    private NodeRef defaultHomeSpaceRef;


    /**
     * @param authenticationService The AuthenticationService to set.
     */
    public void setAuthenticationService(MutableAuthenticationService authenticationService)
    {
        this.authenticationService = authenticationService;
    }

    /**
     * @return authenticationService
     */
    private MutableAuthenticationService getAuthenticationService()
    {
        if (authenticationService == null)
        {
           authenticationService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getAuthenticationService();
        }
        return authenticationService;
    }

    /**
     * @param personService The person service.
     */
    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }
    
    /**
     * @return personService
     */
    private PersonService getPersonService()
    {
        if (personService == null)
        {
           personService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getPersonService(); 
        }
        return personService;
    }

    /**
     * @param tenantService         The tenantService to set.
     */
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }
    
    /**
     * @return tenantService
     */
    private TenantService getTenantService()
    {
        if(tenantService == null)
        {
           tenantService = (TenantService) FacesHelper.getManagedBean(FacesContext.getCurrentInstance(), "tenantService");
        }
        return tenantService;
    }

    /**
     * @param permissionService The PermissionService to set.
     */
    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }
    
    /**
     * @return permissionService
     */
    private PermissionService getPermissionService()
    {
        if (permissionService == null)
        {
           permissionService  = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getPermissionService();
        }
        return permissionService;
    }

    /**
     * @param ownableService The ownableService to set.
     */
    public void setOwnableService(OwnableService ownableService)
    {
        this.ownableService = ownableService;
    }
    
    /**
     * @return ownableService
     */
    private OwnableService getOwnableService()
    {
        if (ownableService == null)
        {
           ownableService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getOwnableService();
        }
        return ownableService;
    }
   
    /**
     * @param contentUsageService The contentUsageService to set.
     */
    public void setContentUsageService(ContentUsageService contentUsageService)
    {
        this.contentUsageService = contentUsageService;
    }
    
    /**
     * @return contentUsageService
     */
    private ContentUsageService getContentUsageService()
    {
       if (contentUsageService == null)
       {
          contentUsageService = (ContentUsageService) FacesHelper.getManagedBean(FacesContext.getCurrentInstance(), "ContentUsageService");
       }
       return contentUsageService;
    }

    /**
     * Initialises the wizard
     */
    @Override
    public void init(Map<String, String> params)
    {
        super.init(params);

        // reset all variables
        this.firstName = "";
        this.lastName = "";
        this.userName = "";
        this.password = "";
        this.confirm = "";
        this.email = "";
        this.companyId = "";
        this.homeSpaceName = "";
        this.homeSpaceLocation = getDefaultHomeSpace();
        this.presenceProvider = "";
        this.presenceUsername = "";
        this.organisation = "";
        this.jobtitle = "";
        this.location = "";
        
        this.sizeQuota = null;
        this.sizeQuotaUnits = "";
    }
    
    @Override
    public String next()
    {
       String stepName = Application.getWizardManager().getCurrentStepName();
       
       if ("summary".equals(stepName))
       {
           FacesContext context = FacesContext.getCurrentInstance();
           
           if (! this.password.equals(this.confirm))
           {
               Utils.addErrorMessage(Application.getMessage(context, UsersDialog.ERROR_PASSWORD_MATCH));
           }
          
           checkTenantUserName();
           
           if (context.getMessages().hasNext())
           {
               Application.getWizardManager().getState().setCurrentStep(Application.getWizardManager().getCurrentStep() - 1);
           }
       }
       
       return super.next();
    }
    
    /**
     * @return Returns the summary data for the wizard.
     */
    public String getSummary()
    {
        ResourceBundle bundle = Application.getBundle(FacesContext.getCurrentInstance());
        
        String homeSpaceLabel = this.homeSpaceName;
        if (this.homeSpaceName.length() == 0 && this.homeSpaceLocation != null)
        {
            homeSpaceLabel = Repository.getNameForNode(this.getNodeService(), this.homeSpaceLocation);
        }
        
        String quotaLabel = "";
        if (this.sizeQuota != null && this.sizeQuota != -1L)
        {
            quotaLabel = Long.toString(this.sizeQuota) + bundle.getString(this.sizeQuotaUnits);
        }
        
        String presenceLabel = "";
        if (this.presenceProvider != null && this.presenceProvider.length() != 0)
        {
            presenceLabel = this.presenceUsername + " (" + presenceProvider + ")";
        }

        return buildSummary(
                new String[] {
                    bundle.getString("name"), bundle.getString("username"),
                    bundle.getString("password"), bundle.getString("homespace"),
                    bundle.getString("email"), bundle.getString("user_organization"),
                    bundle.getString("user_jobtitle"), bundle.getString("user_location"),
                    bundle.getString("presence_username"), bundle.getString("quota")},
                new String[] {
                    Utils.encode(this.firstName + " " + this.lastName), Utils.encode(this.userName),
                    "********", Utils.encode(homeSpaceLabel),
                    Utils.encode(this.email), Utils.encode(this.organisation),
                    Utils.encode(this.jobtitle), Utils.encode(this.location),
                    Utils.encode(presenceLabel), quotaLabel});
    }
    
    /**
     * Init the users screen
     */
    public void setupUsers(ActionEvent event)
    {
       invalidateUserList();
    }

    /**
     * @return Returns the companyId.
     */
    public String getCompanyId()
    {
        return this.companyId;
    }

    /**
     * @param companyId The companyId to set.
     */
    public void setCompanyId(String companyId)
    {
        this.companyId = companyId;
    }

    /**
     * @return Returns the presenceProvider.
     */
    public String getPresenceProvider()
    {
       return this.presenceProvider;
    }

    /**
     * @param presenceProvider
     *            The presenceProvider to set.
     */
    public void setPresenceProvider(String presenceProvider)
    {
       this.presenceProvider = presenceProvider;
    }

    /**
     * @return Returns the presenceUsername.
     */
    public String getPresenceUsername()
    {
       return this.presenceUsername;
    }

    /**
     * @param presenceUsername
     *            The presenceUsername to set.
     */
    public void setPresenceUsername(String presenceUsername)
    {
       this.presenceUsername = presenceUsername;
    }

    /**
     * @return Returns the email.
     */
    public String getEmail()
    {
        return this.email;
    }

    /**
     * @param email The email to set.
     */
    public void setEmail(String email)
    {
        this.email = email;
    }

    /**
     * @return Returns the firstName.
     */
    public String getFirstName()
    {
        return this.firstName;
    }

    /**
     * @param firstName The firstName to set.
     */
    public void setFirstName(String firstName)
    {
        this.firstName = firstName;
    }

    /**
     * @return Returns the homeSpaceLocation.
     */
    public NodeRef getHomeSpaceLocation()
    {
        return this.homeSpaceLocation;
    }

    /**
     * @param homeSpaceLocation The homeSpaceLocation to set.
     */
    public void setHomeSpaceLocation(NodeRef homeSpaceLocation)
    {
        this.homeSpaceLocation = homeSpaceLocation;
    }

    /**
     * @return Returns the homeSpaceName.
     */
    public String getHomeSpaceName()
    {
        return this.homeSpaceName;
    }

    /**
     * @param homeSpaceName The homeSpaceName to set.
     */
    public void setHomeSpaceName(String homeSpaceName)
    {
        this.homeSpaceName = homeSpaceName;
    }

    /**
     * @return Returns the lastName.
     */
    public String getLastName()
    {
        return this.lastName;
    }

    /**
     * @param lastName The lastName to set.
     */
    public void setLastName(String lastName)
    {
        this.lastName = lastName;
    }

    /**
     * @return Returns the userName.
     */
    public String getUserName()
    {
        return this.userName;
    }

    /**
     * @param userName The userName to set.
     */
    public void setUserName(String userName)
    {
        this.userName = (userName != null ? userName.trim() : null);
    }

    /**
     * @return Returns the password.
     */
    public String getPassword()
    {
        return this.password;
    }

    /**
     * @param password The password to set.
     */
    public void setPassword(String password)
    {
        this.password = password;
    }

    /**
     * @return Returns the confirm password.
     */
    public String getConfirm()
    {
        return this.confirm;
    }

    /**
     * @param confirm The confirm password to set.
     */
    public void setConfirm(String confirm)
    {
        this.confirm = confirm;
    }
    
    /**
     * @return the jobtitle
     */
    public String getJobtitle()
    {
        return this.jobtitle;
    }

    /**
     * @param jobtitle the jobtitle to set
     */
    public void setJobtitle(String jobtitle)
    {
        this.jobtitle = jobtitle;
    }

    /**
     * @return the location
     */
    public String getLocation()
    {
        return this.location;
    }

    /**
     * @param location the location to set
     */
    public void setLocation(String location)
    {
        this.location = location;
    }

    /**
     * @return the organisation
     */
    public String getOrganization()
    {
        return this.organisation;
    }

    /**
     * @param organisation the organisation to set
     */
    public void setOrganization(String organisation)
    {
        this.organisation = organisation;
    }

    public Long getSizeQuota()
    {
       return sizeQuota;
    }

    public void setSizeQuota(Long sizeQuota)
    {
       this.sizeQuota = sizeQuota;
    }

    public String getSizeQuotaUnits()
    {
       return sizeQuotaUnits;
    }

    public void setSizeQuotaUnits(String sizeQuotaUnits)
    {
       this.sizeQuotaUnits = sizeQuotaUnits;
    }

    // ------------------------------------------------------------------------------
    // Validator methods

    /**
     * Validate password field data is acceptable
     */
    public void validatePassword(FacesContext context, UIComponent component, Object value) throws ValidatorException
    {
        int minPasswordLength = Application.getClientConfig(context).getMinPasswordLength();
        
        String pass = (String)value;
        if (pass.length() < minPasswordLength || pass.length() > 256)
        {
            String err = MessageFormat.format(Application.getMessage(context, LoginBean.MSG_PASSWORD_LENGTH),
                    new Object[]{minPasswordLength, 256});
            throw new ValidatorException(new FacesMessage(err));
        }
    }

    /**
     * Validate Username field data is acceptable
     */
    public void validateUsername(FacesContext context, UIComponent component, Object value) throws ValidatorException
    {
        int minUsernameLength = Application.getClientConfig(context).getMinUsernameLength();
        
        String name = ((String)value).trim();
        if (name.length() < minUsernameLength || name.length() > 256)
        {
            String err = MessageFormat.format(Application.getMessage(context, LoginBean.MSG_USERNAME_LENGTH),
                    new Object[]{minUsernameLength, 256});
            throw new ValidatorException(new FacesMessage(err));
        }
        if (name.indexOf('"') != -1 || name.indexOf('\\') != -1)
        {
            String err = MessageFormat.format(Application.getMessage(context, LoginBean.MSG_USER_ERR),
                    new Object[]{"\", \\"});
            throw new ValidatorException(new FacesMessage(err));
        }
    }

    // ------------------------------------------------------------------------------
    // Helper methods

    /**
     * Helper to return the company home space
     * 
     * @return company home space NodeRef
     */
    protected NodeRef getCompanyHomeSpace()
    {
        if (this.companyHomeSpaceRef == null)
        {
            String companyXPath = Application.getRootPath(FacesContext.getCurrentInstance());

            NodeRef rootNodeRef = this.getNodeService().getRootNode(Repository.getStoreRef());
            List<NodeRef> nodes = this.getSearchService().selectNodes(rootNodeRef, companyXPath, null, this.getNamespaceService(), false);

            if (nodes.size() == 0)
            {
                throw new IllegalStateException("Unable to find company home space path: " + companyXPath);
            }

            this.companyHomeSpaceRef = nodes.get(0);
        }

        return this.companyHomeSpaceRef;
    }

    protected NodeRef getDefaultHomeSpace()
    {
        if ((this.defaultHomeSpaceRef == null) || !getNodeService().exists(this.defaultHomeSpaceRef))
        {
            String defaultHomeSpacePath = Application.getClientConfig(FacesContext.getCurrentInstance()).getDefaultHomeSpacePath();

            NodeRef rootNodeRef = this.getNodeService().getRootNode(Repository.getStoreRef());
            List<NodeRef> nodes = this.getSearchService().selectNodes(rootNodeRef, defaultHomeSpacePath, null, this.getNamespaceService(), false);

            if (nodes.size() == 0)
            {
                return getCompanyHomeSpace();
            }

            this.defaultHomeSpaceRef = nodes.get(0);
        }

        return this.defaultHomeSpaceRef;
    }

    /**
     * Create the specified home space if it does not exist, and return the ID
     * 
     * @param locationId Parent location
     * @param spaceName Home space to create, can be null to simply return the parent
     * @param error True to throw an error if the space already exists, else ignore and return
     * @return ID of the home space
     */
    protected NodeRef createHomeSpace(String locationId, String spaceName, boolean error)
    {
        NodeRef homeSpaceNodeRef = null;
        if (spaceName != null && spaceName.length() != 0)
        {
            NodeRef parentRef = new NodeRef(Repository.getStoreRef(), locationId);

            // check for existance of home space with same name - return immediately
            // if it exists or throw an exception an give user chance to enter another name
            NodeRef childRef = this.getNodeService().getChildByName(parentRef, ContentModel.ASSOC_CONTAINS, spaceName);
            if (childRef != null)
            {
                if (error)
                {
                    throw new AlfrescoRuntimeException("A Home Space with the same name already exists.");
                }
                else
                {
                    return childRef;
                }
            }

            // space does not exist already, create a new Space under it with
            // the specified name
            String qname = QName.createValidLocalName(spaceName);
            ChildAssociationRef assocRef = this.getNodeService().createNode(parentRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, qname),
                    ContentModel.TYPE_FOLDER);

            NodeRef nodeRef = assocRef.getChildRef();

            // set the name property on the node
            this.getNodeService().setProperty(nodeRef, ContentModel.PROP_NAME, spaceName);

            if (logger.isDebugEnabled())
                logger.debug("Created Home Space for with name: " + spaceName);

            // apply the uifacets aspect - icon, title and description props
            Map<QName, Serializable> uiFacetsProps = new HashMap<QName, Serializable>(3);
            uiFacetsProps.put(ApplicationModel.PROP_ICON, CreateSpaceWizard.DEFAULT_SPACE_ICON_NAME);
            uiFacetsProps.put(ContentModel.PROP_TITLE, spaceName);
            this.getNodeService().addAspect(nodeRef, ApplicationModel.ASPECT_UIFACETS, uiFacetsProps);

            setupHomeSpacePermissions(nodeRef);

            // return the ID of the created space
            homeSpaceNodeRef = nodeRef;
        }

        return homeSpaceNodeRef;
    }

    /**
     * Setup the default permissions for this and other users on the Home Space
     * 
     * @param homeSpaceRef Home Space reference
     */
    private void setupHomeSpacePermissions(NodeRef homeSpaceRef)
    {
        // Admin Authority has full permissions by default (automatic - set in the permission config)
        // give full permissions to the new user
        getPermissionService().setPermission(homeSpaceRef, this.userName, getPermissionService().getAllPermission(), true);

        // by default other users will only have GUEST access to the space contents
        // or whatever is configured as the default in the web-client-xml config
        String permission = getDefaultPermission();
        if (permission != null && permission.length() != 0)
        {
           getPermissionService().setPermission(homeSpaceRef, getPermissionService().getAllAuthorities(), permission, true);
        }

        // the new user is the OWNER of their own space and always has full permissions
        getOwnableService().setOwner(homeSpaceRef, this.userName);
        getPermissionService().setPermission(homeSpaceRef, getPermissionService().getOwnerAuthority(), getPermissionService().getAllPermission(), true);

        // now detach (if we did this first we could not set any permissions!)
        getPermissionService().setInheritParentPermissions(homeSpaceRef, false);
    }

    /**
     * @return default permission string to set for other users for a new Home Space
     */
    private String getDefaultPermission()
    {
        ClientConfigElement config = Application.getClientConfig(FacesContext.getCurrentInstance());
        return config.getHomeSpacePermission();
    }

    private void invalidateUserList()
    {
        UIContextService.getInstance(FacesContext.getCurrentInstance()).notifyBeans();
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Throwable
    {
        // TODO: implement create new Person object from specified details
        try
        {
            if (! this.password.equals(this.confirm))
            {
                Utils.addErrorMessage(Application.getMessage(context, UsersDialog.ERROR_PASSWORD_MATCH));
                outcome = null;
            }
            
            if (checkTenantUserName() == false)
            {         
                outcome = null;
            }
            
            if (outcome != null)
            {
                // create properties for Person type from submitted Form data
                Map<QName, Serializable> props = new HashMap<QName, Serializable>(7, 1.0f);
                setPersonPropertiesAndCreateHomeSpaceIfNeeded(props, context);

                // create the node to represent the Person
                getPersonService().createPerson(props);

                // ensure the user can access their own Person object
                // getPermissionService().setPermission(newPerson, this.userName, getPermissionService().getAllPermission(), true);
                // Not required - now done by the person service.

                if (logger.isDebugEnabled())
                    logger.debug("Created Person node for username: " + this.userName);

                // create the ACEGI Authentication instance for the new user
                getAuthenticationService().createAuthentication(this.userName, this.password.toCharArray());

                if (logger.isDebugEnabled())
                    logger.debug("Created User Authentication instance for username: " + this.userName);
                
                if ((this.sizeQuota != null) && (this.sizeQuota < 0L))
                {
                    Utils.addErrorMessage(MessageFormat.format(Application.getMessage(context, UsersDialog.ERROR_NEGATIVE_QUOTA), this.sizeQuota));
                    outcome = null;
                }
                else
                {
                	putSizeQuotaProperty(this.userName, this.sizeQuota, this.sizeQuotaUnits);
                }
            }
            invalidateUserList();
        }
        catch (Throwable e)
        {
            Utils.addErrorMessage(MessageFormat.format(Application.getMessage(FacesContext.getCurrentInstance(), ERROR), e.getMessage()), e);
            outcome = null;
            this.isFinished = false;
            ReportedException.throwIfNecessary(e);
        }
        
        if (outcome == null)
        {
            this.isFinished = false; 
        }
        
        return outcome;
    }

    protected void setPersonPropertiesAndCreateHomeSpaceIfNeeded(
            Map<QName, Serializable> props, FacesContext context)
    {
        props.put(ContentModel.PROP_USERNAME, this.userName);
        props.put(ContentModel.PROP_FIRSTNAME, this.firstName);
        props.put(ContentModel.PROP_LASTNAME, this.lastName);
        NodeRef homeSpaceNodeRef;
        if (this.homeSpaceLocation != null && this.homeSpaceName.length() != 0)
        {
            // create new
            props.put(ContentModel.PROP_HOME_FOLDER_PROVIDER, "userHomesHomeFolderProvider");
            homeSpaceNodeRef = createHomeSpace(this.homeSpaceLocation.getId(), this.homeSpaceName, true);
        }
        else if (this.homeSpaceLocation != null)
        {
            // set to existing - first ensure it is NOT "User Homes" space!
            if (this.defaultHomeSpaceRef.equals(this.homeSpaceLocation))
            {
                throw new AlfrescoRuntimeException(Application.getMessage(context, MSG_ERROR_NEWUSER_HOME_SPACE));
            }
            props.put(ContentModel.PROP_HOME_FOLDER_PROVIDER, "companyHomeFolderProvider"); // shared folder
            homeSpaceNodeRef = this.homeSpaceLocation;
            setupHomeSpacePermissions(homeSpaceNodeRef);
        }
        else
        {
            // default to Company Home
            props.put(ContentModel.PROP_HOME_FOLDER_PROVIDER, "companyHomeFolderProvider"); // shared folder
            homeSpaceNodeRef = getCompanyHomeSpace();
        }

        props.put(ContentModel.PROP_HOMEFOLDER, homeSpaceNodeRef);
        props.put(ContentModel.PROP_EMAIL, this.email);
        props.put(ContentModel.PROP_ORGID, this.companyId);
        props.put(ContentModel.PROP_ORGANIZATION, this.organisation);
        props.put(ContentModel.PROP_JOBTITLE, this.jobtitle);
        props.put(ContentModel.PROP_LOCATION, this.location);
        props.put(ContentModel.PROP_PRESENCEPROVIDER, this.presenceProvider);
        props.put(ContentModel.PROP_PRESENCEUSERNAME, this.presenceUsername);
    }

    @Override
    public boolean getFinishButtonDisabled()
    {
        if (this.firstName != null && this.lastName != null && this.email != null && this.firstName.length() > 0 && this.lastName.length() > 0 && this.email.length() > 0)
        {
            return false;
        }
        return true;
    }
    
    protected void putSizeQuotaProperty(String userName, Long quota, String quotaUnits)
    {
       if (quota != null)
       {
          if (quota >= 0L)
          {
             quota = convertToBytes(quota, quotaUnits);
          }
          else
          {
             // ignore negative quota
             return;
          }
       }
       
       getContentUsageService().setUserQuota(userName, (quota == null ? -1 : quota));
    }
    
    protected long convertToBytes(long size, String units)
    {
       if (units != null)
       {
          if (units.equals(QUOTA_UNITS_KB))
          {
             size = size * 1024L;
          }
          else if (units.equals(QUOTA_UNITS_MB))
          {
             size = size * 1048576L;
          }
          else if (units.equals(QUOTA_UNITS_GB))
          {
             size = size * 1073741824L;
          }
       }
       return size;
    }
    
    protected Pair<Long, String> convertFromBytes(long size)
    {
       String units = null;
       if (size <= 0)
       {
          units = QUOTA_UNITS_GB;
       }
       else if (size < 999999)
       {
          size = (long)((double)size / 1024.0d);
          units = QUOTA_UNITS_KB;
       }
       else if (size < 999999999)
       {
          size = (long)((double)size / 1048576.0d);
          units = QUOTA_UNITS_MB;
       }
       else
       {
          size = (long)((double)size / 1073741824.0d);
          units = QUOTA_UNITS_GB;
       }
       return new Pair<Long, String>(size, units);
    }
    
    public boolean checkTenantUserName()
    {
        try
        {
            this.userName = PersonServiceImpl.updateUsernameForTenancy(
                    this.userName, getTenantService()
            );
            return true;
        }
        catch(TenantDomainMismatchException e)
        {
            Utils.addErrorMessage(MessageFormat.format(Application.getMessage(FacesContext.getCurrentInstance(), ERROR_DOMAIN_MISMATCH), e.getTenantA(), e.getTenantB()));
            return false;
        }
    }
    
    public Map getPersonPropertiesImmutability()
    {
        return Collections.EMPTY_MAP;
    }
}
