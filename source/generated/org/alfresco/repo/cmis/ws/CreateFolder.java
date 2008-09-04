
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for createFolder element declaration.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;element name="createFolder">
 *   &lt;complexType>
 *     &lt;complexContent>
 *       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *         &lt;sequence>
 *           &lt;element name="typeId" type="{http://www.cmis.org/ns/1.0}ID"/>
 *           &lt;element name="propertyCollection" type="{http://www.cmis.org/ns/1.0}folderObjectType"/>
 *           &lt;element name="folderId" type="{http://www.cmis.org/ns/1.0}objectID"/>
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
    "folderId"
})
@XmlRootElement(name = "createFolder")
public class CreateFolder {

    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected String typeId;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected FolderObjectType propertyCollection;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected String folderId;

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
     *     {@link FolderObjectType }
     *     
     */
    public FolderObjectType getPropertyCollection() {
        return propertyCollection;
    }

    /**
     * Sets the value of the propertyCollection property.
     * 
     * @param value
     *     allowed object is
     *     {@link FolderObjectType }
     *     
     */
    public void setPropertyCollection(FolderObjectType value) {
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

}
