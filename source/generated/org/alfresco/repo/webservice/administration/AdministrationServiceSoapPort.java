/**
 * AdministrationServiceSoapPort.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package org.alfresco.repo.webservice.administration;

public interface AdministrationServiceSoapPort extends java.rmi.Remote {

    /**
     * Gets the details of the requested users.
     */
    public org.alfresco.repo.webservice.administration.UserQueryResults queryUsers(org.alfresco.repo.webservice.administration.UserFilter filter) throws java.rmi.RemoteException, org.alfresco.repo.webservice.administration.AdministrationFault;

    /**
     * Fetch the next bathc of users from an existing user query.
     */
    public org.alfresco.repo.webservice.administration.UserQueryResults fetchMoreUsers(java.lang.String querySession) throws java.rmi.RemoteException, org.alfresco.repo.webservice.administration.AdministrationFault;

    /**
     * Get the details of a specified user.
     */
    public org.alfresco.repo.webservice.administration.UserDetails getUser(java.lang.String userName) throws java.rmi.RemoteException, org.alfresco.repo.webservice.administration.AdministrationFault;

    /**
     * Create new users with the details provided.
     */
    public org.alfresco.repo.webservice.administration.UserDetails[] createUsers(org.alfresco.repo.webservice.administration.NewUserDetails[] newUsers) throws java.rmi.RemoteException, org.alfresco.repo.webservice.administration.AdministrationFault;

    /**
     * Updates the details of the specified users.
     */
    public org.alfresco.repo.webservice.administration.UserDetails[] updateUsers(org.alfresco.repo.webservice.administration.UserDetails[] users) throws java.rmi.RemoteException, org.alfresco.repo.webservice.administration.AdministrationFault;

    /**
     * Changes the password of the specified user.
     */
    public void changePassword(java.lang.String userName, java.lang.String oldPassword, java.lang.String newPassword) throws java.rmi.RemoteException, org.alfresco.repo.webservice.administration.AdministrationFault;

    /**
     * Delete the specified users.
     */
    public void deleteUsers(java.lang.String[] userNames) throws java.rmi.RemoteException, org.alfresco.repo.webservice.administration.AdministrationFault;
}
