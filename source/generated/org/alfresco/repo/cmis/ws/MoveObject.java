
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
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
 *         &lt;element name="repositoryId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="objectId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="targetFolderId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="sourceFolderId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
    "repositoryId",
    "objectId",
    "targetFolderId",
    "sourceFolderId"
})
@XmlRootElement(name = "moveObject")
public class MoveObject {

    @XmlElement(required = true)
    protected String repositoryId;
    @XmlElement(required = true)
    protected String objectId;
    @XmlElement(required = true)
    protected String targetFolderId;
    @XmlElementRef(name = "sourceFolderId", namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", type = JAXBElement.class)
    protected JAXBElement<String> sourceFolderId;

    /**
     * Gets the value of the repositoryId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRepositoryId() {
        return repositoryId;
    }

    /**
     * Sets the value of the repositoryId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRepositoryId(String value) {
        this.repositoryId = value;
    }

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
     * Gets the value of the targetFolderId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTargetFolderId() {
        return targetFolderId;
    }

    /**
     * Sets the value of the targetFolderId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTargetFolderId(String value) {
        this.targetFolderId = value;
    }

    /**
     * Gets the value of the sourceFolderId property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getSourceFolderId() {
        return sourceFolderId;
    }

    /**
     * Sets the value of the sourceFolderId property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setSourceFolderId(JAXBElement<String> value) {
        this.sourceFolderId = ((JAXBElement<String> ) value);
    }

}
