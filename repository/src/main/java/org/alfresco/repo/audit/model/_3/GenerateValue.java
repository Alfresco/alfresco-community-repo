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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for GenerateValue complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GenerateValue">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.alfresco.org/repo/audit/model/3.2}KeyedAuditDefinition">
 *       &lt;attribute name="dataGenerator" use="required" type="{http://www.alfresco.org/repo/audit/model/3.2}NameAttribute" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GenerateValue")
public class GenerateValue
    extends KeyedAuditDefinition
{

    @XmlAttribute(required = true)
    protected String dataGenerator;

    /**
     * Gets the value of the dataGenerator property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDataGenerator() {
        return dataGenerator;
    }

    /**
     * Sets the value of the dataGenerator property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDataGenerator(String value) {
        this.dataGenerator = value;
    }

}
