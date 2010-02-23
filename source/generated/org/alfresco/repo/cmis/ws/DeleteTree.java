
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
 *         &lt;element name="folderId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="allVersions" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="unfileObjects" type="{http://docs.oasis-open.org/ns/cmis/core/200908/}enumUnfileObject" minOccurs="0"/>
 *         &lt;element name="continueOnFailure" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="extension" type="{http://docs.oasis-open.org/ns/cmis/messaging/200908/}cmisExtensionType" minOccurs="0"/>
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
    "folderId",
    "allVersions",
    "unfileObjects",
    "continueOnFailure",
    "extension"
})
@XmlRootElement(name = "deleteTree")
public class DeleteTree {

    @XmlElement(required = true)
    protected String repositoryId;
    @XmlElement(required = true)
    protected String folderId;
    @XmlElementRef(name = "allVersions", namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", type = JAXBElement.class)
    protected JAXBElement<Boolean> allVersions;
    @XmlElementRef(name = "unfileObjects", namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", type = JAXBElement.class)
    protected JAXBElement<EnumUnfileObject> unfileObjects;
    @XmlElementRef(name = "continueOnFailure", namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", type = JAXBElement.class)
    protected JAXBElement<Boolean> continueOnFailure;
    @XmlElementRef(name = "extension", namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", type = JAXBElement.class)
    protected JAXBElement<CmisExtensionType> extension;

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
     * Gets the value of the allVersions property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public JAXBElement<Boolean> getAllVersions() {
        return allVersions;
    }

    /**
     * Sets the value of the allVersions property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public void setAllVersions(JAXBElement<Boolean> value) {
        this.allVersions = ((JAXBElement<Boolean> ) value);
    }

    /**
     * Gets the value of the unfileObjects property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link EnumUnfileObject }{@code >}
     *     
     */
    public JAXBElement<EnumUnfileObject> getUnfileObjects() {
        return unfileObjects;
    }

    /**
     * Sets the value of the unfileObjects property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link EnumUnfileObject }{@code >}
     *     
     */
    public void setUnfileObjects(JAXBElement<EnumUnfileObject> value) {
        this.unfileObjects = ((JAXBElement<EnumUnfileObject> ) value);
    }

    /**
     * Gets the value of the continueOnFailure property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public JAXBElement<Boolean> getContinueOnFailure() {
        return continueOnFailure;
    }

    /**
     * Sets the value of the continueOnFailure property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public void setContinueOnFailure(JAXBElement<Boolean> value) {
        this.continueOnFailure = ((JAXBElement<Boolean> ) value);
    }

    /**
     * Gets the value of the extension property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link CmisExtensionType }{@code >}
     *     
     */
    public JAXBElement<CmisExtensionType> getExtension() {
        return extension;
    }

    /**
     * Sets the value of the extension property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link CmisExtensionType }{@code >}
     *     
     */
    public void setExtension(JAXBElement<CmisExtensionType> value) {
        this.extension = ((JAXBElement<CmisExtensionType> ) value);
    }

}
