
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
 *         &lt;element name="documentId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="major" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="properties" type="{http://docs.oasis-open.org/ns/cmis/core/200901}cmisPropertiesType" minOccurs="0"/>
 *         &lt;element name="contentStream" type="{http://docs.oasis-open.org/ns/cmis/core/200901}cmisContentStreamType" minOccurs="0"/>
 *         &lt;element name="checkinComment" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
    "documentId",
    "major",
    "properties",
    "contentStream",
    "checkinComment"
})
@XmlRootElement(name = "checkIn")
public class CheckIn {

    @XmlElement(required = true)
    protected String repositoryId;
    @XmlElement(required = true)
    protected String documentId;
    @XmlElementRef(name = "major", namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", type = JAXBElement.class)
    protected JAXBElement<Boolean> major;
    @XmlElementRef(name = "properties", namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", type = JAXBElement.class)
    protected JAXBElement<CmisPropertiesType> properties;
    @XmlElementRef(name = "contentStream", namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", type = JAXBElement.class)
    protected JAXBElement<CmisContentStreamType> contentStream;
    @XmlElementRef(name = "checkinComment", namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", type = JAXBElement.class)
    protected JAXBElement<String> checkinComment;

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
     * Gets the value of the documentId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDocumentId() {
        return documentId;
    }

    /**
     * Sets the value of the documentId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDocumentId(String value) {
        this.documentId = value;
    }

    /**
     * Gets the value of the major property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public JAXBElement<Boolean> getMajor() {
        return major;
    }

    /**
     * Sets the value of the major property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public void setMajor(JAXBElement<Boolean> value) {
        this.major = ((JAXBElement<Boolean> ) value);
    }

    /**
     * Gets the value of the properties property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link CmisPropertiesType }{@code >}
     *     
     */
    public JAXBElement<CmisPropertiesType> getProperties() {
        return properties;
    }

    /**
     * Sets the value of the properties property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link CmisPropertiesType }{@code >}
     *     
     */
    public void setProperties(JAXBElement<CmisPropertiesType> value) {
        this.properties = ((JAXBElement<CmisPropertiesType> ) value);
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
     * Gets the value of the checkinComment property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getCheckinComment() {
        return checkinComment;
    }

    /**
     * Sets the value of the checkinComment property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setCheckinComment(JAXBElement<String> value) {
        this.checkinComment = ((JAXBElement<String> ) value);
    }

}
