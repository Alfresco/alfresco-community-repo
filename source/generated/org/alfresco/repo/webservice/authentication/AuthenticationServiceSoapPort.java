/**
 * AuthenticationServiceSoapPort.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.3 Oct 05, 2005 (05:23:37 EDT) WSDL2Java emitter.
 */

package org.alfresco.repo.webservice.authentication;

public interface AuthenticationServiceSoapPort extends java.rmi.Remote {
    public org.alfresco.repo.webservice.authentication.AuthenticationResult startSession(java.lang.String username, java.lang.String password) throws java.rmi.RemoteException, org.alfresco.repo.webservice.authentication.AuthenticationFault;
    public void endSession(java.lang.String ticket) throws java.rmi.RemoteException, org.alfresco.repo.webservice.authentication.AuthenticationFault;
}
