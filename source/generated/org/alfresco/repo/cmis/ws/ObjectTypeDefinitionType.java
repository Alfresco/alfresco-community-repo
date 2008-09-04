
package org.alfresco.repo.cmis.ws;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for objectTypeDefinitionType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="objectTypeDefinitionType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="objectTypeID" type="{http://www.cmis.org/ns/1.0}ID"/>
 *         &lt;element name="objectTypeQueryName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="objectTypeDisplayName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="parentTypeID" type="{http://www.cmis.org/ns/1.0}ID" minOccurs="0"/>
 *         &lt;element name="rootTypeQueryName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="creatable" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="fileable" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="queryable" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="controllable" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="versionable" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="contentStreamAllowed" type="{http://www.cmis.org/ns/1.0}contentStreamAllowedEnum"/>
 *         &lt;element name="allowedSourceType" type="{http://www.cmis.org/ns/1.0}ID" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="allowedTargetType" type="{http://www.cmis.org/ns/1.0}ID" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="property" type="{http://www.cmis.org/ns/1.0}propertyAttributesType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "objectTypeDefinitionType", propOrder = {
    "objectTypeID",
    "objectTypeQueryName",
    "objectTypeDisplayName",
    "parentTypeID",
    "rootTypeQueryName",
    "description",
    "creatable",
    "fileable",
    "queryable",
    "controllable",
    "versionable",
    "contentStreamAllowed",
    "allowedSourceType",
    "allowedTargetType",
    "property"
})
public class ObjectTypeDefinitionType {

    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected String objectTypeID;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected String objectTypeQueryName;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected String objectTypeDisplayName;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected String parentTypeID;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected String rootTypeQueryName;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected String description;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected boolean creatable;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected boolean fileable;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected boolean queryable;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected boolean controllable;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected boolean versionable;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected ContentStreamAllowedEnum contentStreamAllowed;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected List<String> allowedSourceType;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected List<String> allowedTargetType;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected List<PropertyAttributesType> property;

    /**
     * Gets the value of the objectTypeID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getObjectTypeID() {
        return objectTypeID;
    }

    /**
     * Sets the value of the objectTypeID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setObjectTypeID(String value) {
        this.objectTypeID = value;
    }

    /**
     * Gets the value of the objectTypeQueryName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getObjectTypeQueryName() {
        return objectTypeQueryName;
    }

    /**
     * Sets the value of the objectTypeQueryName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setObjectTypeQueryName(String value) {
        this.objectTypeQueryName = value;
    }

    /**
     * Gets the value of the objectTypeDisplayName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getObjectTypeDisplayName() {
        return objectTypeDisplayName;
    }

    /**
     * Sets the value of the objectTypeDisplayName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setObjectTypeDisplayName(String value) {
        this.objectTypeDisplayName = value;
    }

    /**
     * Gets the value of the parentTypeID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getParentTypeID() {
        return parentTypeID;
    }

    /**
     * Sets the value of the parentTypeID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setParentTypeID(String value) {
        this.parentTypeID = value;
    }

    /**
     * Gets the value of the rootTypeQueryName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRootTypeQueryName() {
        return rootTypeQueryName;
    }

    /**
     * Sets the value of the rootTypeQueryName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRootTypeQueryName(String value) {
        this.rootTypeQueryName = value;
    }

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Gets the value of the creatable property.
     * 
     */
    public boolean isCreatable() {
        return creatable;
    }

    /**
     * Sets the value of the creatable property.
     * 
     */
    public void setCreatable(boolean value) {
        this.creatable = value;
    }

    /**
     * Gets the value of the fileable property.
     * 
     */
    public boolean isFileable() {
        return fileable;
    }

    /**
     * Sets the value of the fileable property.
     * 
     */
    public void setFileable(boolean value) {
        this.fileable = value;
    }

    /**
     * Gets the value of the queryable property.
     * 
     */
    public boolean isQueryable() {
        return queryable;
    }

    /**
     * Sets the value of the queryable property.
     * 
     */
    public void setQueryable(boolean value) {
        this.queryable = value;
    }

    /**
     * Gets the value of the controllable property.
     * 
     */
    public boolean isControllable() {
        return controllable;
    }

    /**
     * Sets the value of the controllable property.
     * 
     */
    public void setControllable(boolean value) {
        this.controllable = value;
    }

    /**
     * Gets the value of the versionable property.
     * 
     */
    public boolean isVersionable() {
        return versionable;
    }

    /**
     * Sets the value of the versionable property.
     * 
     */
    public void setVersionable(boolean value) {
        this.versionable = value;
    }

    /**
     * Gets the value of the contentStreamAllowed property.
     * 
     * @return
     *     possible object is
     *     {@link ContentStreamAllowedEnum }
     *     
     */
    public ContentStreamAllowedEnum getContentStreamAllowed() {
        return contentStreamAllowed;
    }

    /**
     * Sets the value of the contentStreamAllowed property.
     * 
     * @param value
     *     allowed object is
     *     {@link ContentStreamAllowedEnum }
     *     
     */
    public void setContentStreamAllowed(ContentStreamAllowedEnum value) {
        this.contentStreamAllowed = value;
    }

    /**
     * Gets the value of the allowedSourceType property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the allowedSourceType property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAllowedSourceType().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getAllowedSourceType() {
        if (allowedSourceType == null) {
            allowedSourceType = new ArrayList<String>();
        }
        return this.allowedSourceType;
    }

    /**
     * Gets the value of the allowedTargetType property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the allowedTargetType property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAllowedTargetType().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getAllowedTargetType() {
        if (allowedTargetType == null) {
            allowedTargetType = new ArrayList<String>();
        }
        return this.allowedTargetType;
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
     * {@link PropertyAttributesType }
     * 
     * 
     */
    public List<PropertyAttributesType> getProperty() {
        if (property == null) {
            property = new ArrayList<PropertyAttributesType>();
        }
        return this.property;
    }

}
