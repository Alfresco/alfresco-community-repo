/**
 * AccessControlServiceSoapPort.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.alfresco.repo.webservice.accesscontrol;

public interface AccessControlServiceSoapPort extends java.rmi.Remote {

    /**
     * Get the list of the ACL's for a reference.
     */
    public org.alfresco.repo.webservice.accesscontrol.ACL[] getACLs(org.alfresco.repo.webservice.types.Predicate predicate, org.alfresco.repo.webservice.accesscontrol.ACE filter) throws java.rmi.RemoteException, org.alfresco.repo.webservice.accesscontrol.AccessControlFault;

    /**
     * Adds a number of ACE's to a collection of references.
     */
    public org.alfresco.repo.webservice.accesscontrol.ACL[] addACEs(org.alfresco.repo.webservice.types.Predicate predicate, org.alfresco.repo.webservice.accesscontrol.ACE[] aces) throws java.rmi.RemoteException, org.alfresco.repo.webservice.accesscontrol.AccessControlFault;

    /**
     * Removes ACE's from a collection of references.
     */
    public org.alfresco.repo.webservice.accesscontrol.ACL[] removeACEs(org.alfresco.repo.webservice.types.Predicate predicate, org.alfresco.repo.webservice.accesscontrol.ACE[] aces) throws java.rmi.RemoteException, org.alfresco.repo.webservice.accesscontrol.AccessControlFault;

    /**
     * Gets a list of the settable permissions for a set of references.
     */
    public org.alfresco.repo.webservice.accesscontrol.GetPermissionsResult[] getPermissions(org.alfresco.repo.webservice.types.Predicate predicate) throws java.rmi.RemoteException, org.alfresco.repo.webservice.accesscontrol.AccessControlFault;

    /**
     * Gets a list of the settable permissions for a list of classes.
     */
    public org.alfresco.repo.webservice.accesscontrol.GetClassPermissionsResult[] getClassPermissions(java.lang.String[] classNames) throws java.rmi.RemoteException, org.alfresco.repo.webservice.accesscontrol.AccessControlFault;

    /**
     * Determines whether the current authenticated user has the permissions
     * on each of the specified references.
     */
    public org.alfresco.repo.webservice.accesscontrol.HasPermissionsResult[] hasPermissions(org.alfresco.repo.webservice.types.Predicate predicate, java.lang.String[] permissions) throws java.rmi.RemoteException, org.alfresco.repo.webservice.accesscontrol.AccessControlFault;

    /**
     * Set the inherit permission property of a set of references.
     */
    public org.alfresco.repo.webservice.accesscontrol.ACL[] setInheritPermission(org.alfresco.repo.webservice.types.Predicate predicate, boolean inheritPermission) throws java.rmi.RemoteException, org.alfresco.repo.webservice.accesscontrol.AccessControlFault;

    /**
     * Get the current node owner.
     */
    public org.alfresco.repo.webservice.accesscontrol.OwnerResult[] getOwners(org.alfresco.repo.webservice.types.Predicate predicate) throws java.rmi.RemoteException, org.alfresco.repo.webservice.accesscontrol.AccessControlFault;

    /**
     * Sets the current node owner.
     */
    public org.alfresco.repo.webservice.accesscontrol.OwnerResult[] setOwners(org.alfresco.repo.webservice.types.Predicate predicate, java.lang.String owner) throws java.rmi.RemoteException, org.alfresco.repo.webservice.accesscontrol.AccessControlFault;
}
