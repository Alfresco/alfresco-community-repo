
package org.alfresco.repo.cmis.ws;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for propertyAttributesType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="propertyAttributesType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="propertyName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="propertyId" type="{http://www.cmis.org/ns/1.0}ID"/>
 *         &lt;element name="displayName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="isInherited" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="propertyType" type="{http://www.cmis.org/ns/1.0}propertyTypeEnum"/>
 *         &lt;element name="cardinality" type="{http://www.cmis.org/ns/1.0}cardinalityEnum"/>
 *         &lt;element name="maximumLength" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="schemaURI" type="{http://www.w3.org/2001/XMLSchema}anyURI" minOccurs="0"/>
 *         &lt;element name="encoding" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="choice" type="{http://www.cmis.org/ns/1.0}choiceType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="openChoice" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="required" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="defaultValue" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="updatability" type="{http://www.cmis.org/ns/1.0}updatabilityEnum"/>
 *         &lt;element name="queryable" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="orderable" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "propertyAttributesType", propOrder = {
    "propertyName",
    "propertyId",
    "displayName",
    "description",
    "isInherited",
    "propertyType",
    "cardinality",
    "maximumLength",
    "schemaURI",
    "encoding",
    "openChoice",
    "required",
    "defaultValue",
    "updatability",
    "queryable",
    "orderable"
})
public class PropertyAttributesType {

    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected String propertyName;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected String propertyId;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected String displayName;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected String description;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected boolean isInherited;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected PropertyTypeEnum propertyType;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected CardinalityEnum cardinality;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected BigInteger maximumLength;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected String schemaURI;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected String encoding;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected Boolean openChoice;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected boolean required;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected String defaultValue;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected UpdatabilityEnum updatability;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected boolean queryable;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected boolean orderable;

    /**
     * Gets the value of the propertyName property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getPropertyName() {
        return propertyName;
    }

    /**
     * Sets the value of the propertyName property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setPropertyName(String value) {
        this.propertyName = value;
    }

    /**
     * Gets the value of the propertyId property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getPropertyId() {
        return propertyId;
    }

    /**
     * Sets the value of the propertyId property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setPropertyId(String value) {
        this.propertyId = value;
    }

    /**
     * Gets the value of the displayName property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the value of the displayName property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setDisplayName(String value) {
        this.displayName = value;
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
     * Gets the value of the isInherited property.
     *
     */
    public boolean isIsInherited() {
        return isInherited;
    }

    /**
     * Sets the value of the isInherited property.
     *
     */
    public void setIsInherited(boolean value) {
        this.isInherited = value;
    }

    /**
     * Gets the value of the propertyType property.
     *
     * @return
     *     possible object is
     *     {@link PropertyTypeEnum }
     *
     */
    public PropertyTypeEnum getPropertyType() {
        return propertyType;
    }

    /**
     * Sets the value of the propertyType property.
     *
     * @param value
     *     allowed object is
     *     {@link PropertyTypeEnum }
     *
     */
    public void setPropertyType(PropertyTypeEnum value) {
        this.propertyType = value;
    }

    /**
     * Gets the value of the cardinality property.
     *
     * @return
     *     possible object is
     *     {@link CardinalityEnum }
     *
     */
    public CardinalityEnum getCardinality() {
        return cardinality;
    }

    /**
     * Sets the value of the cardinality property.
     *
     * @param value
     *     allowed object is
     *     {@link CardinalityEnum }
     *
     */
    public void setCardinality(CardinalityEnum value) {
        this.cardinality = value;
    }

    /**
     * Gets the value of the maximumLength property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getMaximumLength() {
        return maximumLength;
    }

    /**
     * Sets the value of the maximumLength property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setMaximumLength(BigInteger value) {
        this.maximumLength = value;
    }

    /**
     * Gets the value of the schemaURI property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getSchemaURI() {
        return schemaURI;
    }

    /**
     * Sets the value of the schemaURI property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setSchemaURI(String value) {
        this.schemaURI = value;
    }

    /**
     * Gets the value of the encoding property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Sets the value of the encoding property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setEncoding(String value) {
        this.encoding = value;
    }

    /**
     * Gets the value of the openChoice property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isOpenChoice() {
        return openChoice;
    }

    /**
     * Sets the value of the openChoice property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setOpenChoice(Boolean value) {
        this.openChoice = value;
    }

    /**
     * Gets the value of the required property.
     *
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * Sets the value of the required property.
     *
     */
    public void setRequired(boolean value) {
        this.required = value;
    }

    /**
     * Gets the value of the defaultValue property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * Sets the value of the defaultValue property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setDefaultValue(String value) {
        this.defaultValue = value;
    }

    /**
     * Gets the value of the updatability property.
     *
     * @return
     *     possible object is
     *     {@link UpdatabilityEnum }
     *
     */
    public UpdatabilityEnum getUpdatability() {
        return updatability;
    }

    /**
     * Sets the value of the updatability property.
     *
     * @param value
     *     allowed object is
     *     {@link UpdatabilityEnum }
     *
     */
    public void setUpdatability(UpdatabilityEnum value) {
        this.updatability = value;
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
     * Gets the value of the orderable property.
     *
     */
    public boolean isOrderable() {
        return orderable;
    }

    /**
     * Sets the value of the orderable property.
     *
     */
    public void setOrderable(boolean value) {
        this.orderable = value;
    }

}
