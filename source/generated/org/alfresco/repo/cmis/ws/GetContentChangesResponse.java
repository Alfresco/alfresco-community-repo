
package org.alfresco.repo.cmis.ws;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="changedObject" type="{http://docs.oasis-open.org/ns/cmis/core/200901}cmisObjectType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="changeToken" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "changedObject",
    "changeToken"
})
@XmlRootElement(name = "getContentChangesResponse")
public class GetContentChangesResponse {

    @XmlElement(nillable = true)
    protected List<CmisObjectType> changedObject;
    @XmlElement(required = true)
    protected String changeToken;

    /**
     * Gets the value of the changedObject property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the changedObject property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getChangedObject().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CmisObjectType }
     * 
     * 
     */
    public List<CmisObjectType> getChangedObject() {
        if (changedObject == null) {
            changedObject = new ArrayList<CmisObjectType>();
        }
        return this.changedObject;
    }

    /**
     * Gets the value of the changeToken property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getChangeToken() {
        return changeToken;
    }

    /**
     * Sets the value of the changeToken property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setChangeToken(String value) {
        this.changeToken = value;
    }

}
