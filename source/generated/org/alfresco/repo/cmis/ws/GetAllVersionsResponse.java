
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getAllVersionsResponse element declaration.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;element name="getAllVersionsResponse">
 *   &lt;complexType>
 *     &lt;complexContent>
 *       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *         &lt;sequence>
 *           &lt;element name="versions" type="{http://www.cmis.org/ns/1.0}objectCollectionType"/>
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
    "versions"
})
@XmlRootElement(name = "getAllVersionsResponse")
public class GetAllVersionsResponse {

    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected ObjectCollectionType versions;

    /**
     * Gets the value of the versions property.
     * 
     * @return
     *     possible object is
     *     {@link ObjectCollectionType }
     *     
     */
    public ObjectCollectionType getVersions() {
        return versions;
    }

    /**
     * Sets the value of the versions property.
     * 
     * @param value
     *     allowed object is
     *     {@link ObjectCollectionType }
     *     
     */
    public void setVersions(ObjectCollectionType value) {
        this.versions = value;
    }

}
