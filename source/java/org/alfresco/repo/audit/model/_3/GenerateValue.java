
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
 *       &lt;attribute name="scope" use="required" type="{http://www.alfresco.org/repo/audit/model/3.2}ScopeAttribute" />
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
    @XmlAttribute(required = true)
    protected ScopeAttribute scope;

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

    /**
     * Gets the value of the scope property.
     * 
     * @return
     *     possible object is
     *     {@link ScopeAttribute }
     *     
     */
    public ScopeAttribute getScope() {
        return scope;
    }

    /**
     * Sets the value of the scope property.
     * 
     * @param value
     *     allowed object is
     *     {@link ScopeAttribute }
     *     
     */
    public void setScope(ScopeAttribute value) {
        this.scope = value;
    }

}
