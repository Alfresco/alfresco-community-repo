
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for updateProperties element declaration.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;element name="updateProperties">
 *   &lt;complexType>
 *     &lt;complexContent>
 *       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *         &lt;sequence>
 *           &lt;element name="objectId" type="{http://www.cmis.org/ns/1.0}objectID"/>
 *           &lt;element name="changeToken" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *           &lt;element name="object" type="{http://www.cmis.org/ns/1.0}documentFolderOrRelationshipObjectType"/>
 *         &lt;/sequence>
 *       &lt;/restriction>
 *     &lt;/complexContent>
 *   &lt;/complexType>
 * &lt;/element>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "objectId",
    "changeToken",
    "object"
})
@XmlRootElement(name = "updateProperties")
public class UpdateProperties {

    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected String objectId;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected String changeToken;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected DocumentFolderOrRelationshipObjectType object;

    /**
     * Gets the value of the objectId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getObjectId() {
        return objectId;
    }

    /**
     * Sets the value of the objectId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setObjectId(String value) {
        this.objectId = value;
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

    /**
     * Gets the value of the object property.
     * 
     * @return
     *     possible object is
     *     {@link DocumentFolderOrRelationshipObjectType }
     *     
     */
    public DocumentFolderOrRelationshipObjectType getObject() {
        return object;
    }

    /**
     * Sets the value of the object property.
     * 
     * @param value
     *     allowed object is
     *     {@link DocumentFolderOrRelationshipObjectType }
     *     
     */
    public void setObject(DocumentFolderOrRelationshipObjectType value) {
        this.object = value;
    }

}
