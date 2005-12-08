/**
 * AuthoringServiceSoapBindingStub.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package org.alfresco.example.webservice.authoring;

public class AuthoringServiceSoapBindingStub extends org.apache.axis.client.Stub implements org.alfresco.example.webservice.authoring.AuthoringServiceSoapPort {
    private java.util.Vector cachedSerClasses = new java.util.Vector();
    private java.util.Vector cachedSerQNames = new java.util.Vector();
    private java.util.Vector cachedSerFactories = new java.util.Vector();
    private java.util.Vector cachedDeserFactories = new java.util.Vector();

    static org.apache.axis.description.OperationDesc [] _operations;

    static {
        _operations = new org.apache.axis.description.OperationDesc[11];
        _initOperationDesc1();
        _initOperationDesc2();
    }

    private static void _initOperationDesc1(){
        org.apache.axis.description.OperationDesc oper;
        org.apache.axis.description.ParameterDesc param;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("checkout");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "items"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "Predicate"), org.alfresco.example.webservice.types.Predicate.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "destination"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "ParentReference"), org.alfresco.example.webservice.types.ParentReference.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "CheckoutResult"));
        oper.setReturnClass(org.alfresco.example.webservice.authoring.CheckoutResult.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "checkoutReturn"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "AuthoringFault"),
                      "org.alfresco.example.webservice.authoring.AuthoringFault",
                      new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "AuthoringFault"), 
                      true
                     ));
        _operations[0] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("checkin");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "items"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "Predicate"), org.alfresco.example.webservice.types.Predicate.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "comments"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "NamedValue"), org.alfresco.example.webservice.types.NamedValue[].class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "keepCheckedOut"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"), boolean.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "CheckinResult"));
        oper.setReturnClass(org.alfresco.example.webservice.authoring.CheckinResult.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "checkinReturn"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "AuthoringFault"),
                      "org.alfresco.example.webservice.authoring.AuthoringFault",
                      new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "AuthoringFault"), 
                      true
                     ));
        _operations[1] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("checkinExternal");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "items"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "Reference"), org.alfresco.example.webservice.types.Reference.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "comments"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "NamedValue"), org.alfresco.example.webservice.types.NamedValue[].class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "keepCheckedOut"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"), boolean.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "format"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "ContentFormat"), org.alfresco.example.webservice.types.ContentFormat.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "content"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "base64Binary"), byte[].class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "Reference"));
        oper.setReturnClass(org.alfresco.example.webservice.types.Reference.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "checkinExternalReturn"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "AuthoringFault"),
                      "org.alfresco.example.webservice.authoring.AuthoringFault",
                      new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "AuthoringFault"), 
                      true
                     ));
        _operations[2] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("cancelCheckout");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "items"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "Predicate"), org.alfresco.example.webservice.types.Predicate.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "CancelCheckoutResult"));
        oper.setReturnClass(org.alfresco.example.webservice.authoring.CancelCheckoutResult.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "cancelCheckoutReturn"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "AuthoringFault"),
                      "org.alfresco.example.webservice.authoring.AuthoringFault",
                      new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "AuthoringFault"), 
                      true
                     ));
        _operations[3] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("lock");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "items"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "Predicate"), org.alfresco.example.webservice.types.Predicate.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "lockChildren"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"), boolean.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "lockType"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "LockTypeEnum"), org.alfresco.example.webservice.authoring.LockTypeEnum.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "Reference"));
        oper.setReturnClass(org.alfresco.example.webservice.types.Reference[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "lockReturn"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "AuthoringFault"),
                      "org.alfresco.example.webservice.authoring.AuthoringFault",
                      new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "AuthoringFault"), 
                      true
                     ));
        _operations[4] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("unlock");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "items"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "Predicate"), org.alfresco.example.webservice.types.Predicate.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "unlockChildren"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"), boolean.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "Reference"));
        oper.setReturnClass(org.alfresco.example.webservice.types.Reference[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "unlockReturn"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "AuthoringFault"),
                      "org.alfresco.example.webservice.authoring.AuthoringFault",
                      new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "AuthoringFault"), 
                      true
                     ));
        _operations[5] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getLockStatus");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "items"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "Predicate"), org.alfresco.example.webservice.types.Predicate.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "LockStatus"));
        oper.setReturnClass(org.alfresco.example.webservice.authoring.LockStatus[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "getLockStatusReturn"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "AuthoringFault"),
                      "org.alfresco.example.webservice.authoring.AuthoringFault",
                      new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "AuthoringFault"), 
                      true
                     ));
        _operations[6] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("createVersion");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "items"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "Predicate"), org.alfresco.example.webservice.types.Predicate.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "comments"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "NamedValue"), org.alfresco.example.webservice.types.NamedValue[].class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "versionChildren"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"), boolean.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "VersionResult"));
        oper.setReturnClass(org.alfresco.example.webservice.authoring.VersionResult.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "createVersionReturn"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "AuthoringFault"),
                      "org.alfresco.example.webservice.authoring.AuthoringFault",
                      new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "AuthoringFault"), 
                      true
                     ));
        _operations[7] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getVersionHistory");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "node"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "Reference"), org.alfresco.example.webservice.types.Reference.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "VersionHistory"));
        oper.setReturnClass(org.alfresco.example.webservice.types.VersionHistory.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "getVersionHistoryReturn"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "AuthoringFault"),
                      "org.alfresco.example.webservice.authoring.AuthoringFault",
                      new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "AuthoringFault"), 
                      true
                     ));
        _operations[8] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("revertVersion");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "node"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "Reference"), org.alfresco.example.webservice.types.Reference.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "versionLabel"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "AuthoringFault"),
                      "org.alfresco.example.webservice.authoring.AuthoringFault",
                      new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "AuthoringFault"), 
                      true
                     ));
        _operations[9] = oper;

    }

    private static void _initOperationDesc2(){
        org.apache.axis.description.OperationDesc oper;
        org.apache.axis.description.ParameterDesc param;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("deleteAllVersions");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "node"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "Reference"), org.alfresco.example.webservice.types.Reference.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "VersionHistory"));
        oper.setReturnClass(org.alfresco.example.webservice.types.VersionHistory.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "deleteAllVersionsReturn"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "AuthoringFault"),
                      "org.alfresco.example.webservice.authoring.AuthoringFault",
                      new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "AuthoringFault"), 
                      true
                     ));
        _operations[10] = oper;

    }

    public AuthoringServiceSoapBindingStub() throws org.apache.axis.AxisFault {
         this(null);
    }

    public AuthoringServiceSoapBindingStub(java.net.URL endpointURL, javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
         this(service);
         super.cachedEndpoint = endpointURL;
    }

    public AuthoringServiceSoapBindingStub(javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
        if (service == null) {
            super.service = new org.apache.axis.client.Service();
        } else {
            super.service = service;
        }
        ((org.apache.axis.client.Service)super.service).setTypeMappingVersion("1.2");
            java.lang.Class cls;
            javax.xml.namespace.QName qName;
            javax.xml.namespace.QName qName2;
            java.lang.Class beansf = org.apache.axis.encoding.ser.BeanSerializerFactory.class;
            java.lang.Class beandf = org.apache.axis.encoding.ser.BeanDeserializerFactory.class;
            java.lang.Class enumsf = org.apache.axis.encoding.ser.EnumSerializerFactory.class;
            java.lang.Class enumdf = org.apache.axis.encoding.ser.EnumDeserializerFactory.class;
            java.lang.Class arraysf = org.apache.axis.encoding.ser.ArraySerializerFactory.class;
            java.lang.Class arraydf = org.apache.axis.encoding.ser.ArrayDeserializerFactory.class;
            java.lang.Class simplesf = org.apache.axis.encoding.ser.SimpleSerializerFactory.class;
            java.lang.Class simpledf = org.apache.axis.encoding.ser.SimpleDeserializerFactory.class;
            java.lang.Class simplelistsf = org.apache.axis.encoding.ser.SimpleListSerializerFactory.class;
            java.lang.Class simplelistdf = org.apache.axis.encoding.ser.SimpleListDeserializerFactory.class;
            qName = new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", ">ContentFormat>encoding");
            cachedSerQNames.add(qName);
            cls = java.lang.String.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(simplesf);
            cachedDeserFactories.add(simpledf);

            qName = new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", ">ContentFormat>mimetype");
            cachedSerQNames.add(qName);
            cls = java.lang.String.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(simplesf);
            cachedDeserFactories.add(simpledf);

            qName = new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", ">ResultSetRow>node");
            cachedSerQNames.add(qName);
            cls = org.alfresco.example.webservice.types.ResultSetRowNode.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", ">Store>address");
            cachedSerQNames.add(qName);
            cls = java.lang.String.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(simplesf);
            cachedDeserFactories.add(simpledf);

            qName = new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "AssociationDefinition");
            cachedSerQNames.add(qName);
            cls = org.alfresco.example.webservice.types.AssociationDefinition.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "Cardinality");
            cachedSerQNames.add(qName);
            cls = org.alfresco.example.webservice.types.Cardinality.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "Category");
            cachedSerQNames.add(qName);
            cls = org.alfresco.example.webservice.types.Category.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "ClassDefinition");
            cachedSerQNames.add(qName);
            cls = org.alfresco.example.webservice.types.ClassDefinition.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "Classification");
            cachedSerQNames.add(qName);
            cls = org.alfresco.example.webservice.types.Classification.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "ContentFormat");
            cachedSerQNames.add(qName);
            cls = org.alfresco.example.webservice.types.ContentFormat.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "Name");
            cachedSerQNames.add(qName);
            cls = java.lang.String.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(simplesf);
            cachedDeserFactories.add(simpledf);

            qName = new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "NamedValue");
            cachedSerQNames.add(qName);
            cls = org.alfresco.example.webservice.types.NamedValue.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "Node");
            cachedSerQNames.add(qName);
            cls = org.alfresco.example.webservice.types.Node.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "NodeDefinition");
            cachedSerQNames.add(qName);
            cls = org.alfresco.example.webservice.types.NodeDefinition.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "ParentReference");
            cachedSerQNames.add(qName);
            cls = org.alfresco.example.webservice.types.ParentReference.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "Path");
            cachedSerQNames.add(qName);
            cls = java.lang.String.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(simplesf);
            cachedDeserFactories.add(simpledf);

            qName = new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "Predicate");
            cachedSerQNames.add(qName);
            cls = org.alfresco.example.webservice.types.Predicate.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "PropertyDefinition");
            cachedSerQNames.add(qName);
            cls = org.alfresco.example.webservice.types.PropertyDefinition.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "Query");
            cachedSerQNames.add(qName);
            cls = org.alfresco.example.webservice.types.Query.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "QueryLanguageEnum");
            cachedSerQNames.add(qName);
            cls = org.alfresco.example.webservice.types.QueryLanguageEnum.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "Reference");
            cachedSerQNames.add(qName);
            cls = org.alfresco.example.webservice.types.Reference.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "ResultSet");
            cachedSerQNames.add(qName);
            cls = org.alfresco.example.webservice.types.ResultSet.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "ResultSetMetaData");
            cachedSerQNames.add(qName);
            cls = org.alfresco.example.webservice.types.ResultSetMetaData.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "ResultSetRow");
            cachedSerQNames.add(qName);
            cls = org.alfresco.example.webservice.types.ResultSetRow.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "RoleDefinition");
            cachedSerQNames.add(qName);
            cls = org.alfresco.example.webservice.types.RoleDefinition.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "Store");
            cachedSerQNames.add(qName);
            cls = org.alfresco.example.webservice.types.Store.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "StoreEnum");
            cachedSerQNames.add(qName);
            cls = org.alfresco.example.webservice.types.StoreEnum.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "UUID");
            cachedSerQNames.add(qName);
            cls = java.lang.String.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(simplesf);
            cachedDeserFactories.add(simpledf);

            qName = new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "ValueDefinition");
            cachedSerQNames.add(qName);
            cls = org.alfresco.example.webservice.types.ValueDefinition.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "Version");
            cachedSerQNames.add(qName);
            cls = org.alfresco.example.webservice.types.Version.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "VersionHistory");
            cachedSerQNames.add(qName);
            cls = org.alfresco.example.webservice.types.VersionHistory.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "AuthoringFault");
            cachedSerQNames.add(qName);
            cls = org.alfresco.example.webservice.authoring.AuthoringFault.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "CancelCheckoutResult");
            cachedSerQNames.add(qName);
            cls = org.alfresco.example.webservice.authoring.CancelCheckoutResult.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "CheckinResult");
            cachedSerQNames.add(qName);
            cls = org.alfresco.example.webservice.authoring.CheckinResult.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "CheckoutResult");
            cachedSerQNames.add(qName);
            cls = org.alfresco.example.webservice.authoring.CheckoutResult.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "LockStatus");
            cachedSerQNames.add(qName);
            cls = org.alfresco.example.webservice.authoring.LockStatus.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "LockTypeEnum");
            cachedSerQNames.add(qName);
            cls = org.alfresco.example.webservice.authoring.LockTypeEnum.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "VersionResult");
            cachedSerQNames.add(qName);
            cls = org.alfresco.example.webservice.authoring.VersionResult.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

    }

    protected org.apache.axis.client.Call createCall() throws java.rmi.RemoteException {
        try {
            org.apache.axis.client.Call _call = super._createCall();
            if (super.maintainSessionSet) {
                _call.setMaintainSession(super.maintainSession);
            }
            if (super.cachedUsername != null) {
                _call.setUsername(super.cachedUsername);
            }
            if (super.cachedPassword != null) {
                _call.setPassword(super.cachedPassword);
            }
            if (super.cachedEndpoint != null) {
                _call.setTargetEndpointAddress(super.cachedEndpoint);
            }
            if (super.cachedTimeout != null) {
                _call.setTimeout(super.cachedTimeout);
            }
            if (super.cachedPortName != null) {
                _call.setPortName(super.cachedPortName);
            }
            java.util.Enumeration keys = super.cachedProperties.keys();
            while (keys.hasMoreElements()) {
                java.lang.String key = (java.lang.String) keys.nextElement();
                _call.setProperty(key, super.cachedProperties.get(key));
            }
            // All the type mapping information is registered
            // when the first call is made.
            // The type mapping information is actually registered in
            // the TypeMappingRegistry of the service, which
            // is the reason why registration is only needed for the first call.
            synchronized (this) {
                if (firstCall()) {
                    // must set encoding style before registering serializers
                    _call.setEncodingStyle(null);
                    for (int i = 0; i < cachedSerFactories.size(); ++i) {
                        java.lang.Class cls = (java.lang.Class) cachedSerClasses.get(i);
                        javax.xml.namespace.QName qName =
                                (javax.xml.namespace.QName) cachedSerQNames.get(i);
                        java.lang.Object x = cachedSerFactories.get(i);
                        if (x instanceof Class) {
                            java.lang.Class sf = (java.lang.Class)
                                 cachedSerFactories.get(i);
                            java.lang.Class df = (java.lang.Class)
                                 cachedDeserFactories.get(i);
                            _call.registerTypeMapping(cls, qName, sf, df, false);
                        }
                        else if (x instanceof javax.xml.rpc.encoding.SerializerFactory) {
                            org.apache.axis.encoding.SerializerFactory sf = (org.apache.axis.encoding.SerializerFactory)
                                 cachedSerFactories.get(i);
                            org.apache.axis.encoding.DeserializerFactory df = (org.apache.axis.encoding.DeserializerFactory)
                                 cachedDeserFactories.get(i);
                            _call.registerTypeMapping(cls, qName, sf, df, false);
                        }
                    }
                }
            }
            return _call;
        }
        catch (java.lang.Throwable _t) {
            throw new org.apache.axis.AxisFault("Failure trying to get the Call object", _t);
        }
    }


    /**
     * Checkout a content resource for editing.
     */
    public org.alfresco.example.webservice.authoring.CheckoutResult checkout(org.alfresco.example.webservice.types.Predicate items, org.alfresco.example.webservice.types.ParentReference destination) throws java.rmi.RemoteException, org.alfresco.example.webservice.authoring.AuthoringFault {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[0]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://www.alfresco.org/ws/service/authoring/1.0/checkout");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "checkout"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {items, destination});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (org.alfresco.example.webservice.authoring.CheckoutResult) _resp;
            } catch (java.lang.Exception _exception) {
                return (org.alfresco.example.webservice.authoring.CheckoutResult) org.apache.axis.utils.JavaUtils.convert(_resp, org.alfresco.example.webservice.authoring.CheckoutResult.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.alfresco.example.webservice.authoring.AuthoringFault) {
              throw (org.alfresco.example.webservice.authoring.AuthoringFault) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }


    /**
     * Checkin a content resource.
     */
    public org.alfresco.example.webservice.authoring.CheckinResult checkin(org.alfresco.example.webservice.types.Predicate items, org.alfresco.example.webservice.types.NamedValue[] comments, boolean keepCheckedOut) throws java.rmi.RemoteException, org.alfresco.example.webservice.authoring.AuthoringFault {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[1]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://www.alfresco.org/ws/service/authoring/1.0/checkin");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "checkin"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {items, comments, new java.lang.Boolean(keepCheckedOut)});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (org.alfresco.example.webservice.authoring.CheckinResult) _resp;
            } catch (java.lang.Exception _exception) {
                return (org.alfresco.example.webservice.authoring.CheckinResult) org.apache.axis.utils.JavaUtils.convert(_resp, org.alfresco.example.webservice.authoring.CheckinResult.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.alfresco.example.webservice.authoring.AuthoringFault) {
              throw (org.alfresco.example.webservice.authoring.AuthoringFault) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }


    /**
     * Checkin an external content resource thus replacing the existing
     * working content.
     */
    public org.alfresco.example.webservice.types.Reference checkinExternal(org.alfresco.example.webservice.types.Reference items, org.alfresco.example.webservice.types.NamedValue[] comments, boolean keepCheckedOut, org.alfresco.example.webservice.types.ContentFormat format, byte[] content) throws java.rmi.RemoteException, org.alfresco.example.webservice.authoring.AuthoringFault {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[2]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://www.alfresco.org/ws/service/authoring/1.0/checkinExternal");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "checkinExternal"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {items, comments, new java.lang.Boolean(keepCheckedOut), format, content});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (org.alfresco.example.webservice.types.Reference) _resp;
            } catch (java.lang.Exception _exception) {
                return (org.alfresco.example.webservice.types.Reference) org.apache.axis.utils.JavaUtils.convert(_resp, org.alfresco.example.webservice.types.Reference.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.alfresco.example.webservice.authoring.AuthoringFault) {
              throw (org.alfresco.example.webservice.authoring.AuthoringFault) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }


    /**
     * Cancels the checkout.
     */
    public org.alfresco.example.webservice.authoring.CancelCheckoutResult cancelCheckout(org.alfresco.example.webservice.types.Predicate items) throws java.rmi.RemoteException, org.alfresco.example.webservice.authoring.AuthoringFault {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[3]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://www.alfresco.org/ws/service/authoring/1.0/cancelCheckout");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "cancelCheckout"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {items});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (org.alfresco.example.webservice.authoring.CancelCheckoutResult) _resp;
            } catch (java.lang.Exception _exception) {
                return (org.alfresco.example.webservice.authoring.CancelCheckoutResult) org.apache.axis.utils.JavaUtils.convert(_resp, org.alfresco.example.webservice.authoring.CancelCheckoutResult.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.alfresco.example.webservice.authoring.AuthoringFault) {
              throw (org.alfresco.example.webservice.authoring.AuthoringFault) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }


    /**
     * Locks a content resource.
     */
    public org.alfresco.example.webservice.types.Reference[] lock(org.alfresco.example.webservice.types.Predicate items, boolean lockChildren, org.alfresco.example.webservice.authoring.LockTypeEnum lockType) throws java.rmi.RemoteException, org.alfresco.example.webservice.authoring.AuthoringFault {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[4]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://www.alfresco.org/ws/service/authoring/1.0/lock");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "lock"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {items, new java.lang.Boolean(lockChildren), lockType});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (org.alfresco.example.webservice.types.Reference[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (org.alfresco.example.webservice.types.Reference[]) org.apache.axis.utils.JavaUtils.convert(_resp, org.alfresco.example.webservice.types.Reference[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.alfresco.example.webservice.authoring.AuthoringFault) {
              throw (org.alfresco.example.webservice.authoring.AuthoringFault) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }


    /**
     * Unlocks a content resource.
     */
    public org.alfresco.example.webservice.types.Reference[] unlock(org.alfresco.example.webservice.types.Predicate items, boolean unlockChildren) throws java.rmi.RemoteException, org.alfresco.example.webservice.authoring.AuthoringFault {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[5]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://www.alfresco.org/ws/service/authoring/1.0/unlock");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "unlock"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {items, new java.lang.Boolean(unlockChildren)});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (org.alfresco.example.webservice.types.Reference[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (org.alfresco.example.webservice.types.Reference[]) org.apache.axis.utils.JavaUtils.convert(_resp, org.alfresco.example.webservice.types.Reference[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.alfresco.example.webservice.authoring.AuthoringFault) {
              throw (org.alfresco.example.webservice.authoring.AuthoringFault) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }


    /**
     * Gets the lock status of the specified nodes.
     */
    public org.alfresco.example.webservice.authoring.LockStatus[] getLockStatus(org.alfresco.example.webservice.types.Predicate items) throws java.rmi.RemoteException, org.alfresco.example.webservice.authoring.AuthoringFault {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[6]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://www.alfresco.org/ws/service/authoring/1.0/getLockStatus");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "getLockStatus"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {items});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (org.alfresco.example.webservice.authoring.LockStatus[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (org.alfresco.example.webservice.authoring.LockStatus[]) org.apache.axis.utils.JavaUtils.convert(_resp, org.alfresco.example.webservice.authoring.LockStatus[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.alfresco.example.webservice.authoring.AuthoringFault) {
              throw (org.alfresco.example.webservice.authoring.AuthoringFault) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }


    /**
     * Create a snapshot of the specified node(s) in the version store.
     */
    public org.alfresco.example.webservice.authoring.VersionResult createVersion(org.alfresco.example.webservice.types.Predicate items, org.alfresco.example.webservice.types.NamedValue[] comments, boolean versionChildren) throws java.rmi.RemoteException, org.alfresco.example.webservice.authoring.AuthoringFault {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[7]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://www.alfresco.org/ws/service/authoring/1.0/createVersion");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "createVersion"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {items, comments, new java.lang.Boolean(versionChildren)});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (org.alfresco.example.webservice.authoring.VersionResult) _resp;
            } catch (java.lang.Exception _exception) {
                return (org.alfresco.example.webservice.authoring.VersionResult) org.apache.axis.utils.JavaUtils.convert(_resp, org.alfresco.example.webservice.authoring.VersionResult.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.alfresco.example.webservice.authoring.AuthoringFault) {
              throw (org.alfresco.example.webservice.authoring.AuthoringFault) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }


    /**
     * Gets the version history for the specfied node.
     */
    public org.alfresco.example.webservice.types.VersionHistory getVersionHistory(org.alfresco.example.webservice.types.Reference node) throws java.rmi.RemoteException, org.alfresco.example.webservice.authoring.AuthoringFault {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[8]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://www.alfresco.org/ws/service/authoring/1.0/getVersionHistory");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "getVersionHistory"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {node});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (org.alfresco.example.webservice.types.VersionHistory) _resp;
            } catch (java.lang.Exception _exception) {
                return (org.alfresco.example.webservice.types.VersionHistory) org.apache.axis.utils.JavaUtils.convert(_resp, org.alfresco.example.webservice.types.VersionHistory.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.alfresco.example.webservice.authoring.AuthoringFault) {
              throw (org.alfresco.example.webservice.authoring.AuthoringFault) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }


    /**
     * Revert a node to the snapshot of the specified version.
     */
    public void revertVersion(org.alfresco.example.webservice.types.Reference node, java.lang.String versionLabel) throws java.rmi.RemoteException, org.alfresco.example.webservice.authoring.AuthoringFault {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[9]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://www.alfresco.org/ws/service/authoring/1.0/revertVersion");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "revertVersion"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {node, versionLabel});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        extractAttachments(_call);
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.alfresco.example.webservice.authoring.AuthoringFault) {
              throw (org.alfresco.example.webservice.authoring.AuthoringFault) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }


    /**
     * Delete all snapshot versions of the specified node.
     */
    public org.alfresco.example.webservice.types.VersionHistory deleteAllVersions(org.alfresco.example.webservice.types.Reference node) throws java.rmi.RemoteException, org.alfresco.example.webservice.authoring.AuthoringFault {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[10]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://www.alfresco.org/ws/service/authoring/1.0/deleteAllVersions");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "deleteAllVersions"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {node});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (org.alfresco.example.webservice.types.VersionHistory) _resp;
            } catch (java.lang.Exception _exception) {
                return (org.alfresco.example.webservice.types.VersionHistory) org.apache.axis.utils.JavaUtils.convert(_resp, org.alfresco.example.webservice.types.VersionHistory.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.alfresco.example.webservice.authoring.AuthoringFault) {
              throw (org.alfresco.example.webservice.authoring.AuthoringFault) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

}
