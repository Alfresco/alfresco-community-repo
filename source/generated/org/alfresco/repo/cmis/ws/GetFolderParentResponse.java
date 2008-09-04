
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getFolderParentResponse element declaration.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;element name="getFolderParentResponse">
 *   &lt;complexType>
 *     &lt;complexContent>
 *       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *         &lt;sequence>
 *           &lt;element name="ancestors" type="{http://www.cmis.org/ns/1.0}objectCollectionType"/>
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
    "ancestors"
})
@XmlRootElement(name = "getFolderParentResponse")
public class GetFolderParentResponse {

    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected ObjectCollectionType ancestors;

    /**
     * Gets the value of the ancestors property.
     * 
     * @return
     *     possible object is
     *     {@link ObjectCollectionType }
     *     
     */
    public ObjectCollectionType getAncestors() {
        return ancestors;
    }

    /**
     * Sets the value of the ancestors property.
     * 
     * @param value
     *     allowed object is
     *     {@link ObjectCollectionType }
     *     
     */
    public void setAncestors(ObjectCollectionType value) {
        this.ancestors = value;
    }

}
