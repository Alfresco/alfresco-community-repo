/**
 * RepositoryServiceSoapBindingStub.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package org.alfresco.example.webservice.repository;

public class RepositoryServiceSoapBindingStub extends org.apache.axis.client.Stub implements org.alfresco.example.webservice.repository.RepositoryServiceSoapPort {
    private java.util.Vector cachedSerClasses = new java.util.Vector();
    private java.util.Vector cachedSerQNames = new java.util.Vector();
    private java.util.Vector cachedSerFactories = new java.util.Vector();
    private java.util.Vector cachedDeserFactories = new java.util.Vector();

    static org.apache.axis.description.OperationDesc [] _operations;

    static {
        _operations = new org.apache.axis.description.OperationDesc[9];
        _initOperationDesc1();
    }

    private static void _initOperationDesc1(){
        org.apache.axis.description.OperationDesc oper;
        org.apache.axis.description.ParameterDesc param;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getStores");
        oper.setReturnType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "Store"));
        oper.setReturnClass(org.alfresco.example.webservice.types.Store[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/repository/1.0", "getStoresReturn"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/repository/1.0", "RepositoryFault"),
                      "org.alfresco.example.webservice.repository.RepositoryFault",
                      new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/repository/1.0", "RepositoryFault"), 
                      true
                     ));
        _operations[0] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("query");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/repository/1.0", "store"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "Store"), org.alfresco.example.webservice.types.Store.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/repository/1.0", "query"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "Query"), org.alfresco.example.webservice.types.Query.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/repository/1.0", "includeMetaData"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"), boolean.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/repository/1.0", "QueryResult"));
        oper.setReturnClass(org.alfresco.example.webservice.repository.QueryResult.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/repository/1.0", "queryReturn"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/repository/1.0", "RepositoryFault"),
                      "org.alfresco.example.webservice.repository.RepositoryFault",
                      new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/repository/1.0", "RepositoryFault"), 
                      true
                     ));
        _operations[1] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("queryChildren");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/repository/1.0", "node"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "Reference"), org.alfresco.example.webservice.types.Reference.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/repository/1.0", "QueryResult"));
        oper.setReturnClass(org.alfresco.example.webservice.repository.QueryResult.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/repository/1.0", "queryReturn"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/repository/1.0", "RepositoryFault"),
                      "org.alfresco.example.webservice.repository.RepositoryFault",
                      new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/repository/1.0", "RepositoryFault"), 
                      true
                     ));
        _operations[2] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("queryParents");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/repository/1.0", "node"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "Reference"), org.alfresco.example.webservice.types.Reference.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/repository/1.0", "QueryResult"));
        oper.setReturnClass(org.alfresco.example.webservice.repository.QueryResult.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/repository/1.0", "queryReturn"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/repository/1.0", "RepositoryFault"),
                      "org.alfresco.example.webservice.repository.RepositoryFault",
                      new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/repository/1.0", "RepositoryFault"), 
                      true
                     ));
        _operations[3] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("queryAssociated");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/repository/1.0", "node"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "Reference"), org.alfresco.example.webservice.types.Reference.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/repository/1.0", "association"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/repository/1.0", "Association"), org.alfresco.example.webservice.repository.Association[].class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/repository/1.0", "QueryResult"));
        oper.setReturnClass(org.alfresco.example.webservice.repository.QueryResult.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/repository/1.0", "queryReturn"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/repository/1.0", "RepositoryFault"),
                      "org.alfresco.example.webservice.repository.RepositoryFault",
                      new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/repository/1.0", "RepositoryFault"), 
                      true
                     ));
        _operations[4] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("fetchMore");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/repository/1.0", "querySession"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/repository/1.0", "QueryResult"));
        oper.setReturnClass(org.alfresco.example.webservice.repository.QueryResult.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/repository/1.0", "queryReturn"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/repository/1.0", "RepositoryFault"),
                      "org.alfresco.example.webservice.repository.RepositoryFault",
                      new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/repository/1.0", "RepositoryFault"), 
                      true
                     ));
        _operations[5] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("update");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/repository/1.0", "statements"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.alfresco.org/ws/cml/1.0", "CML"), org.alfresco.example.webservice.types.CML.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/repository/1.0", "UpdateResult"));
        oper.setReturnClass(org.alfresco.example.webservice.repository.UpdateResult[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/repository/1.0", "updateReturn"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/repository/1.0", "RepositoryFault"),
                      "org.alfresco.example.webservice.repository.RepositoryFault",
                      new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/repository/1.0", "RepositoryFault"), 
                      true
                     ));
        _operations[6] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("describe");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/repository/1.0", "items"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "Predicate"), org.alfresco.example.webservice.types.Predicate.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "NodeDefinition"));
        oper.setReturnClass(org.alfresco.example.webservice.types.NodeDefinition[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/repository/1.0", "describeReturn"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/repository/1.0", "RepositoryFault"),
                      "org.alfresco.example.webservice.repository.RepositoryFault",
                      new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/repository/1.0", "RepositoryFault"), 
                      true
                     ));
        _operations[7] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("get");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/repository/1.0", "where"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "Predicate"), org.alfresco.example.webservice.types.Predicate.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "Node"));
        oper.setReturnClass(org.alfresco.example.webservice.types.Node[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/repository/1.0", "getReturn"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/repository/1.0", "RepositoryFault"),
                      "org.alfresco.example.webservice.repository.RepositoryFault",
                      new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/repository/1.0", "RepositoryFault"), 
                      true
                     ));
        _operations[8] = oper;

    }

    public RepositoryServiceSoapBindingStub() throws org.apache.axis.AxisFault {
         this(null);
    }

    public RepositoryServiceSoapBindingStub(java.net.URL endpointURL, javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
         this(service);
         super.cachedEndpoint = endpointURL;
    }

    public RepositoryServiceSoapBindingStub(javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
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
            qName = new javax.xml.namespace.QName("http://www.alfresco.org/ws/cml/1.0", ">CML>addAspect");
            cachedSerQNames.add(qName);
            cls = org.alfresco.example.webservice.types.CMLAddAspect.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.alfresco.org/ws/cml/1.0", ">CML>addChild");
            cachedSerQNames.add(qName);
            cls = org.alfresco.example.webservice.types.CMLAddChild.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.alfresco.org/ws/cml/1.0", ">CML>copy");
            cachedSerQNames.add(qName);
            cls = org.alfresco.example.webservice.types.CMLCopy.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.alfresco.org/ws/cml/1.0", ">CML>create");
            cachedSerQNames.add(qName);
            cls = org.alfresco.example.webservice.types.CMLCreate.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.alfresco.org/ws/cml/1.0", ">CML>createAssociation");
            cachedSerQNames.add(qName);
            cls = org.alfresco.example.webservice.types.CMLCreateAssociation.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.alfresco.org/ws/cml/1.0", ">CML>delete");
            cachedSerQNames.add(qName);
            cls = org.alfresco.example.webservice.types.CMLDelete.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.alfresco.org/ws/cml/1.0", ">CML>move");
            cachedSerQNames.add(qName);
            cls = org.alfresco.example.webservice.types.CMLMove.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.alfresco.org/ws/cml/1.0", ">CML>removeAspect");
            cachedSerQNames.add(qName);
            cls = org.alfresco.example.webservice.types.CMLRemoveAspect.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.alfresco.org/ws/cml/1.0", ">CML>removeAssociation");
            cachedSerQNames.add(qName);
            cls = org.alfresco.example.webservice.types.CMLRemoveAssociation.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.alfresco.org/ws/cml/1.0", ">CML>removeChild");
            cachedSerQNames.add(qName);
            cls = org.alfresco.example.webservice.types.CMLRemoveChild.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.alfresco.org/ws/cml/1.0", ">CML>update");
            cachedSerQNames.add(qName);
            cls = org.alfresco.example.webservice.types.CMLUpdate.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.alfresco.org/ws/cml/1.0", "CML");
            cachedSerQNames.add(qName);
            cls = org.alfresco.example.webservice.types.CML.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.alfresco.org/ws/headers/1.0", ">NamespaceConfiguration>mapping");
            cachedSerQNames.add(qName);
            cls = org.alfresco.example.webservice.types.NamespaceConfigurationMapping.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.alfresco.org/ws/headers/1.0", "LocaleConfiguration");
            cachedSerQNames.add(qName);
            cls = org.alfresco.example.webservice.types.LocaleConfiguration.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.alfresco.org/ws/headers/1.0", "NamespaceConfiguration");
            cachedSerQNames.add(qName);
            cls = org.alfresco.example.webservice.types.NamespaceConfigurationMapping[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://www.alfresco.org/ws/headers/1.0", ">NamespaceConfiguration>mapping");
            qName2 = new javax.xml.namespace.QName("http://www.alfresco.org/ws/headers/1.0", "mapping");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://www.alfresco.org/ws/headers/1.0", "QueryConfiguration");
            cachedSerQNames.add(qName);
            cls = org.alfresco.example.webservice.types.QueryConfiguration.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

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

            qName = new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/repository/1.0", "Association");
            cachedSerQNames.add(qName);
            cls = org.alfresco.example.webservice.repository.Association.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/repository/1.0", "AssociationDirectionEnum");
            cachedSerQNames.add(qName);
            cls = org.alfresco.example.webservice.repository.AssociationDirectionEnum.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/repository/1.0", "QueryResult");
            cachedSerQNames.add(qName);
            cls = org.alfresco.example.webservice.repository.QueryResult.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/repository/1.0", "RepositoryFault");
            cachedSerQNames.add(qName);
            cls = org.alfresco.example.webservice.repository.RepositoryFault.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/repository/1.0", "UpdateResult");
            cachedSerQNames.add(qName);
            cls = org.alfresco.example.webservice.repository.UpdateResult.class;
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
     * Retrieves a list of stores where content resources are held.
     */
    public org.alfresco.example.webservice.types.Store[] getStores() throws java.rmi.RemoteException, org.alfresco.example.webservice.repository.RepositoryFault {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[0]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://www.alfresco.org/ws/service/repository/1.0/getStores");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/repository/1.0", "getStores"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (org.alfresco.example.webservice.types.Store[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (org.alfresco.example.webservice.types.Store[]) org.apache.axis.utils.JavaUtils.convert(_resp, org.alfresco.example.webservice.types.Store[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.alfresco.example.webservice.repository.RepositoryFault) {
              throw (org.alfresco.example.webservice.repository.RepositoryFault) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }


    /**
     * Executes a query against a store
     */
    public org.alfresco.example.webservice.repository.QueryResult query(org.alfresco.example.webservice.types.Store store, org.alfresco.example.webservice.types.Query query, boolean includeMetaData) throws java.rmi.RemoteException, org.alfresco.example.webservice.repository.RepositoryFault {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[1]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://www.alfresco.org/ws/service/repository/1.0/query");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/repository/1.0", "query"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {store, query, new java.lang.Boolean(includeMetaData)});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (org.alfresco.example.webservice.repository.QueryResult) _resp;
            } catch (java.lang.Exception _exception) {
                return (org.alfresco.example.webservice.repository.QueryResult) org.apache.axis.utils.JavaUtils.convert(_resp, org.alfresco.example.webservice.repository.QueryResult.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.alfresco.example.webservice.repository.RepositoryFault) {
              throw (org.alfresco.example.webservice.repository.RepositoryFault) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }


    /**
     * Executes a query to retrieve the children of the specified
     * resource.
     */
    public org.alfresco.example.webservice.repository.QueryResult queryChildren(org.alfresco.example.webservice.types.Reference node) throws java.rmi.RemoteException, org.alfresco.example.webservice.repository.RepositoryFault {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[2]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://www.alfresco.org/ws/service/repository/1.0/queryChildren");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/repository/1.0", "queryChildren"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {node});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (org.alfresco.example.webservice.repository.QueryResult) _resp;
            } catch (java.lang.Exception _exception) {
                return (org.alfresco.example.webservice.repository.QueryResult) org.apache.axis.utils.JavaUtils.convert(_resp, org.alfresco.example.webservice.repository.QueryResult.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.alfresco.example.webservice.repository.RepositoryFault) {
              throw (org.alfresco.example.webservice.repository.RepositoryFault) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }


    /**
     * Executes a query to retrieve the parents of the specified resource.
     */
    public org.alfresco.example.webservice.repository.QueryResult queryParents(org.alfresco.example.webservice.types.Reference node) throws java.rmi.RemoteException, org.alfresco.example.webservice.repository.RepositoryFault {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[3]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://www.alfresco.org/ws/service/repository/1.0/queryParents");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/repository/1.0", "queryParents"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {node});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (org.alfresco.example.webservice.repository.QueryResult) _resp;
            } catch (java.lang.Exception _exception) {
                return (org.alfresco.example.webservice.repository.QueryResult) org.apache.axis.utils.JavaUtils.convert(_resp, org.alfresco.example.webservice.repository.QueryResult.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.alfresco.example.webservice.repository.RepositoryFault) {
              throw (org.alfresco.example.webservice.repository.RepositoryFault) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }


    /**
     * Executes a query to retrieve associated resources of the specified
     * resource.
     */
    public org.alfresco.example.webservice.repository.QueryResult queryAssociated(org.alfresco.example.webservice.types.Reference node, org.alfresco.example.webservice.repository.Association[] association) throws java.rmi.RemoteException, org.alfresco.example.webservice.repository.RepositoryFault {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[4]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://www.alfresco.org/ws/service/repository/1.0/queryAssociated");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/repository/1.0", "queryAssociated"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {node, association});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (org.alfresco.example.webservice.repository.QueryResult) _resp;
            } catch (java.lang.Exception _exception) {
                return (org.alfresco.example.webservice.repository.QueryResult) org.apache.axis.utils.JavaUtils.convert(_resp, org.alfresco.example.webservice.repository.QueryResult.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.alfresco.example.webservice.repository.RepositoryFault) {
              throw (org.alfresco.example.webservice.repository.RepositoryFault) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }


    /**
     * Fetches the next batch of query results.
     */
    public org.alfresco.example.webservice.repository.QueryResult fetchMore(java.lang.String querySession) throws java.rmi.RemoteException, org.alfresco.example.webservice.repository.RepositoryFault {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[5]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://www.alfresco.org/ws/service/repository/1.0/fetchMore");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/repository/1.0", "fetchMore"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {querySession});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (org.alfresco.example.webservice.repository.QueryResult) _resp;
            } catch (java.lang.Exception _exception) {
                return (org.alfresco.example.webservice.repository.QueryResult) org.apache.axis.utils.JavaUtils.convert(_resp, org.alfresco.example.webservice.repository.QueryResult.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.alfresco.example.webservice.repository.RepositoryFault) {
              throw (org.alfresco.example.webservice.repository.RepositoryFault) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }


    /**
     * Executes a CML script to manipulate the contents of a Repository
     * store.
     */
    public org.alfresco.example.webservice.repository.UpdateResult[] update(org.alfresco.example.webservice.types.CML statements) throws java.rmi.RemoteException, org.alfresco.example.webservice.repository.RepositoryFault {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[6]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://www.alfresco.org/ws/service/repository/1.0/update");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/repository/1.0", "update"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {statements});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (org.alfresco.example.webservice.repository.UpdateResult[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (org.alfresco.example.webservice.repository.UpdateResult[]) org.apache.axis.utils.JavaUtils.convert(_resp, org.alfresco.example.webservice.repository.UpdateResult[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.alfresco.example.webservice.repository.RepositoryFault) {
              throw (org.alfresco.example.webservice.repository.RepositoryFault) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }


    /**
     * Describes a content resource.
     */
    public org.alfresco.example.webservice.types.NodeDefinition[] describe(org.alfresco.example.webservice.types.Predicate items) throws java.rmi.RemoteException, org.alfresco.example.webservice.repository.RepositoryFault {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[7]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://www.alfresco.org/ws/service/repository/1.0/describe");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/repository/1.0", "describe"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {items});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (org.alfresco.example.webservice.types.NodeDefinition[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (org.alfresco.example.webservice.types.NodeDefinition[]) org.apache.axis.utils.JavaUtils.convert(_resp, org.alfresco.example.webservice.types.NodeDefinition[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.alfresco.example.webservice.repository.RepositoryFault) {
              throw (org.alfresco.example.webservice.repository.RepositoryFault) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }


    /**
     * Gets a resource from the respository.
     */
    public org.alfresco.example.webservice.types.Node[] get(org.alfresco.example.webservice.types.Predicate where) throws java.rmi.RemoteException, org.alfresco.example.webservice.repository.RepositoryFault {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[8]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://www.alfresco.org/ws/service/repository/1.0/get");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/repository/1.0", "get"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {where});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (org.alfresco.example.webservice.types.Node[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (org.alfresco.example.webservice.types.Node[]) org.apache.axis.utils.JavaUtils.convert(_resp, org.alfresco.example.webservice.types.Node[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.alfresco.example.webservice.repository.RepositoryFault) {
              throw (org.alfresco.example.webservice.repository.RepositoryFault) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

}
