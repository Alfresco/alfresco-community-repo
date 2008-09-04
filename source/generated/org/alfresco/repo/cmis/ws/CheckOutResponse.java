
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for checkOutResponse element declaration.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;element name="checkOutResponse">
 *   &lt;complexType>
 *     &lt;complexContent>
 *       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *         &lt;sequence>
 *           &lt;element name="documentId" type="{http://www.cmis.org/ns/1.0}objectID"/>
 *           &lt;element name="contentCopied" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
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
    "contentCopied"
})
@XmlRootElement(name = "checkOutResponse")
public class CheckOutResponse {

    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected String documentId;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected boolean contentCopied;

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
     * Gets the value of the contentCopied property.
     * 
     */
    public boolean isContentCopied() {
        return contentCopied;
    }

    /**
     * Sets the value of the contentCopied property.
     * 
     */
    public void setContentCopied(boolean value) {
        this.contentCopied = value;
    }

}
