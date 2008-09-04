
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getPropertiesResponse element declaration.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;element name="getPropertiesResponse">
 *   &lt;complexType>
 *     &lt;complexContent>
 *       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *         &lt;sequence>
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
    "object"
})
@XmlRootElement(name = "getPropertiesResponse")
public class GetPropertiesResponse {

    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected DocumentFolderOrRelationshipObjectType object;

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
