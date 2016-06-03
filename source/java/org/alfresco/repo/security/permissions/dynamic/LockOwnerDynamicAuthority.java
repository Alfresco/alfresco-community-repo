package org.alfresco.repo.security.permissions.dynamic;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.permissions.DynamicAuthority;
import org.alfresco.repo.security.permissions.PermissionReference;
import org.alfresco.repo.security.permissions.impl.ModelDAO;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.util.PropertyCheck;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

/**
 * LockOwnerDynamicAuthority
 */
public class LockOwnerDynamicAuthority extends AbstractLifecycleBean implements DynamicAuthority
{
    private LockService lockService;
    
    private CheckOutCheckInService checkOutCheckInService;
    
    private ModelDAO modelDAO;
    
    private List<String> requiredFor;
    
    private Set<PermissionReference> whenRequired;
    
    public boolean hasAuthority(final NodeRef nodeRef, final String userName)
    {
        return AuthenticationUtil.runAs(new RunAsWork<Boolean>(){

            public Boolean doWork() throws Exception
            {
                if (lockService.getLockStatus(nodeRef, userName) == LockStatus.LOCK_OWNER)
                {
                    return true;
                }
                NodeRef original = checkOutCheckInService.getCheckedOut(nodeRef);
                if (original != null)
                {
                    return (lockService.getLockStatus(original, userName) == LockStatus.LOCK_OWNER);
                }
                else
                {
                    return false;
                }
            }}, AuthenticationUtil.getSystemUserName());
        
        
        
    }

    public String getAuthority()
    {
        return PermissionService.LOCK_OWNER_AUTHORITY;
    }

    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        ApplicationContext ctx = super.getApplicationContext();
        checkOutCheckInService = (CheckOutCheckInService) ctx.getBean("checkOutCheckInService");
        
        PropertyCheck.mandatory(this, "lockService", lockService);
        PropertyCheck.mandatory(this, "checkOutCheckInService", checkOutCheckInService);
        PropertyCheck.mandatory(this, "modelDAO", modelDAO);

        // Build the permission set
        if(requiredFor != null)
        {
            whenRequired = new HashSet<PermissionReference>();
            for(String permission : requiredFor)
            {
                PermissionReference permissionReference = modelDAO.getPermissionReference(null, permission);
                whenRequired.addAll(modelDAO.getGranteePermissions(permissionReference));
                whenRequired.addAll(modelDAO.getGrantingPermissions(permissionReference));
            }
        }
    }

    /**
     * No-op
     */
    @Override
    protected void onShutdown(ApplicationEvent event)
    {
    }

    /**
     * Set the lock service
     */
    public void setLockService(LockService lockService)
    {
        this.lockService = lockService;
    }
    
    /**
     * Service to get the check-in details.  This is not used for Spring configuration
     * because it requires a permission-wrapped public service that in turn depends on
     * this component.
     */
    public void setCheckOutCheckInService(CheckOutCheckInService checkOutCheckInService)
    {
        this.checkOutCheckInService = checkOutCheckInService;
    }

    /**
     * Set the permissions model dao
     */
    public void setModelDAO(ModelDAO modelDAO)
    {
        this.modelDAO = modelDAO;
    }
    
    /**
     * Set the permissions for which this dynamic authority is required
     */
    public void setRequiredFor(List<String> requiredFor)
    {
        this.requiredFor = requiredFor;
    }
    
    public Set<PermissionReference> requiredFor()
    {
        return whenRequired;
    }
}
