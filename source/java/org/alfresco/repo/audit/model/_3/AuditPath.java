
package org.alfresco.repo.audit.model._3;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for AuditPath complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AuditPath">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.alfresco.org/repo/audit/model/3.2}KeyedAuditDefinition">
 *       &lt;sequence>
 *         &lt;element name="RecordValue" type="{http://www.alfresco.org/repo/audit/model/3.2}RecordValue" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="GenerateValue" type="{http://www.alfresco.org/repo/audit/model/3.2}GenerateValue" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="AuditPath" type="{http://www.alfresco.org/repo/audit/model/3.2}AuditPath" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AuditPath", propOrder = {
    "recordValue",
    "generateValue",
    "auditPath"
})
@XmlSeeAlso({
    Application.class
})
public class AuditPath
    extends KeyedAuditDefinition
{

    @XmlElement(name = "RecordValue")
    protected List<RecordValue> recordValue;
    @XmlElement(name = "GenerateValue")
    protected List<GenerateValue> generateValue;
    @XmlElement(name = "AuditPath")
    protected List<AuditPath> auditPath;

    /**
     * Gets the value of the recordValue property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the recordValue property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRecordValue().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link RecordValue }
     * 
     * 
     */
    public List<RecordValue> getRecordValue() {
        if (recordValue == null) {
            recordValue = new ArrayList<RecordValue>();
        }
        return this.recordValue;
    }

    /**
     * Gets the value of the generateValue property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the generateValue property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getGenerateValue().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link GenerateValue }
     * 
     * 
     */
    public List<GenerateValue> getGenerateValue() {
        if (generateValue == null) {
            generateValue = new ArrayList<GenerateValue>();
        }
        return this.generateValue;
    }

    /**
     * Gets the value of the auditPath property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the auditPath property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAuditPath().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AuditPath }
     * 
     * 
     */
    public List<AuditPath> getAuditPath() {
        if (auditPath == null) {
            auditPath = new ArrayList<AuditPath>();
        }
        return this.auditPath;
    }

}
