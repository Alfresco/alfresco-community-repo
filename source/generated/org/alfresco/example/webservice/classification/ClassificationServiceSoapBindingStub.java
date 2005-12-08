/**
 * ClassificationServiceSoapBindingStub.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package org.alfresco.example.webservice.classification;

public class ClassificationServiceSoapBindingStub extends org.apache.axis.client.Stub implements org.alfresco.example.webservice.classification.ClassificationServiceSoapPort {
    private java.util.Vector cachedSerClasses = new java.util.Vector();
    private java.util.Vector cachedSerQNames = new java.util.Vector();
    private java.util.Vector cachedSerFactories = new java.util.Vector();
    private java.util.Vector cachedDeserFactories = new java.util.Vector();

    static org.apache.axis.description.OperationDesc [] _operations;

    static {
        _operations = new org.apache.axis.description.OperationDesc[5];
        _initOperationDesc1();
    }

    private static void _initOperationDesc1(){
        org.apache.axis.description.OperationDesc oper;
        org.apache.axis.description.ParameterDesc param;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getClassifications");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/classification/1.0", "store"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "Store"), org.alfresco.example.webservice.types.Store.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "Classification"));
        oper.setReturnClass(org.alfresco.example.webservice.types.Classification[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/classification/1.0", "getClassificationsReturn"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/classification/1.0", "ClassificationFault"),
                      "org.alfresco.example.webservice.classification.ClassificationFault",
                      new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/classification/1.0", "ClassificationFault"), 
                      true
                     ));
        _operations[0] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getChildCategories");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/classification/1.0", "parentCategory"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "Reference"), org.alfresco.example.webservice.types.Reference.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "Category"));
        oper.setReturnClass(org.alfresco.example.webservice.types.Category[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/classification/1.0", "getChildCategoriesReturn"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/classification/1.0", "ClassificationFault"),
                      "org.alfresco.example.webservice.classification.ClassificationFault",
                      new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/classification/1.0", "ClassificationFault"), 
                      true
                     ));
        _operations[1] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getCategories");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/classification/1.0", "items"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "Predicate"), org.alfresco.example.webservice.types.Predicate.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/classification/1.0", "CategoriesResult"));
        oper.setReturnClass(org.alfresco.example.webservice.classification.CategoriesResult[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/classification/1.0", "getCategoriesReturn"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/classification/1.0", "ClassificationFault"),
                      "org.alfresco.example.webservice.classification.ClassificationFault",
                      new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/classification/1.0", "ClassificationFault"), 
                      true
                     ));
        _operations[2] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("setCategories");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/classification/1.0", "items"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "Predicate"), org.alfresco.example.webservice.types.Predicate.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/classification/1.0", "categories"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/classification/1.0", "AppliedCategory"), org.alfresco.example.webservice.classification.AppliedCategory[].class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/classification/1.0", "CategoriesResult"));
        oper.setReturnClass(org.alfresco.example.webservice.classification.CategoriesResult[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/classification/1.0", "setCategoriesReturn"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/classification/1.0", "ClassificationFault"),
                      "org.alfresco.example.webservice.classification.ClassificationFault",
                      new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/classification/1.0", "ClassificationFault"), 
                      true
                     ));
        _operations[3] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("describeClassification");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/classification/1.0", "classification"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "Name"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "ClassDefinition"));
        oper.setReturnClass(org.alfresco.example.webservice.types.ClassDefinition.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/classification/1.0", "describeClassificationReturn"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/classification/1.0", "ClassificationFault"),
                      "org.alfresco.example.webservice.classification.ClassificationFault",
                      new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/classification/1.0", "ClassificationFault"), 
                      true
                     ));
        _operations[4] = oper;

    }

    public ClassificationServiceSoapBindingStub() throws org.apache.axis.AxisFault {
         this(null);
    }

    public ClassificationServiceSoapBindingStub(java.net.URL endpointURL, javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
         this(service);
         super.cachedEndpoint = endpointURL;
    }

    public ClassificationServiceSoapBindingStub(javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
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

            qName = new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/classification/1.0", "AppliedCategory");
            cachedSerQNames.add(qName);
            cls = org.alfresco.example.webservice.classification.AppliedCategory.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/classification/1.0", "CategoriesResult");
            cachedSerQNames.add(qName);
            cls = org.alfresco.example.webservice.classification.CategoriesResult.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/classification/1.0", "ClassificationFault");
            cachedSerQNames.add(qName);
            cls = org.alfresco.example.webservice.classification.ClassificationFault.class;
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
     * Gets available classifications.
     */
    public org.alfresco.example.webservice.types.Classification[] getClassifications(org.alfresco.example.webservice.types.Store store) throws java.rmi.RemoteException, org.alfresco.example.webservice.classification.ClassificationFault {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[0]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://www.alfresco.org/ws/service/classification/1.0/getClassifications");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/classification/1.0", "getClassifications"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {store});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (org.alfresco.example.webservice.types.Classification[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (org.alfresco.example.webservice.types.Classification[]) org.apache.axis.utils.JavaUtils.convert(_resp, org.alfresco.example.webservice.types.Classification[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.alfresco.example.webservice.classification.ClassificationFault) {
              throw (org.alfresco.example.webservice.classification.ClassificationFault) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }


    /**
     * Gets child categories for the specified parent category.
     */
    public org.alfresco.example.webservice.types.Category[] getChildCategories(org.alfresco.example.webservice.types.Reference parentCategory) throws java.rmi.RemoteException, org.alfresco.example.webservice.classification.ClassificationFault {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[1]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://www.alfresco.org/ws/service/classification/1.0/getChildCategories");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/classification/1.0", "getChildCategories"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parentCategory});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (org.alfresco.example.webservice.types.Category[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (org.alfresco.example.webservice.types.Category[]) org.apache.axis.utils.JavaUtils.convert(_resp, org.alfresco.example.webservice.types.Category[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.alfresco.example.webservice.classification.ClassificationFault) {
              throw (org.alfresco.example.webservice.classification.ClassificationFault) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }


    /**
     * Gets the categories applied to the specified content resource.
     */
    public org.alfresco.example.webservice.classification.CategoriesResult[] getCategories(org.alfresco.example.webservice.types.Predicate items) throws java.rmi.RemoteException, org.alfresco.example.webservice.classification.ClassificationFault {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[2]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://www.alfresco.org/ws/service/classification/1.0/getCategories");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/classification/1.0", "getCategories"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {items});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (org.alfresco.example.webservice.classification.CategoriesResult[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (org.alfresco.example.webservice.classification.CategoriesResult[]) org.apache.axis.utils.JavaUtils.convert(_resp, org.alfresco.example.webservice.classification.CategoriesResult[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.alfresco.example.webservice.classification.ClassificationFault) {
              throw (org.alfresco.example.webservice.classification.ClassificationFault) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }


    /**
     * Modifies the categories applied to the specified content resource.
     */
    public org.alfresco.example.webservice.classification.CategoriesResult[] setCategories(org.alfresco.example.webservice.types.Predicate items, org.alfresco.example.webservice.classification.AppliedCategory[] categories) throws java.rmi.RemoteException, org.alfresco.example.webservice.classification.ClassificationFault {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[3]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://www.alfresco.org/ws/service/classification/1.0/setCategories");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/classification/1.0", "setCategories"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {items, categories});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (org.alfresco.example.webservice.classification.CategoriesResult[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (org.alfresco.example.webservice.classification.CategoriesResult[]) org.apache.axis.utils.JavaUtils.convert(_resp, org.alfresco.example.webservice.classification.CategoriesResult[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.alfresco.example.webservice.classification.ClassificationFault) {
              throw (org.alfresco.example.webservice.classification.ClassificationFault) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }


    /**
     * Describe a classification in terms of how it is represented
     * in the meta model i.e. is it an aspect, which property holds the applied
     * category.
     */
    public org.alfresco.example.webservice.types.ClassDefinition describeClassification(java.lang.String classification) throws java.rmi.RemoteException, org.alfresco.example.webservice.classification.ClassificationFault {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[4]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://www.alfresco.org/ws/service/classification/1.0/describeClassification");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/classification/1.0", "describeClassification"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {classification});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (org.alfresco.example.webservice.types.ClassDefinition) _resp;
            } catch (java.lang.Exception _exception) {
                return (org.alfresco.example.webservice.types.ClassDefinition) org.apache.axis.utils.JavaUtils.convert(_resp, org.alfresco.example.webservice.types.ClassDefinition.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.alfresco.example.webservice.classification.ClassificationFault) {
              throw (org.alfresco.example.webservice.classification.ClassificationFault) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

}
