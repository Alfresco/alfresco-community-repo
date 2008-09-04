
package org.alfresco.repo.cmis.ws;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for documentObjectType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="documentObjectType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.cmis.org/ns/1.0}objectTypeBase">
 *       &lt;sequence>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="isImmutable" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="isLatestVersion" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="isMajorVersion" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="isLatestMajorVersion" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="versionSeriesIsCheckedOut" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="versionSeriesCheckedOutBy" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="versionSeriesCheckedOutOID" type="{http://www.cmis.org/ns/1.0}objectID" minOccurs="0"/>
 *         &lt;element name="checkinComment" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="contentStreamLength" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="contentStreamMimeType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="contentStreamFilename" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="contentStreamURI" type="{http://www.w3.org/2001/XMLSchema}anyURI" minOccurs="0"/>
 *         &lt;element ref="{http://www.cmis.org/ns/1.0}property" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "documentObjectType", propOrder = {
    "name",
    "isImmutable",
    "isLatestVersion",
    "isMajorVersion",
    "isLatestMajorVersion",
    "versionSeriesIsCheckedOut",
    "versionSeriesCheckedOutBy",
    "versionSeriesCheckedOutOID",
    "checkinComment",
    "contentStreamLength",
    "contentStreamMimeType",
    "contentStreamFilename",
    "contentStreamURI",
    "property"
})
public class DocumentObjectType
    extends ObjectTypeBase
{

    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected String name;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected Boolean isImmutable;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected Boolean isLatestVersion;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected Boolean isMajorVersion;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected Boolean isLatestMajorVersion;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected Boolean versionSeriesIsCheckedOut;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected String versionSeriesCheckedOutBy;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected String versionSeriesCheckedOutOID;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected String checkinComment;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected BigInteger contentStreamLength;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected String contentStreamMimeType;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected String contentStreamFilename;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected String contentStreamURI;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected List<Property> property;

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the isImmutable property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsImmutable() {
        return isImmutable;
    }

    /**
     * Sets the value of the isImmutable property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsImmutable(Boolean value) {
        this.isImmutable = value;
    }

    /**
     * Gets the value of the isLatestVersion property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsLatestVersion() {
        return isLatestVersion;
    }

    /**
     * Sets the value of the isLatestVersion property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsLatestVersion(Boolean value) {
        this.isLatestVersion = value;
    }

    /**
     * Gets the value of the isMajorVersion property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsMajorVersion() {
        return isMajorVersion;
    }

    /**
     * Sets the value of the isMajorVersion property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsMajorVersion(Boolean value) {
        this.isMajorVersion = value;
    }

    /**
     * Gets the value of the isLatestMajorVersion property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsLatestMajorVersion() {
        return isLatestMajorVersion;
    }

    /**
     * Sets the value of the isLatestMajorVersion property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsLatestMajorVersion(Boolean value) {
        this.isLatestMajorVersion = value;
    }

    /**
     * Gets the value of the versionSeriesIsCheckedOut property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isVersionSeriesIsCheckedOut() {
        return versionSeriesIsCheckedOut;
    }

    /**
     * Sets the value of the versionSeriesIsCheckedOut property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setVersionSeriesIsCheckedOut(Boolean value) {
        this.versionSeriesIsCheckedOut = value;
    }

    /**
     * Gets the value of the versionSeriesCheckedOutBy property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVersionSeriesCheckedOutBy() {
        return versionSeriesCheckedOutBy;
    }

    /**
     * Sets the value of the versionSeriesCheckedOutBy property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVersionSeriesCheckedOutBy(String value) {
        this.versionSeriesCheckedOutBy = value;
    }

    /**
     * Gets the value of the versionSeriesCheckedOutOID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVersionSeriesCheckedOutOID() {
        return versionSeriesCheckedOutOID;
    }

    /**
     * Sets the value of the versionSeriesCheckedOutOID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVersionSeriesCheckedOutOID(String value) {
        this.versionSeriesCheckedOutOID = value;
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

    /**
     * Gets the value of the contentStreamLength property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getContentStreamLength() {
        return contentStreamLength;
    }

    /**
     * Sets the value of the contentStreamLength property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setContentStreamLength(BigInteger value) {
        this.contentStreamLength = value;
    }

    /**
     * Gets the value of the contentStreamMimeType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getContentStreamMimeType() {
        return contentStreamMimeType;
    }

    /**
     * Sets the value of the contentStreamMimeType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setContentStreamMimeType(String value) {
        this.contentStreamMimeType = value;
    }

    /**
     * Gets the value of the contentStreamFilename property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getContentStreamFilename() {
        return contentStreamFilename;
    }

    /**
     * Sets the value of the contentStreamFilename property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setContentStreamFilename(String value) {
        this.contentStreamFilename = value;
    }

    /**
     * Gets the value of the contentStreamURI property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getContentStreamURI() {
        return contentStreamURI;
    }

    /**
     * Sets the value of the contentStreamURI property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setContentStreamURI(String value) {
        this.contentStreamURI = value;
    }

    /**
     * Gets the value of the property property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the property property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getProperty().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Property }
     * 
     * 
     */
    public List<Property> getProperty() {
        if (property == null) {
            property = new ArrayList<Property>();
        }
        return this.property;
    }

}
