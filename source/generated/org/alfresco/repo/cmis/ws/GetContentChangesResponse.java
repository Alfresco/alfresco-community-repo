
package org.alfresco.repo.cmis.ws;

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
 *         &lt;element name="objects" type="{http://docs.oasis-open.org/ns/cmis/messaging/200908/}cmisObjectListType"/>
 *         &lt;element name="changeLogToken" type="{http://www.w3.org/2001/XMLSchema}string"/>
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
    "objects",
    "changeLogToken"
})
@XmlRootElement(name = "getContentChangesResponse")
public class GetContentChangesResponse {

    @XmlElement(required = true)
    protected CmisObjectListType objects;
    @XmlElement(required = true)
    protected String changeLogToken;

    /**
     * Gets the value of the objects property.
     * 
     * @return
     *     possible object is
     *     {@link CmisObjectListType }
     *     
     */
    public CmisObjectListType getObjects() {
        return objects;
    }

    /**
     * Sets the value of the objects property.
     * 
     * @param value
     *     allowed object is
     *     {@link CmisObjectListType }
     *     
     */
    public void setObjects(CmisObjectListType value) {
        this.objects = value;
    }

    /**
     * Gets the value of the changeLogToken property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getChangeLogToken() {
        return changeLogToken;
    }

    /**
     * Sets the value of the changeLogToken property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setChangeLogToken(String value) {
        this.changeLogToken = value;
    }

}
