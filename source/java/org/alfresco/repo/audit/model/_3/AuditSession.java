
package org.alfresco.repo.audit.model._3;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for AuditSession complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AuditSession">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="GenerateValue" type="{http://www.alfresco.org/repo/audit/model/3.2}GenerateValue" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AuditSession", propOrder = {
    "generateValue"
})
public class AuditSession {

    @XmlElement(name = "GenerateValue")
    protected List<GenerateValue> generateValue;

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

}
