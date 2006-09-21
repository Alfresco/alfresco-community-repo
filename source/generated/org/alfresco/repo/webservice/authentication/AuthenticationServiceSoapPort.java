/**
 * AuthenticationServiceSoapPort.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.alfresco.repo.webservice.authentication;

public interface AuthenticationServiceSoapPort extends java.rmi.Remote {
    public org.alfresco.repo.webservice.authentication.AuthenticationResult startSession(java.lang.String username, java.lang.String password) throws java.rmi.RemoteException, org.alfresco.repo.webservice.authentication.AuthenticationFault;
    public void endSession(java.lang.String ticket) throws java.rmi.RemoteException, org.alfresco.repo.webservice.authentication.AuthenticationFault;
}
