/**
 * ActionService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package org.alfresco.example.webservice.action;

public interface ActionService extends javax.xml.rpc.Service {

/**
 * Provides Action and Rule manipulation methods.
 */
    public java.lang.String getActionServiceAddress();

    public org.alfresco.example.webservice.action.ActionServiceSoapPort getActionService() throws javax.xml.rpc.ServiceException;

    public org.alfresco.example.webservice.action.ActionServiceSoapPort getActionService(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
