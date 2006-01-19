package org.alfresco.repo.webservice.accesscontrol;

import java.rmi.RemoteException;

import org.alfresco.repo.transaction.TransactionComponent;
import org.alfresco.repo.webservice.AbstractWebService;
import org.alfresco.repo.webservice.types.Predicate;

public class AccessControlService extends AbstractWebService implements AccessControlServiceSoapPort
{
    private TransactionComponent transactionService = null;
    
    public void setTransactionService(TransactionComponent transactionService)
    {
        this.transactionService = transactionService;
    }
    
    public ACL[] getACLs(Predicate predicate, ACE filter) throws RemoteException, AccessControlFault
    {
        // TODO Auto-generated method stub
        return null;
    }

    public ACL[] addACEs(Predicate predicate, ACE[] aces) throws RemoteException, AccessControlFault
    {
        // TODO Auto-generated method stub
        return null;
    }

    public ACL[] removeACEs(Predicate predicate, ACE[] aces) throws RemoteException, AccessControlFault
    {
        // TODO Auto-generated method stub
        return null;
    }

    public GetPermissionsResult[] getPermissions(Predicate predicate) throws RemoteException, AccessControlFault
    {
        // TODO Auto-generated method stub
        return null;
    }

    public GetPermissionsResult[] getClassPermissions(String[] classNames) throws RemoteException, AccessControlFault
    {
        // TODO Auto-generated method stub
        return null;
    }

    public HasPermissionsResult hasPermissions(Predicate predicate, String[] permissions) throws RemoteException, AccessControlFault
    {
        // TODO Auto-generated method stub
        return null;
    }

    public ACL[] setInheritPermission(Predicate predicate, boolean inheritPermission) throws RemoteException, AccessControlFault
    {
        // TODO Auto-generated method stub
        return null;
    }

    public OwnerResult[] getOwners(Predicate predicate) throws RemoteException, AccessControlFault
    {
        // TODO Auto-generated method stub
        return null;
    }

    public OwnerResult[] setOwners(Predicate predicate, String owner) throws RemoteException, AccessControlFault
    {
        // TODO Auto-generated method stub
        return null;
    }

}
