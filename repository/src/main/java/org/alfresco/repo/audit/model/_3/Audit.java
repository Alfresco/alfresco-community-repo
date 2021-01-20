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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Audit complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Audit">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="DataExtractors" type="{http://www.alfresco.org/repo/audit/model/3.2}DataExtractors" minOccurs="0"/>
 *         &lt;element name="DataGenerators" type="{http://www.alfresco.org/repo/audit/model/3.2}DataGenerators" minOccurs="0"/>
 *         &lt;element name="PathMappings" type="{http://www.alfresco.org/repo/audit/model/3.2}PathMappings" minOccurs="0"/>
 *         &lt;element name="Application" type="{http://www.alfresco.org/repo/audit/model/3.2}Application" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Audit", propOrder = {
    "dataExtractors",
    "dataGenerators",
    "pathMappings",
    "application"
})
public class Audit {

    @XmlElement(name = "DataExtractors")
    protected DataExtractors dataExtractors;
    @XmlElement(name = "DataGenerators")
    protected DataGenerators dataGenerators;
    @XmlElement(name = "PathMappings")
    protected PathMappings pathMappings;
    @XmlElement(name = "Application")
    protected List<Application> application;

    /**
     * Gets the value of the dataExtractors property.
     * 
     * @return
     *     possible object is
     *     {@link DataExtractors }
     *     
     */
    public DataExtractors getDataExtractors() {
        return dataExtractors;
    }

    /**
     * Sets the value of the dataExtractors property.
     * 
     * @param value
     *     allowed object is
     *     {@link DataExtractors }
     *     
     */
    public void setDataExtractors(DataExtractors value) {
        this.dataExtractors = value;
    }

    /**
     * Gets the value of the dataGenerators property.
     * 
     * @return
     *     possible object is
     *     {@link DataGenerators }
     *     
     */
    public DataGenerators getDataGenerators() {
        return dataGenerators;
    }

    /**
     * Sets the value of the dataGenerators property.
     * 
     * @param value
     *     allowed object is
     *     {@link DataGenerators }
     *     
     */
    public void setDataGenerators(DataGenerators value) {
        this.dataGenerators = value;
    }

    /**
     * Gets the value of the pathMappings property.
     * 
     * @return
     *     possible object is
     *     {@link PathMappings }
     *     
     */
    public PathMappings getPathMappings() {
        return pathMappings;
    }

    /**
     * Sets the value of the pathMappings property.
     * 
     * @param value
     *     allowed object is
     *     {@link PathMappings }
     *     
     */
    public void setPathMappings(PathMappings value) {
        this.pathMappings = value;
    }

    /**
     * Gets the value of the application property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the application property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getApplication().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Application }
     * 
     * 
     */
    public List<Application> getApplication() {
        if (application == null) {
            application = new ArrayList<Application>();
        }
        return this.application;
    }

}
