
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for checkIn element declaration.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;element name="checkIn">
 *   &lt;complexType>
 *     &lt;complexContent>
 *       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *         &lt;sequence>
 *           &lt;element name="documentId" type="{http://www.cmis.org/ns/1.0}objectID"/>
 *           &lt;element name="major" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *           &lt;element name="properties" type="{http://www.cmis.org/ns/1.0}documentObjectType" minOccurs="0"/>
 *           &lt;element ref="{http://www.cmis.org/ns/1.0}contentStream" minOccurs="0"/>
 *           &lt;element name="checkinComment" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
    "documentId",
    "major",
    "properties",
    "contentStream",
    "checkinComment"
})
@XmlRootElement(name = "checkIn")
public class CheckIn {

    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected String documentId;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected Boolean major;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected DocumentObjectType properties;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected ContentStream contentStream;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected String checkinComment;

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
     *     {@link Boolean }
     *     
     */
    public Boolean isMajor() {
        return major;
    }

    /**
     * Sets the value of the major property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setMajor(Boolean value) {
        this.major = value;
    }

    /**
     * Gets the value of the properties property.
     * 
     * @return
     *     possible object is
     *     {@link DocumentObjectType }
     *     
     */
    public DocumentObjectType getProperties() {
        return properties;
    }

    /**
     * Sets the value of the properties property.
     * 
     * @param value
     *     allowed object is
     *     {@link DocumentObjectType }
     *     
     */
    public void setProperties(DocumentObjectType value) {
        this.properties = value;
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
     * Gets the value of the checkinComment property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCheckinComment() {
        return checkinComment;
    }

    /**
     * Sets the value of the checkinComment property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCheckinComment(String value) {
        this.checkinComment = value;
    }

}
