
package org.alfresco.repo.audit.model._3;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for RecordValue complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RecordValue">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.alfresco.org/repo/audit/model/3.2}KeyedAuditDefinition">
 *       &lt;attribute name="dataExtractor" use="required" type="{http://www.alfresco.org/repo/audit/model/3.2}NameAttribute" />
 *       &lt;attribute name="dataSource" type="{http://www.alfresco.org/repo/audit/model/3.2}PathAttribute" />
 *       &lt;attribute name="dataTrigger" type="{http://www.alfresco.org/repo/audit/model/3.2}PathAttribute" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RecordValue")
public class RecordValue
    extends KeyedAuditDefinition
{

    @XmlAttribute(required = true)
    protected String dataExtractor;
    @XmlAttribute
    protected String dataSource;
    @XmlAttribute
    protected String dataTrigger;

    /**
     * Gets the value of the dataExtractor property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDataExtractor() {
        return dataExtractor;
    }

    /**
     * Sets the value of the dataExtractor property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDataExtractor(String value) {
        this.dataExtractor = value;
    }

    /**
     * Gets the value of the dataSource property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDataSource() {
        return dataSource;
    }

    /**
     * Sets the value of the dataSource property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDataSource(String value) {
        this.dataSource = value;
    }

    /**
     * Gets the value of the dataTrigger property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDataTrigger() {
        return dataTrigger;
    }

    /**
     * Sets the value of the dataTrigger property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDataTrigger(String value) {
        this.dataTrigger = value;
    }

}
