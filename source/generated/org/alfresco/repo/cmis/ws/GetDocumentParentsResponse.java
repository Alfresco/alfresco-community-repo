
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getDocumentParentsResponse element declaration.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;element name="getDocumentParentsResponse">
 *   &lt;complexType>
 *     &lt;complexContent>
 *       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *         &lt;sequence>
 *           &lt;element ref="{http://www.cmis.org/ns/1.0}folderCollection"/>
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
    "folderCollection"
})
@XmlRootElement(name = "getDocumentParentsResponse")
public class GetDocumentParentsResponse {

    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected FolderCollection folderCollection;

    /**
     * Gets the value of the folderCollection property.
     * 
     * @return
     *     possible object is
     *     {@link FolderCollection }
     *     
     */
    public FolderCollection getFolderCollection() {
        return folderCollection;
    }

    /**
     * Sets the value of the folderCollection property.
     * 
     * @param value
     *     allowed object is
     *     {@link FolderCollection }
     *     
     */
    public void setFolderCollection(FolderCollection value) {
        this.folderCollection = value;
    }

}
