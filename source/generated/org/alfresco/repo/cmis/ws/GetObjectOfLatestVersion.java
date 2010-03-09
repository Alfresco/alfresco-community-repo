
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="repositoryId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="objectId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="major" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="filter" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="includeAllowableActions" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="includeRelationships" type="{http://docs.oasis-open.org/ns/cmis/core/200908/}enumIncludeRelationships" minOccurs="0"/>
 *         &lt;element name="renditionFilter" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="includePolicyIds" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="includeACL" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="extension" type="{http://docs.oasis-open.org/ns/cmis/messaging/200908/}cmisExtensionType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "repositoryId",
    "objectId",
    "major",
    "filter",
    "includeAllowableActions",
    "includeRelationships",
    "renditionFilter",
    "includePolicyIds",
    "includeACL",
    "extension"
})
@XmlRootElement(name = "getObjectOfLatestVersion")
public class GetObjectOfLatestVersion {

    @XmlElement(required = true)
    protected String repositoryId;
    @XmlElement(required = true)
    protected String objectId;
    protected Boolean major;
    @XmlElementRef(name = "filter", namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", type = JAXBElement.class)
    protected JAXBElement<String> filter;
    @XmlElementRef(name = "includeAllowableActions", namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", type = JAXBElement.class)
    protected JAXBElement<Boolean> includeAllowableActions;
    @XmlElementRef(name = "includeRelationships", namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", type = JAXBElement.class)
    protected JAXBElement<EnumIncludeRelationships> includeRelationships;
    @XmlElementRef(name = "renditionFilter", namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", type = JAXBElement.class)
    protected JAXBElement<String> renditionFilter;
    @XmlElementRef(name = "includePolicyIds", namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", type = JAXBElement.class)
    protected JAXBElement<Boolean> includePolicyIds;
    @XmlElementRef(name = "includeACL", namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", type = JAXBElement.class)
    protected JAXBElement<Boolean> includeACL;
    @XmlElementRef(name = "extension", namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", type = JAXBElement.class)
    protected JAXBElement<CmisExtensionType> extension;

    /**
     * Gets the value of the repositoryId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRepositoryId() {
        return repositoryId;
    }

    /**
     * Sets the value of the repositoryId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRepositoryId(String value) {
        this.repositoryId = value;
    }

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
     * Gets the value of the filter property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getFilter() {
        return filter;
    }

    /**
     * Sets the value of the filter property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setFilter(JAXBElement<String> value) {
        this.filter = ((JAXBElement<String> ) value);
    }

    /**
     * Gets the value of the includeAllowableActions property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public JAXBElement<Boolean> getIncludeAllowableActions() {
        return includeAllowableActions;
    }

    /**
     * Sets the value of the includeAllowableActions property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public void setIncludeAllowableActions(JAXBElement<Boolean> value) {
        this.includeAllowableActions = ((JAXBElement<Boolean> ) value);
    }

    /**
     * Gets the value of the includeRelationships property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link EnumIncludeRelationships }{@code >}
     *     
     */
    public JAXBElement<EnumIncludeRelationships> getIncludeRelationships() {
        return includeRelationships;
    }

    /**
     * Sets the value of the includeRelationships property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link EnumIncludeRelationships }{@code >}
     *     
     */
    public void setIncludeRelationships(JAXBElement<EnumIncludeRelationships> value) {
        this.includeRelationships = ((JAXBElement<EnumIncludeRelationships> ) value);
    }

    /**
     * Gets the value of the renditionFilter property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getRenditionFilter() {
        return renditionFilter;
    }

    /**
     * Sets the value of the renditionFilter property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setRenditionFilter(JAXBElement<String> value) {
        this.renditionFilter = ((JAXBElement<String> ) value);
    }

    /**
     * Gets the value of the includePolicyIds property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public JAXBElement<Boolean> getIncludePolicyIds() {
        return includePolicyIds;
    }

    /**
     * Sets the value of the includePolicyIds property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public void setIncludePolicyIds(JAXBElement<Boolean> value) {
        this.includePolicyIds = ((JAXBElement<Boolean> ) value);
    }

    /**
     * Gets the value of the includeACL property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public JAXBElement<Boolean> getIncludeACL() {
        return includeACL;
    }

    /**
     * Sets the value of the includeACL property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public void setIncludeACL(JAXBElement<Boolean> value) {
        this.includeACL = ((JAXBElement<Boolean> ) value);
    }

    /**
     * Gets the value of the extension property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link CmisExtensionType }{@code >}
     *     
     */
    public JAXBElement<CmisExtensionType> getExtension() {
        return extension;
    }

    /**
     * Sets the value of the extension property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link CmisExtensionType }{@code >}
     *     
     */
    public void setExtension(JAXBElement<CmisExtensionType> value) {
        this.extension = ((JAXBElement<CmisExtensionType> ) value);
    }

}
