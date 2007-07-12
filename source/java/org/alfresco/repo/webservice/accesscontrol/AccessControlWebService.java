package org.alfresco.repo.webservice.accesscontrol;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.alfresco.repo.transaction.TransactionServiceImpl;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.webservice.AbstractWebService;
import org.alfresco.repo.webservice.Utils;
import org.alfresco.repo.webservice.action.ActionFault;
import org.alfresco.repo.webservice.types.Predicate;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AccessControlWebService extends AbstractWebService implements AccessControlServiceSoapPort
{
    /** Log */
    private static Log logger = LogFactory.getLog(AccessControlWebService.class);
    
    /** Transaction service */
    private TransactionServiceImpl transactionService = null;
    
    /** Permission service */
    private PermissionService permissionService = null;
    
    /** Ownable service */
    private OwnableService ownableService = null;
    
    /**
     * Set the transaction service
     * 
     * @param transactionService    the transaction service
     */
    public void setTransactionService(TransactionServiceImpl transactionService)
    {
        this.transactionService = transactionService;
    }
    
    /**
     * Set the permissions service
     * 
     * @param permissionService     the permissions service
     */
    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }
    
    /**
     * Set the ownable service
     * 
     * @param ownableService    the ownable service
     */
    public void setOwnableService(OwnableService ownableService)
    {
        this.ownableService = ownableService;
    }
    
    /**
     * @see org.alfresco.repo.webservice.accesscontrol.AccessControlServiceSoapPort#getACLs(org.alfresco.repo.webservice.types.Predicate, org.alfresco.repo.webservice.accesscontrol.ACE)
     */
    public ACL[] getACLs(final Predicate predicate, final ACE filter) throws RemoteException, AccessControlFault
    {
        try
        {
            RetryingTransactionCallback<ACL[]> callback = new RetryingTransactionCallback<ACL[]>()
            {
                public ACL[] execute() throws Exception
                {
                    return getACLsImpl(predicate, filter);
                }
            };
            return transactionService.getRetryingTransactionHelper().doInTransaction(callback);
        }
        catch (Throwable exception)
        {
            if (logger.isDebugEnabled())
            {
                logger.error("Unexpected error occurred", exception);
            }
            
            throw new ActionFault(0, exception.getMessage());
        }
    }

    /**
     * Get the ACL's for the predicate, filtered if appropraite.
     * 
     * @param predicate     the predicate
     * @param filter        the fileter (optional)
     * @return              an array of ACL's
     */
    private ACL[] getACLsImpl(Predicate predicate, ACE filter)
    {
        // Resolve the nodes
        List<NodeRef> nodes = Utils.resolvePredicate(predicate, this.nodeService, this.searchService, this.namespaceService);
        ACL[] acls = new ACL[nodes.size()];
        
        int index = 0;
        for (NodeRef node : nodes)
        {
            // Create ACL of node
            ACL acl = getACLFromNodeRef(node, filter);            
            
            // Add the acl to the results
            acls[index] = acl;
            index++;
        }
        
        return acls;
    }

    /**
     * Given a node reference, creates the relating ACL
     * 
     * @param node  the node reference
     * @return      the ACL
     */
    private ACL getACLFromNodeRef(NodeRef node, ACE filter)
    {
        // Create the acl
        ACL acl = new ACL();
        acl.setReference(Utils.convertToReference(this.nodeService, this.namespaceService, node));
        
        // Set the inhertied value
        boolean inheritPermission = this.permissionService.getInheritParentPermissions(node);
        acl.setInheritPermissions(inheritPermission);
        
        // Get the access permissions
        Set<AccessPermission> accessPermissions = this.permissionService.getAllSetPermissions(node);
        ACE[] aces = new ACE[accessPermissions.size()];
        
        // Marshal the permissions into ACE's
        int count = 0;
        for (AccessPermission permission : accessPermissions)
        {
            // TODO need to filter the results accordingly using ACE filter            
            
            // Create the ace
            org.alfresco.repo.webservice.accesscontrol.AccessStatus accessStatus = org.alfresco.repo.webservice.accesscontrol.AccessStatus.declined;
            if (AccessStatus.ALLOWED.equals(permission.getAccessStatus()) == true)
            {
                accessStatus = org.alfresco.repo.webservice.accesscontrol.AccessStatus.acepted;
            }
            ACE ace = new ACE(permission.getAuthority(),permission.getPermission(), accessStatus);
            
            // Add ace to array
            aces[count] = ace;
            count ++;
        }
        acl.setAces(aces);
        return acl;
    }

    /**
     * @see org.alfresco.repo.webservice.accesscontrol.AccessControlServiceSoapPort#addACEs(org.alfresco.repo.webservice.types.Predicate, org.alfresco.repo.webservice.accesscontrol.ACE[])
     */
    public ACL[] addACEs(final Predicate predicate, final ACE[] aces) throws RemoteException, AccessControlFault
    {
        try
        {
            RetryingTransactionCallback<ACL[]> callback = new RetryingTransactionCallback<ACL[]>()
            {
                public ACL[] execute() throws Exception
                {
                    return addACEsImpl(predicate, aces);
                }
            };
            return transactionService.getRetryingTransactionHelper().doInTransaction(callback);
        }
        catch (Throwable exception)
        {
            if (logger.isDebugEnabled())
            {
                logger.error("Unexpected error occurred", exception);
            }
            
            throw new ActionFault(0, exception.getMessage());
        }
    }

    /**
     * Add ACE to a collection of nodes
     * 
     * @param predicate      the predicate
     * @param aces           the ACE's to add
     * @return               the ACL's of the modified node
     */
    private ACL[] addACEsImpl(Predicate predicate, ACE[] aces)
    {
        // Resolce the predicate
        List<NodeRef> nodes = Utils.resolvePredicate(predicate, this.nodeService, this.searchService, this.namespaceService);
        ACL[] acls = new ACL[nodes.size()];
        
        int count = 0;
        for (NodeRef node : nodes)
        {
            // Add the permissions for each ace
            for (ACE ace : aces)
            {
                // Add the permissions associated with the ace
                boolean allow = false;
                if (ace.getAccessStatus().equals(org.alfresco.repo.webservice.accesscontrol.AccessStatus.acepted) == true)
                {
                    allow = true;
                }
                this.permissionService.setPermission(node, ace.getAuthority(), ace.getPermission(), allow);
            }
            
            // Add the ACL forthis node to the returned array
            acls[count] = getACLFromNodeRef(node, null);
            count++;
        }
        
        return acls;
    }

    /**
     * @see org.alfresco.repo.webservice.accesscontrol.AccessControlServiceSoapPort#removeACEs(org.alfresco.repo.webservice.types.Predicate, org.alfresco.repo.webservice.accesscontrol.ACE[])
     */
    public ACL[] removeACEs(final Predicate predicate, final ACE[] aces) throws RemoteException, AccessControlFault
    {
        try
        {
            RetryingTransactionCallback<ACL[]> callback = new RetryingTransactionCallback<ACL[]>()
            {
                public ACL[] execute() throws Exception
                {
                    return removeACEsImpl(predicate, aces);
                }
            };
            return transactionService.getRetryingTransactionHelper().doInTransaction(callback);
        }
        catch (Throwable exception)
        {
            if (logger.isDebugEnabled())
            {
                logger.error("Unexpected error occurred", exception);
            }
            
            throw new ActionFault(0, exception.getMessage());
        }
    }

    /**
     * Remove specified ACE's from the nodes.  Removes all permissions if no ACE's specified.
     * 
     * @param predicate     the predicate
     * @param aces          the ACE's to remove
     * @return              the modified ACL's
     */
    private ACL[] removeACEsImpl(Predicate predicate, ACE[] aces)
    {
        // Resolce the predicate
        List<NodeRef> nodes = Utils.resolvePredicate(predicate, this.nodeService, this.searchService, this.namespaceService);
        ACL[] acls = new ACL[nodes.size()];
        
        int count = 0;
        for (NodeRef node : nodes)
        {
            if (aces == null)
            {
                // Delete all the permissions
                this.permissionService.deletePermissions(node);
            }
            else
            {
                // Delete the permissions for each ACE
                for (ACE ace : aces)
                {
                    this.permissionService.deletePermission(node, ace.getAuthority(), ace.getPermission());
                }
            }
            
            // Add the ACL forthis node to the returned array
            acls[count] = getACLFromNodeRef(node, null);
            count++;
        }
        
        return acls;
    }

    /**
     * @see org.alfresco.repo.webservice.accesscontrol.AccessControlServiceSoapPort#getPermissions(org.alfresco.repo.webservice.types.Predicate)
     */
    public GetPermissionsResult[] getPermissions(final Predicate predicate) throws RemoteException, AccessControlFault
    {
        try
        {
            RetryingTransactionCallback<GetPermissionsResult[]> callback = new RetryingTransactionCallback<GetPermissionsResult[]>()
            {
                public GetPermissionsResult[] execute() throws Exception
                {
                    return getPermissionsImpl(predicate);
                }
            };
            return transactionService.getRetryingTransactionHelper().doInTransaction(callback);
        }
        catch (Throwable exception)
        {
            if (logger.isDebugEnabled())
            {
                logger.error("Unexpected error occurred", exception);
            }
            
            throw new ActionFault(0, exception.getMessage());
        }
    }

    /**
     * Get the permissions
     * 
     * @param predicate     the predicate
     * @return              the permissions available
     */
    private GetPermissionsResult[] getPermissionsImpl(Predicate predicate)
    {
        // Resolve the predicate
        List<NodeRef> nodes = Utils.resolvePredicate(predicate, this.nodeService, this.searchService, this.namespaceService);
        GetPermissionsResult[] results = new GetPermissionsResult[nodes.size()];
        
        int count = 0;
        for (NodeRef node : nodes)
        {
            // Get the permissions
            Set<String> permissions = this.permissionService.getSettablePermissions(node);
            
            // Create the permissions result object
            GetPermissionsResult result = new GetPermissionsResult();
            result.setReference(Utils.convertToReference(this.nodeService, this.namespaceService, node));
            result.setPermissions((String[])permissions.toArray(new String[permissions.size()]));
            
            // Add result to array
            results[count] = result;
            count ++;
        }
        
        return results;
    }

    /**
     * @see org.alfresco.repo.webservice.accesscontrol.AccessControlServiceSoapPort#getClassPermissions(java.lang.String[])
     */
    public GetClassPermissionsResult[] getClassPermissions(final String[] classNames) throws RemoteException, AccessControlFault
    {
        try
        {
            RetryingTransactionCallback<GetClassPermissionsResult[]> callback = new RetryingTransactionCallback<GetClassPermissionsResult[]>()
            {
                public GetClassPermissionsResult[] execute() throws Exception
                {
                    return getClassPermissionsImpl(classNames);
                }
            };
            return transactionService.getRetryingTransactionHelper().doInTransaction(callback);
        }
        catch (Throwable exception)
        {
            if (logger.isDebugEnabled())
            {
                logger.error("Unexpected error occurred", exception);
            }
            
            throw new ActionFault(0, exception.getMessage());
        }
    }

    /**
     * Get the permissions based on type
     * 
     * @param classNames    the class names
     * @return              the permission results
     */
    private GetClassPermissionsResult[] getClassPermissionsImpl(String[] classNames)
    {
        // Resolve the predicate
        GetClassPermissionsResult[] results = new GetClassPermissionsResult[classNames.length];
        
        int count = 0;
        for (String className : classNames)
        {
            // Get the permissions
            Set<String> permissions = this.permissionService.getSettablePermissions(QName.createQName(className));
            
            // Create the permissions result object
            GetClassPermissionsResult result = new GetClassPermissionsResult();
            result.setClassName(className);
            result.setPermissions((String[])permissions.toArray(new String[permissions.size()]));
            
            // Add result to array
            results[count] = result;
            count ++;
        }
        
        return results;
    }

    /**
     * @see org.alfresco.repo.webservice.accesscontrol.AccessControlServiceSoapPort#hasPermissions(org.alfresco.repo.webservice.types.Predicate, java.lang.String[])
     */
    public HasPermissionsResult[] hasPermissions(final Predicate predicate, final String[] permissions) throws RemoteException, AccessControlFault
    {
        try
        {
            RetryingTransactionCallback<HasPermissionsResult[]> callback = new RetryingTransactionCallback<HasPermissionsResult[]>()
            {
                public HasPermissionsResult[] execute() throws Exception
                {
                    return hasPermissionsImpl(predicate, permissions);
                }
            };
            return transactionService.getRetryingTransactionHelper().doInTransaction(callback);
        }
        catch (Throwable exception)
        {
            if (logger.isDebugEnabled())
            {
                logger.error("Unexpected error occurred", exception);
            }
            
            throw new ActionFault(0, exception.getMessage());
        }
    }

    /**
     * Determines whether a set of node has a given set of permissions.
     * 
     * @param predicate     the predicate
     * @param permissions   the permissions
     * @return              the permissions result
     */
    private HasPermissionsResult[] hasPermissionsImpl(Predicate predicate, String[] permissions)
    {
        // Resolve the predicate
        List<NodeRef> nodes = Utils.resolvePredicate(predicate, this.nodeService, this.searchService, this.namespaceService);
        List<HasPermissionsResult> results = new ArrayList<HasPermissionsResult>(20);
        
        for (NodeRef node : nodes)
        {
            for (String permission : permissions)
            {
                // Detemine whether the node has the permissions
                AccessStatus accessStatus = this.permissionService.hasPermission(node, permission);
                org.alfresco.repo.webservice.accesscontrol.AccessStatus accessState = org.alfresco.repo.webservice.accesscontrol.AccessStatus.declined;
                if (AccessStatus.ALLOWED.equals(accessStatus) == true)
                {
                    accessState = org.alfresco.repo.webservice.accesscontrol.AccessStatus.acepted;
                }
                
                // Add to the results list
                results.add(new HasPermissionsResult(Utils.convertToReference(this.nodeService, this.namespaceService, node), permission, accessState));
            }            
        }
        
        return (HasPermissionsResult[])results.toArray(new HasPermissionsResult[results.size()]);
    }

    /**
     * @see org.alfresco.repo.webservice.accesscontrol.AccessControlServiceSoapPort#setInheritPermission(org.alfresco.repo.webservice.types.Predicate, boolean)
     */
    public ACL[] setInheritPermission(final Predicate predicate, final boolean inheritPermission) throws RemoteException, AccessControlFault
    {
        try
        {
            RetryingTransactionCallback<ACL[]> callback = new RetryingTransactionCallback<ACL[]>()
            {
                public ACL[] execute() throws Exception
                {
                    return setInheritPermissionImpl(predicate, inheritPermission);
                }
            };
            return transactionService.getRetryingTransactionHelper().doInTransaction(callback);
        }
        catch (Throwable exception)
        {
            if (logger.isDebugEnabled())
            {
                logger.error("Unexpected error occurred", exception);
            }
            
            throw new ActionFault(0, exception.getMessage());
        }
    }

    /**
     * Set the inherit permissions flag
     * 
     * @param predicate             the predicate
     * @param inheritPermission     indicates whether the permissions are inherited or not
     * @return                      the updated acl's
     */
    private ACL[] setInheritPermissionImpl(Predicate predicate, boolean inheritPermission)
    {
        // Resolve the predicate
        List<NodeRef> nodes = Utils.resolvePredicate(predicate, this.nodeService, this.searchService, this.namespaceService);
        ACL[] acls = new ACL[nodes.size()];
        
        int count = 0;
        for (NodeRef node : nodes)
        {
            // Set the inherited permission value
            this.permissionService.setInheritParentPermissions(node, inheritPermission);
            
            // Add the ACL of the modified node to the result
            acls[count] = getACLFromNodeRef(node, null);
            count ++;
        }
        
        return acls;  
    }

    /**
     * @see org.alfresco.repo.webservice.accesscontrol.AccessControlServiceSoapPort#getOwners(org.alfresco.repo.webservice.types.Predicate)
     */
    public OwnerResult[] getOwners(final Predicate predicate) throws RemoteException, AccessControlFault
    {
        try
        {
            RetryingTransactionCallback<OwnerResult[]> callback = new RetryingTransactionCallback<OwnerResult[]>()
            {
                public OwnerResult[] execute() throws Exception
                {
                    return getOwnersImpl(predicate);
                }
            };
            return transactionService.getRetryingTransactionHelper().doInTransaction(callback);
        }
        catch (Throwable exception)
        {
            if (logger.isDebugEnabled())
            {
                logger.error("Unexpected error occurred", exception);
            }
            
            throw new ActionFault(0, exception.getMessage());
        }
    }

    /**
     * Gets the owners of the nodes
     * 
     * @param predicate     the predicate
     * @return              the owner details
     */
    private OwnerResult[] getOwnersImpl(Predicate predicate)
    {
        // Convert predicate
        List<NodeRef> nodes = Utils.resolvePredicate(predicate, this.nodeService, this.searchService, this.namespaceService);
        OwnerResult[] result = new OwnerResult[nodes.size()];
        
        int count = 0;
        for (NodeRef node : nodes)
        {
            // Get the current owner of the node
            String owner = this.ownableService.getOwner(node);
            
            // Marshal into result            
            result[count] = new OwnerResult(Utils.convertToReference(this.nodeService, this.namespaceService, node), owner);
            count ++;
        }
        
        return result;
    }

    /**
     * @see org.alfresco.repo.webservice.accesscontrol.AccessControlServiceSoapPort#setOwners(org.alfresco.repo.webservice.types.Predicate, java.lang.String)
     */
    public OwnerResult[] setOwners(final Predicate predicate, final String owner) throws RemoteException, AccessControlFault
    {
        try
        {
            RetryingTransactionCallback<OwnerResult[]> callback = new RetryingTransactionCallback<OwnerResult[]>()
            {
                public OwnerResult[] execute() throws Exception
                {
                    return setOwnersImpl(predicate, owner);
                }
            };
            return transactionService.getRetryingTransactionHelper().doInTransaction(callback);
        }
        catch (Throwable exception)
        {
            if (logger.isDebugEnabled())
            {
                logger.error("Unexpected error occurred", exception);
            }
            
            throw new ActionFault(0, exception.getMessage());
        }
    }

    /**
     * Set the owner of a nodes
     * 
     * @param predicate     the predicate
     * @param owner         the owner
     * @return              the owner results updated
     */
    private OwnerResult[] setOwnersImpl(Predicate predicate, String owner)
    {
        // Convert predicate
        List<NodeRef> nodes = Utils.resolvePredicate(predicate, this.nodeService, this.searchService, this.namespaceService);
        OwnerResult[] result = new OwnerResult[nodes.size()];
        
        int count = 0;
        for (NodeRef node : nodes)
        {
            // Set the owner of the node
            this.ownableService.setOwner(node, owner);
            
            // Marshal into result            
            result[count] = new OwnerResult(Utils.convertToReference(this.nodeService, this.namespaceService, node), owner);
            count ++;
        }
        
        return result;
    }
}
