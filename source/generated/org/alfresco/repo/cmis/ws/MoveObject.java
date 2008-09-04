
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for moveObject element declaration.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;element name="moveObject">
 *   &lt;complexType>
 *     &lt;complexContent>
 *       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *         &lt;sequence>
 *           &lt;element name="repositoryId" type="{http://www.cmis.org/ns/1.0}ID"/>
 *           &lt;element name="objectId" type="{http://www.cmis.org/ns/1.0}ID"/>
 *           &lt;element name="targetFolderId" type="{http://www.cmis.org/ns/1.0}ID"/>
 *           &lt;element name="sourceFolderId" type="{http://www.cmis.org/ns/1.0}ID" minOccurs="0"/>
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
    "repositoryId",
    "objectId",
    "targetFolderId",
    "sourceFolderId"
})
@XmlRootElement(name = "moveObject")
public class MoveObject {

    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected String repositoryId;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected String objectId;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected String targetFolderId;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected String sourceFolderId;

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
     *     {@link String }
     *     
     */
    public String getSourceFolderId() {
        return sourceFolderId;
    }

    /**
     * Sets the value of the sourceFolderId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSourceFolderId(String value) {
        this.sourceFolderId = value;
    }

}
