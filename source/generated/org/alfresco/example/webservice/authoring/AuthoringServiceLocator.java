/**
 * AuthoringServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package org.alfresco.example.webservice.authoring;

public class AuthoringServiceLocator extends org.apache.axis.client.Service implements org.alfresco.example.webservice.authoring.AuthoringService {

/**
 * Provides support for collaborative editing of content.
 */

    public AuthoringServiceLocator() {
    }


    public AuthoringServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public AuthoringServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for AuthoringService
    private java.lang.String AuthoringService_address = "http://localhost:8080/alfresco/api/AuthoringService";

    public java.lang.String getAuthoringServiceAddress() {
        return AuthoringService_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String AuthoringServiceWSDDServiceName = "AuthoringService";

    public java.lang.String getAuthoringServiceWSDDServiceName() {
        return AuthoringServiceWSDDServiceName;
    }

    public void setAuthoringServiceWSDDServiceName(java.lang.String name) {
        AuthoringServiceWSDDServiceName = name;
    }

    public org.alfresco.example.webservice.authoring.AuthoringServiceSoapPort getAuthoringService() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(AuthoringService_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getAuthoringService(endpoint);
    }

    public org.alfresco.example.webservice.authoring.AuthoringServiceSoapPort getAuthoringService(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            org.alfresco.example.webservice.authoring.AuthoringServiceSoapBindingStub _stub = new org.alfresco.example.webservice.authoring.AuthoringServiceSoapBindingStub(portAddress, this);
            _stub.setPortName(getAuthoringServiceWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setAuthoringServiceEndpointAddress(java.lang.String address) {
        AuthoringService_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (org.alfresco.example.webservice.authoring.AuthoringServiceSoapPort.class.isAssignableFrom(serviceEndpointInterface)) {
                org.alfresco.example.webservice.authoring.AuthoringServiceSoapBindingStub _stub = new org.alfresco.example.webservice.authoring.AuthoringServiceSoapBindingStub(new java.net.URL(AuthoringService_address), this);
                _stub.setPortName(getAuthoringServiceWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("AuthoringService".equals(inputPortName)) {
            return getAuthoringService();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "AuthoringService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "AuthoringService"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("AuthoringService".equals(portName)) {
            setAuthoringServiceEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
