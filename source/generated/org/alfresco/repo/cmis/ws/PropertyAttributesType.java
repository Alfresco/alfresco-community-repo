
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
 *         &lt;element name="displayName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="propertyType" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="cardinality" type="{http://www.cmis.org/ns/1.0}cardinalityEnum"/>
 *         &lt;element name="maximumLength" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="choice" type="{http://www.cmis.org/ns/1.0}choiceType" maxOccurs="unbounded" minOccurs="0"/>
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
    "displayName",
    "description",
    "propertyType",
    "cardinality",
    "maximumLength",
    "choice",
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
    protected String displayName;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected String description;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected String propertyType;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected CardinalityEnum cardinality;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected BigInteger maximumLength;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected List<ChoiceType> choice;
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
     * Gets the value of the propertyType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPropertyType() {
        return propertyType;
    }

    /**
     * Sets the value of the propertyType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPropertyType(String value) {
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
     * Gets the value of the choice property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the choice property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getChoice().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ChoiceType }
     * 
     * 
     */
    public List<ChoiceType> getChoice() {
        if (choice == null) {
            choice = new ArrayList<ChoiceType>();
        }
        return this.choice;
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
