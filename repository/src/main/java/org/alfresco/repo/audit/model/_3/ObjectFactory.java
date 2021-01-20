/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.repo.audit.model._3;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.alfresco.repo.audit.model._3 package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Audit_QNAME = new QName("http://www.alfresco.org/repo/audit/model/3.2", "Audit");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.alfresco.repo.audit.model._3
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Audit }
     * 
     */
    public Audit createAudit() {
        return new Audit();
    }

    /**
     * Create an instance of {@link RecordValue }
     * 
     */
    public RecordValue createRecordValue() {
        return new RecordValue();
    }

    /**
     * Create an instance of {@link PathMap }
     * 
     */
    public PathMap createPathMap() {
        return new PathMap();
    }

    /**
     * Create an instance of {@link AuditPath }
     * 
     */
    public AuditPath createAuditPath() {
        return new AuditPath();
    }

    /**
     * Create an instance of {@link GenerateValue }
     * 
     */
    public GenerateValue createGenerateValue() {
        return new GenerateValue();
    }

    /**
     * Create an instance of {@link Application }
     * 
     */
    public Application createApplication() {
        return new Application();
    }

    /**
     * Create an instance of {@link KeyedAuditDefinition }
     * 
     */
    public KeyedAuditDefinition createKeyedAuditDefinition() {
        return new KeyedAuditDefinition();
    }

    /**
     * Create an instance of {@link DataExtractors }
     * 
     */
    public DataExtractors createDataExtractors() {
        return new DataExtractors();
    }

    /**
     * Create an instance of {@link DataGenerator }
     * 
     */
    public DataGenerator createDataGenerator() {
        return new DataGenerator();
    }

    /**
     * Create an instance of {@link PathMappings }
     * 
     */
    public PathMappings createPathMappings() {
        return new PathMappings();
    }

    /**
     * Create an instance of {@link DataExtractor }
     * 
     */
    public DataExtractor createDataExtractor() {
        return new DataExtractor();
    }

    /**
     * Create an instance of {@link DataGenerators }
     * 
     */
    public DataGenerators createDataGenerators() {
        return new DataGenerators();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Audit }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.alfresco.org/repo/audit/model/3.2", name = "Audit")
    public JAXBElement<Audit> createAudit(Audit value) {
        return new JAXBElement<Audit>(_Audit_QNAME, Audit.class, null, value);
    }

}
