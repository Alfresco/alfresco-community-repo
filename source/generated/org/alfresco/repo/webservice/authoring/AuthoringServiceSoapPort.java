/**
 * AuthoringServiceSoapPort.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.3 Oct 05, 2005 (05:23:37 EDT) WSDL2Java emitter.
 */

package org.alfresco.repo.webservice.authoring;

public interface AuthoringServiceSoapPort extends java.rmi.Remote {

    /**
     * Checkout a content resource for editing.
     */
    public org.alfresco.repo.webservice.authoring.CheckoutResult checkout(org.alfresco.repo.webservice.types.Predicate items, org.alfresco.repo.webservice.types.ParentReference destination) throws java.rmi.RemoteException, org.alfresco.repo.webservice.authoring.AuthoringFault;

    /**
     * Checkin a content resource.
     */
    public org.alfresco.repo.webservice.authoring.CheckinResult checkin(org.alfresco.repo.webservice.types.Predicate items, org.alfresco.repo.webservice.types.NamedValue[] comments, boolean keepCheckedOut) throws java.rmi.RemoteException, org.alfresco.repo.webservice.authoring.AuthoringFault;

    /**
     * Checkin an external content resource thus replacing the existing
     * working content.
     */
    public org.alfresco.repo.webservice.types.Reference checkinExternal(org.alfresco.repo.webservice.types.Reference items, org.alfresco.repo.webservice.types.NamedValue[] comments, boolean keepCheckedOut, org.alfresco.repo.webservice.types.ContentFormat format, byte[] content) throws java.rmi.RemoteException, org.alfresco.repo.webservice.authoring.AuthoringFault;

    /**
     * Cancels the checkout.
     */
    public org.alfresco.repo.webservice.authoring.CancelCheckoutResult cancelCheckout(org.alfresco.repo.webservice.types.Predicate items) throws java.rmi.RemoteException, org.alfresco.repo.webservice.authoring.AuthoringFault;

    /**
     * Locks a content resource.
     */
    public org.alfresco.repo.webservice.types.Reference[] lock(org.alfresco.repo.webservice.types.Predicate items, boolean lockChildren, org.alfresco.repo.webservice.authoring.LockTypeEnum lockType) throws java.rmi.RemoteException, org.alfresco.repo.webservice.authoring.AuthoringFault;

    /**
     * Unlocks a content resource.
     */
    public org.alfresco.repo.webservice.types.Reference[] unlock(org.alfresco.repo.webservice.types.Predicate items, boolean unlockChildren) throws java.rmi.RemoteException, org.alfresco.repo.webservice.authoring.AuthoringFault;

    /**
     * Gets the lock status of the specified nodes.
     */
    public org.alfresco.repo.webservice.authoring.LockStatus[] getLockStatus(org.alfresco.repo.webservice.types.Predicate items) throws java.rmi.RemoteException, org.alfresco.repo.webservice.authoring.AuthoringFault;

    /**
     * Create a snapshot of the specified node(s) in the version store.
     */
    public org.alfresco.repo.webservice.authoring.VersionResult createVersion(org.alfresco.repo.webservice.types.Predicate items, org.alfresco.repo.webservice.types.NamedValue[] comments, boolean versionChildren) throws java.rmi.RemoteException, org.alfresco.repo.webservice.authoring.AuthoringFault;

    /**
     * Gets the version history for the specfied node.
     */
    public org.alfresco.repo.webservice.types.VersionHistory getVersionHistory(org.alfresco.repo.webservice.types.Reference node) throws java.rmi.RemoteException, org.alfresco.repo.webservice.authoring.AuthoringFault;

    /**
     * Revert a node to the snapshot of the specified version.
     */
    public void revertVersion(org.alfresco.repo.webservice.types.Reference node, java.lang.String versionLabel) throws java.rmi.RemoteException, org.alfresco.repo.webservice.authoring.AuthoringFault;

    /**
     * Delete all snapshot versions of the specified node.
     */
    public org.alfresco.repo.webservice.types.VersionHistory deleteAllVersions(org.alfresco.repo.webservice.types.Reference node) throws java.rmi.RemoteException, org.alfresco.repo.webservice.authoring.AuthoringFault;
}
