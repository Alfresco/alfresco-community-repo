
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for deleteTree element declaration.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;element name="deleteTree">
 *   &lt;complexType>
 *     &lt;complexContent>
 *       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *         &lt;sequence>
 *           &lt;element name="repositoryId" type="{http://www.cmis.org/ns/1.0}ID"/>
 *           &lt;element name="folderId" type="{http://www.cmis.org/ns/1.0}ID"/>
 *           &lt;element name="unfileNonfolderObjects" type="{http://www.cmis.org/ns/1.0}unfileNonfolderObjectsEnum"/>
 *           &lt;element name="continueOnFailure" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
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
    "folderId",
    "unfileNonfolderObjects",
    "continueOnFailure"
})
@XmlRootElement(name = "deleteTree")
public class DeleteTree {

    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected String repositoryId;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected String folderId;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected UnfileNonfolderObjectsEnum unfileNonfolderObjects;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected Boolean continueOnFailure;

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
     * Gets the value of the folderId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFolderId() {
        return folderId;
    }

    /**
     * Sets the value of the folderId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFolderId(String value) {
        this.folderId = value;
    }

    /**
     * Gets the value of the unfileNonfolderObjects property.
     * 
     * @return
     *     possible object is
     *     {@link UnfileNonfolderObjectsEnum }
     *     
     */
    public UnfileNonfolderObjectsEnum getUnfileNonfolderObjects() {
        return unfileNonfolderObjects;
    }

    /**
     * Sets the value of the unfileNonfolderObjects property.
     * 
     * @param value
     *     allowed object is
     *     {@link UnfileNonfolderObjectsEnum }
     *     
     */
    public void setUnfileNonfolderObjects(UnfileNonfolderObjectsEnum value) {
        this.unfileNonfolderObjects = value;
    }

    /**
     * Gets the value of the continueOnFailure property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isContinueOnFailure() {
        return continueOnFailure;
    }

    /**
     * Sets the value of the continueOnFailure property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setContinueOnFailure(Boolean value) {
        this.continueOnFailure = value;
    }

}
