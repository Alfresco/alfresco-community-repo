/**
 * RepositoryServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package org.alfresco.example.webservice.repository;

public class RepositoryServiceLocator extends org.apache.axis.client.Service implements org.alfresco.example.webservice.repository.RepositoryService {

/**
 * Provides read and write operations against a repository.
 */

    public RepositoryServiceLocator() {
    }


    public RepositoryServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public RepositoryServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for RepositoryService
    private java.lang.String RepositoryService_address = "http://localhost:8080/alfresco/api/RepositoryService";

    public java.lang.String getRepositoryServiceAddress() {
        return RepositoryService_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String RepositoryServiceWSDDServiceName = "RepositoryService";

    public java.lang.String getRepositoryServiceWSDDServiceName() {
        return RepositoryServiceWSDDServiceName;
    }

    public void setRepositoryServiceWSDDServiceName(java.lang.String name) {
        RepositoryServiceWSDDServiceName = name;
    }

    public org.alfresco.example.webservice.repository.RepositoryServiceSoapPort getRepositoryService() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(RepositoryService_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getRepositoryService(endpoint);
    }

    public org.alfresco.example.webservice.repository.RepositoryServiceSoapPort getRepositoryService(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            org.alfresco.example.webservice.repository.RepositoryServiceSoapBindingStub _stub = new org.alfresco.example.webservice.repository.RepositoryServiceSoapBindingStub(portAddress, this);
            _stub.setPortName(getRepositoryServiceWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setRepositoryServiceEndpointAddress(java.lang.String address) {
        RepositoryService_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (org.alfresco.example.webservice.repository.RepositoryServiceSoapPort.class.isAssignableFrom(serviceEndpointInterface)) {
                org.alfresco.example.webservice.repository.RepositoryServiceSoapBindingStub _stub = new org.alfresco.example.webservice.repository.RepositoryServiceSoapBindingStub(new java.net.URL(RepositoryService_address), this);
                _stub.setPortName(getRepositoryServiceWSDDServiceName());
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
        if ("RepositoryService".equals(inputPortName)) {
            return getRepositoryService();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/repository/1.0", "RepositoryService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/repository/1.0", "RepositoryService"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("RepositoryService".equals(portName)) {
            setRepositoryServiceEndpointAddress(address);
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
