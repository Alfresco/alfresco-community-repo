
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getUnfiledDocsResponse element declaration.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;element name="getUnfiledDocsResponse">
 *   &lt;complexType>
 *     &lt;complexContent>
 *       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *         &lt;sequence>
 *           &lt;element ref="{http://www.cmis.org/ns/1.0}documentCollection"/>
 *           &lt;element ref="{http://www.cmis.org/ns/1.0}hasMoreItems"/>
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
    "documentCollection",
    "hasMoreItems"
})
@XmlRootElement(name = "getUnfiledDocsResponse")
public class GetUnfiledDocsResponse {

    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected DocumentCollection documentCollection;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected boolean hasMoreItems;

    /**
     * Gets the value of the documentCollection property.
     * 
     * @return
     *     possible object is
     *     {@link DocumentCollection }
     *     
     */
    public DocumentCollection getDocumentCollection() {
        return documentCollection;
    }

    /**
     * Sets the value of the documentCollection property.
     * 
     * @param value
     *     allowed object is
     *     {@link DocumentCollection }
     *     
     */
    public void setDocumentCollection(DocumentCollection value) {
        this.documentCollection = value;
    }

    /**
     * Gets the value of the hasMoreItems property.
     * 
     */
    public boolean isHasMoreItems() {
        return hasMoreItems;
    }

    /**
     * Sets the value of the hasMoreItems property.
     * 
     */
    public void setHasMoreItems(boolean value) {
        this.hasMoreItems = value;
    }

}
