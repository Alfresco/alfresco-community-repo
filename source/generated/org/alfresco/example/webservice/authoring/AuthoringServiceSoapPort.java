/**
 * AuthoringServiceSoapPort.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package org.alfresco.example.webservice.authoring;

public interface AuthoringServiceSoapPort extends java.rmi.Remote {

    /**
     * Checkout a content resource for editing.
     */
    public org.alfresco.example.webservice.authoring.CheckoutResult checkout(org.alfresco.example.webservice.types.Predicate items, org.alfresco.example.webservice.types.ParentReference destination) throws java.rmi.RemoteException, org.alfresco.example.webservice.authoring.AuthoringFault;

    /**
     * Checkin a content resource.
     */
    public org.alfresco.example.webservice.authoring.CheckinResult checkin(org.alfresco.example.webservice.types.Predicate items, org.alfresco.example.webservice.types.NamedValue[] comments, boolean keepCheckedOut) throws java.rmi.RemoteException, org.alfresco.example.webservice.authoring.AuthoringFault;

    /**
     * Checkin an external content resource thus replacing the existing
     * working content.
     */
    public org.alfresco.example.webservice.types.Reference checkinExternal(org.alfresco.example.webservice.types.Reference items, org.alfresco.example.webservice.types.NamedValue[] comments, boolean keepCheckedOut, org.alfresco.example.webservice.types.ContentFormat format, byte[] content) throws java.rmi.RemoteException, org.alfresco.example.webservice.authoring.AuthoringFault;

    /**
     * Cancels the checkout.
     */
    public org.alfresco.example.webservice.authoring.CancelCheckoutResult cancelCheckout(org.alfresco.example.webservice.types.Predicate items) throws java.rmi.RemoteException, org.alfresco.example.webservice.authoring.AuthoringFault;

    /**
     * Locks a content resource.
     */
    public org.alfresco.example.webservice.types.Reference[] lock(org.alfresco.example.webservice.types.Predicate items, boolean lockChildren, org.alfresco.example.webservice.authoring.LockTypeEnum lockType) throws java.rmi.RemoteException, org.alfresco.example.webservice.authoring.AuthoringFault;

    /**
     * Unlocks a content resource.
     */
    public org.alfresco.example.webservice.types.Reference[] unlock(org.alfresco.example.webservice.types.Predicate items, boolean unlockChildren) throws java.rmi.RemoteException, org.alfresco.example.webservice.authoring.AuthoringFault;

    /**
     * Gets the lock status of the specified nodes.
     */
    public org.alfresco.example.webservice.authoring.LockStatus[] getLockStatus(org.alfresco.example.webservice.types.Predicate items) throws java.rmi.RemoteException, org.alfresco.example.webservice.authoring.AuthoringFault;

    /**
     * Create a snapshot of the specified node(s) in the version store.
     */
    public org.alfresco.example.webservice.authoring.VersionResult createVersion(org.alfresco.example.webservice.types.Predicate items, org.alfresco.example.webservice.types.NamedValue[] comments, boolean versionChildren) throws java.rmi.RemoteException, org.alfresco.example.webservice.authoring.AuthoringFault;

    /**
     * Gets the version history for the specfied node.
     */
    public org.alfresco.example.webservice.types.VersionHistory getVersionHistory(org.alfresco.example.webservice.types.Reference node) throws java.rmi.RemoteException, org.alfresco.example.webservice.authoring.AuthoringFault;

    /**
     * Revert a node to the snapshot of the specified version.
     */
    public void revertVersion(org.alfresco.example.webservice.types.Reference node, java.lang.String versionLabel) throws java.rmi.RemoteException, org.alfresco.example.webservice.authoring.AuthoringFault;

    /**
     * Delete all snapshot versions of the specified node.
     */
    public org.alfresco.example.webservice.types.VersionHistory deleteAllVersions(org.alfresco.example.webservice.types.Reference node) throws java.rmi.RemoteException, org.alfresco.example.webservice.authoring.AuthoringFault;
}
