
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
 *         &lt;element name="typeId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="properties" type="{http://docs.oasis-open.org/ns/cmis/core/200901}cmisPropertiesType"/>
 *         &lt;element name="folderId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="contentStream" type="{http://docs.oasis-open.org/ns/cmis/core/200901}cmisContentStreamType" minOccurs="0"/>
 *         &lt;element name="versioningState" type="{http://docs.oasis-open.org/ns/cmis/core/200901}enumVersioningState" minOccurs="0"/>
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
    "typeId",
    "properties",
    "folderId",
    "contentStream",
    "versioningState"
})
@XmlRootElement(name = "createDocument")
public class CreateDocument {

    @XmlElement(required = true)
    protected String repositoryId;
    @XmlElement(required = true)
    protected String typeId;
    @XmlElement(required = true)
    protected CmisPropertiesType properties;
    protected String folderId;
    @XmlElementRef(name = "contentStream", namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", type = JAXBElement.class)
    protected JAXBElement<CmisContentStreamType> contentStream;
    @XmlElementRef(name = "versioningState", namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", type = JAXBElement.class)
    protected JAXBElement<EnumVersioningState> versioningState;

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
     * Gets the value of the properties property.
     * 
     * @return
     *     possible object is
     *     {@link CmisPropertiesType }
     *     
     */
    public CmisPropertiesType getProperties() {
        return properties;
    }

    /**
     * Sets the value of the properties property.
     * 
     * @param value
     *     allowed object is
     *     {@link CmisPropertiesType }
     *     
     */
    public void setProperties(CmisPropertiesType value) {
        this.properties = value;
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
     *     {@link JAXBElement }{@code <}{@link CmisContentStreamType }{@code >}
     *     
     */
    public JAXBElement<CmisContentStreamType> getContentStream() {
        return contentStream;
    }

    /**
     * Sets the value of the contentStream property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link CmisContentStreamType }{@code >}
     *     
     */
    public void setContentStream(JAXBElement<CmisContentStreamType> value) {
        this.contentStream = ((JAXBElement<CmisContentStreamType> ) value);
    }

    /**
     * Gets the value of the versioningState property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link EnumVersioningState }{@code >}
     *     
     */
    public JAXBElement<EnumVersioningState> getVersioningState() {
        return versioningState;
    }

    /**
     * Sets the value of the versioningState property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link EnumVersioningState }{@code >}
     *     
     */
    public void setVersioningState(JAXBElement<EnumVersioningState> value) {
        this.versioningState = ((JAXBElement<EnumVersioningState> ) value);
    }

}
