
package org.alfresco.repo.cmis.ws;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;
import org.w3c.dom.Element;


/**
 * <p>Java class for cmisPropertyDefinitionType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="cmisPropertyDefinitionType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="localName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="localNamespace" type="{http://www.w3.org/2001/XMLSchema}anyURI" minOccurs="0"/>
 *         &lt;element name="displayName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="queryName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="propertyType" type="{http://docs.oasis-open.org/ns/cmis/core/200908/}enumPropertyType"/>
 *         &lt;element name="cardinality" type="{http://docs.oasis-open.org/ns/cmis/core/200908/}enumCardinality"/>
 *         &lt;element name="updatability" type="{http://docs.oasis-open.org/ns/cmis/core/200908/}enumUpdatability"/>
 *         &lt;element name="inherited" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="required" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="queryable" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="orderable" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="openChoice" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;any/>
 *       &lt;/sequence>
 *       &lt;attGroup ref="{http://docs.oasis-open.org/ns/cmis/core/200908/}cmisUndefinedAttribute"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "cmisPropertyDefinitionType", namespace = "http://docs.oasis-open.org/ns/cmis/core/200908/", propOrder = {
    "id",
    "localName",
    "localNamespace",
    "displayName",
    "queryName",
    "description",
    "propertyType",
    "cardinality",
    "updatability",
    "inherited",
    "required",
    "queryable",
    "orderable",
    "openChoice",
    "any"
})
@XmlSeeAlso({
    CmisPropertyDateTimeDefinitionType.class,
    CmisPropertyUriDefinitionType.class,
    CmisPropertyDecimalDefinitionType.class,
    CmisPropertyIntegerDefinitionType.class,
    CmisPropertyBooleanDefinitionType.class,
    CmisPropertyHtmlDefinitionType.class,
    CmisPropertyIdDefinitionType.class,
    CmisPropertyStringDefinitionType.class
})
public class CmisPropertyDefinitionType {

    @XmlElement(required = true)
    protected String id;
    @XmlElement(required = true)
    protected String localName;
    @XmlSchemaType(name = "anyURI")
    protected String localNamespace;
    protected String displayName;
    protected String queryName;
    protected String description;
    @XmlElement(required = true)
    protected EnumPropertyType propertyType;
    @XmlElement(required = true)
    protected EnumCardinality cardinality;
    @XmlElement(required = true)
    protected EnumUpdatability updatability;
    protected Boolean inherited;
    protected boolean required;
    protected boolean queryable;
    protected boolean orderable;
    protected Boolean openChoice;
    @XmlAnyElement(lax = true)
    protected List<Object> any;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the localName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLocalName() {
        return localName;
    }

    /**
     * Sets the value of the localName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLocalName(String value) {
        this.localName = value;
    }

    /**
     * Gets the value of the localNamespace property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLocalNamespace() {
        return localNamespace;
    }

    /**
     * Sets the value of the localNamespace property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLocalNamespace(String value) {
        this.localNamespace = value;
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
     * Gets the value of the queryName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getQueryName() {
        return queryName;
    }

    /**
     * Sets the value of the queryName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setQueryName(String value) {
        this.queryName = value;
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
     *     {@link EnumPropertyType }
     *     
     */
    public EnumPropertyType getPropertyType() {
        return propertyType;
    }

    /**
     * Sets the value of the propertyType property.
     * 
     * @param value
     *     allowed object is
     *     {@link EnumPropertyType }
     *     
     */
    public void setPropertyType(EnumPropertyType value) {
        this.propertyType = value;
    }

    /**
     * Gets the value of the cardinality property.
     * 
     * @return
     *     possible object is
     *     {@link EnumCardinality }
     *     
     */
    public EnumCardinality getCardinality() {
        return cardinality;
    }

    /**
     * Sets the value of the cardinality property.
     * 
     * @param value
     *     allowed object is
     *     {@link EnumCardinality }
     *     
     */
    public void setCardinality(EnumCardinality value) {
        this.cardinality = value;
    }

    /**
     * Gets the value of the updatability property.
     * 
     * @return
     *     possible object is
     *     {@link EnumUpdatability }
     *     
     */
    public EnumUpdatability getUpdatability() {
        return updatability;
    }

    /**
     * Sets the value of the updatability property.
     * 
     * @param value
     *     allowed object is
     *     {@link EnumUpdatability }
     *     
     */
    public void setUpdatability(EnumUpdatability value) {
        this.updatability = value;
    }

    /**
     * Gets the value of the inherited property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isInherited() {
        return inherited;
    }

    /**
     * Sets the value of the inherited property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setInherited(Boolean value) {
        this.inherited = value;
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
     * Gets the value of the any property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the any property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAny().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * {@link Element }
     * 
     * 
     */
    public List<Object> getAny() {
        if (any == null) {
            any = new ArrayList<Object>();
        }
        return this.any;
    }

    /**
     * Gets a map that contains attributes that aren't bound to any typed property on this class.
     * 
     * <p>
     * the map is keyed by the name of the attribute and 
     * the value is the string value of the attribute.
     * 
     * the map returned by this method is live, and you can add new attribute
     * by updating the map directly. Because of this design, there's no setter.
     * 
     * 
     * @return
     *     always non-null
     */
    public Map<QName, String> getOtherAttributes() {
        return otherAttributes;
    }

}
