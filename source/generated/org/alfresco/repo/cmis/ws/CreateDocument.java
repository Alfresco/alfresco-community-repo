
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for createDocument element declaration.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;element name="createDocument">
 *   &lt;complexType>
 *     &lt;complexContent>
 *       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *         &lt;sequence>
 *           &lt;element name="typeId" type="{http://www.cmis.org/ns/1.0}ID"/>
 *           &lt;element name="propertyCollection" type="{http://www.cmis.org/ns/1.0}documentObjectType"/>
 *           &lt;element name="folderId" type="{http://www.cmis.org/ns/1.0}objectID" minOccurs="0"/>
 *           &lt;element ref="{http://www.cmis.org/ns/1.0}contentStream" minOccurs="0"/>
 *           &lt;element name="versioningState" type="{http://www.cmis.org/ns/1.0}versioningStateEnum" minOccurs="0"/>
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
    "typeId",
    "propertyCollection",
    "folderId",
    "contentStream",
    "versioningState"
})
@XmlRootElement(name = "createDocument")
public class CreateDocument {

    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected String typeId;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected DocumentObjectType propertyCollection;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected String folderId;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected ContentStream contentStream;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected VersioningStateEnum versioningState;

    /**
     * Gets the value of the typeId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTypeId() {
        return typeId;
    }

    /**
     * Sets the value of the typeId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTypeId(String value) {
        this.typeId = value;
    }

    /**
     * Gets the value of the propertyCollection property.
     * 
     * @return
     *     possible object is
     *     {@link DocumentObjectType }
     *     
     */
    public DocumentObjectType getPropertyCollection() {
        return propertyCollection;
    }

    /**
     * Sets the value of the propertyCollection property.
     * 
     * @param value
     *     allowed object is
     *     {@link DocumentObjectType }
     *     
     */
    public void setPropertyCollection(DocumentObjectType value) {
        this.propertyCollection = value;
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
     * Gets the value of the contentStream property.
     * 
     * @return
     *     possible object is
     *     {@link ContentStream }
     *     
     */
    public ContentStream getContentStream() {
        return contentStream;
    }

    /**
     * Sets the value of the contentStream property.
     * 
     * @param value
     *     allowed object is
     *     {@link ContentStream }
     *     
     */
    public void setContentStream(ContentStream value) {
        this.contentStream = value;
    }

    /**
     * Gets the value of the versioningState property.
     * 
     * @return
     *     possible object is
     *     {@link VersioningStateEnum }
     *     
     */
    public VersioningStateEnum getVersioningState() {
        return versioningState;
    }

    /**
     * Sets the value of the versioningState property.
     * 
     * @param value
     *     allowed object is
     *     {@link VersioningStateEnum }
     *     
     */
    public void setVersioningState(VersioningStateEnum value) {
        this.versioningState = value;
    }

}
