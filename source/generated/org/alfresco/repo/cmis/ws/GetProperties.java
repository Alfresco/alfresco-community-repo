
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getProperties element declaration.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;element name="getProperties">
 *   &lt;complexType>
 *     &lt;complexContent>
 *       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *         &lt;sequence>
 *           &lt;element name="objectId" type="{http://www.cmis.org/ns/1.0}objectID"/>
 *           &lt;element name="returnVersion" type="{http://www.cmis.org/ns/1.0}versionEnum" minOccurs="0"/>
 *           &lt;element ref="{http://www.cmis.org/ns/1.0}filter" minOccurs="0"/>
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
    "objectId",
    "returnVersion",
    "filter"
})
@XmlRootElement(name = "getProperties")
public class GetProperties {

    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected String objectId;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected VersionEnum returnVersion;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected String filter;

    /**
     * Gets the value of the objectId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getObjectId() {
        return objectId;
    }

    /**
     * Sets the value of the objectId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setObjectId(String value) {
        this.objectId = value;
    }

    /**
     * Gets the value of the returnVersion property.
     * 
     * @return
     *     possible object is
     *     {@link VersionEnum }
     *     
     */
    public VersionEnum getReturnVersion() {
        return returnVersion;
    }

    /**
     * Sets the value of the returnVersion property.
     * 
     * @param value
     *     allowed object is
     *     {@link VersionEnum }
     *     
     */
    public void setReturnVersion(VersionEnum value) {
        this.returnVersion = value;
    }

    /**
     * Gets the value of the filter property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFilter() {
        return filter;
    }

    /**
     * Sets the value of the filter property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFilter(String value) {
        this.filter = value;
    }

}
