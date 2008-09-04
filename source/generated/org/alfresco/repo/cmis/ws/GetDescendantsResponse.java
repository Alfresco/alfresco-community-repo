
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getDescendantsResponse element declaration.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;element name="getDescendantsResponse">
 *   &lt;complexType>
 *     &lt;complexContent>
 *       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *         &lt;sequence>
 *           &lt;element ref="{http://www.cmis.org/ns/1.0}documentAndFolderCollection"/>
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
    "documentAndFolderCollection"
})
@XmlRootElement(name = "getDescendantsResponse")
public class GetDescendantsResponse {

    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected DocumentAndFolderCollection documentAndFolderCollection;

    /**
     * Gets the value of the documentAndFolderCollection property.
     * 
     * @return
     *     possible object is
     *     {@link DocumentAndFolderCollection }
     *     
     */
    public DocumentAndFolderCollection getDocumentAndFolderCollection() {
        return documentAndFolderCollection;
    }

    /**
     * Sets the value of the documentAndFolderCollection property.
     * 
     * @param value
     *     allowed object is
     *     {@link DocumentAndFolderCollection }
     *     
     */
    public void setDocumentAndFolderCollection(DocumentAndFolderCollection value) {
        this.documentAndFolderCollection = value;
    }

}
