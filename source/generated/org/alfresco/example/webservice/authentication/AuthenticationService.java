/**
 * AuthenticationService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package org.alfresco.example.webservice.authentication;

public interface AuthenticationService extends javax.xml.rpc.Service {

/**
 * Provides simple authentication capability.
 */
    public java.lang.String getAuthenticationServiceAddress();

    public org.alfresco.example.webservice.authentication.AuthenticationServiceSoapPort getAuthenticationService() throws javax.xml.rpc.ServiceException;

    public org.alfresco.example.webservice.authentication.AuthenticationServiceSoapPort getAuthenticationService(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
