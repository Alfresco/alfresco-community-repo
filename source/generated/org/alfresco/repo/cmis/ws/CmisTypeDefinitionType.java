
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
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;
import org.w3c.dom.Element;


/**
 * <p>Java class for cmisTypeDefinitionType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="cmisTypeDefinitionType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="localName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="localNamespace" type="{http://www.w3.org/2001/XMLSchema}anyURI" minOccurs="0"/>
 *         &lt;element name="displayName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="queryName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="baseTypeId" type="{http://docs.oasis-open.org/ns/cmis/core/200901}enumBaseObjectTypeIds"/>
 *         &lt;element name="parentId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="creatable" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="fileable" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="queryable" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="fulltextindexed" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="includedInSupertypeQuery" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="controllablePolicy" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="controllableACL" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element name="propertyBooleanDefinition" type="{http://docs.oasis-open.org/ns/cmis/core/200901}cmisPropertyBooleanDefinitionType"/>
 *           &lt;element name="propertyDateTimeDefinition" type="{http://docs.oasis-open.org/ns/cmis/core/200901}cmisPropertyDateTimeDefinitionType"/>
 *           &lt;element name="propertyDecimalDefinition" type="{http://docs.oasis-open.org/ns/cmis/core/200901}cmisPropertyDecimalDefinitionType"/>
 *           &lt;element name="propertyIdDefinition" type="{http://docs.oasis-open.org/ns/cmis/core/200901}cmisPropertyIdDefinitionType"/>
 *           &lt;element name="propertyIntegerDefinition" type="{http://docs.oasis-open.org/ns/cmis/core/200901}cmisPropertyIntegerDefinitionType"/>
 *           &lt;element name="propertyHtmlDefinition" type="{http://docs.oasis-open.org/ns/cmis/core/200901}cmisPropertyHtmlDefinitionType"/>
 *           &lt;element name="propertyXhtmlDefinition" type="{http://docs.oasis-open.org/ns/cmis/core/200901}cmisPropertyXhtmlDefinitionType"/>
 *           &lt;element name="propertyStringDefinition" type="{http://docs.oasis-open.org/ns/cmis/core/200901}cmisPropertyStringDefinitionType"/>
 *           &lt;element name="propertyXmlDefinition" type="{http://docs.oasis-open.org/ns/cmis/core/200901}cmisPropertyXmlDefinitionType"/>
 *           &lt;element name="propertyUriDefinition" type="{http://docs.oasis-open.org/ns/cmis/core/200901}cmisPropertyUriDefinitionType"/>
 *         &lt;/choice>
 *         &lt;any/>
 *       &lt;/sequence>
 *       &lt;attGroup ref="{http://docs.oasis-open.org/ns/cmis/core/200901}cmisUndefinedAttribute"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "cmisTypeDefinitionType", namespace = "http://docs.oasis-open.org/ns/cmis/core/200901", propOrder = {
    "id",
    "localName",
    "localNamespace",
    "displayName",
    "queryName",
    "description",
    "baseTypeId",
    "parentId",
    "creatable",
    "fileable",
    "queryable",
    "fulltextindexed",
    "includedInSupertypeQuery",
    "controllablePolicy",
    "controllableACL",
    "propertyDefinition",
    "any"
})
@XmlSeeAlso({
    CmisTypeDocumentDefinitionType.class,
    CmisTypePolicyDefinitionType.class,
    CmisTypeFolderDefinitionType.class,
    CmisTypeRelationshipDefinitionType.class
})
public class CmisTypeDefinitionType {

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
    protected EnumBaseObjectTypeIds baseTypeId;
    protected String parentId;
    protected boolean creatable;
    protected boolean fileable;
    protected boolean queryable;
    protected boolean fulltextindexed;
    @XmlElement(defaultValue = "true")
    protected boolean includedInSupertypeQuery;
    protected boolean controllablePolicy;
    protected boolean controllableACL;
    @XmlElements({
        @XmlElement(name = "propertyHtmlDefinition", type = CmisPropertyHtmlDefinitionType.class),
        @XmlElement(name = "propertyXmlDefinition", type = CmisPropertyXmlDefinitionType.class),
        @XmlElement(name = "propertyDateTimeDefinition", type = CmisPropertyDateTimeDefinitionType.class),
        @XmlElement(name = "propertyUriDefinition", type = CmisPropertyUriDefinitionType.class),
        @XmlElement(name = "propertyStringDefinition", type = CmisPropertyStringDefinitionType.class),
        @XmlElement(name = "propertyIntegerDefinition", type = CmisPropertyIntegerDefinitionType.class),
        @XmlElement(name = "propertyIdDefinition", type = CmisPropertyIdDefinitionType.class),
        @XmlElement(name = "propertyXhtmlDefinition", type = CmisPropertyXhtmlDefinitionType.class),
        @XmlElement(name = "propertyDecimalDefinition", type = CmisPropertyDecimalDefinitionType.class),
        @XmlElement(name = "propertyBooleanDefinition", type = CmisPropertyBooleanDefinitionType.class)
    })
    protected List<CmisPropertyDefinitionType> propertyDefinition;
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
     * Gets the value of the baseTypeId property.
     * 
     * @return
     *     possible object is
     *     {@link EnumBaseObjectTypeIds }
     *     
     */
    public EnumBaseObjectTypeIds getBaseTypeId() {
        return baseTypeId;
    }

    /**
     * Sets the value of the baseTypeId property.
     * 
     * @param value
     *     allowed object is
     *     {@link EnumBaseObjectTypeIds }
     *     
     */
    public void setBaseTypeId(EnumBaseObjectTypeIds value) {
        this.baseTypeId = value;
    }

    /**
     * Gets the value of the parentId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getParentId() {
        return parentId;
    }

    /**
     * Sets the value of the parentId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setParentId(String value) {
        this.parentId = value;
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
     * Gets the value of the fulltextindexed property.
     * 
     */
    public boolean isFulltextindexed() {
        return fulltextindexed;
    }

    /**
     * Sets the value of the fulltextindexed property.
     * 
     */
    public void setFulltextindexed(boolean value) {
        this.fulltextindexed = value;
    }

    /**
     * Gets the value of the includedInSupertypeQuery property.
     * 
     */
    public boolean isIncludedInSupertypeQuery() {
        return includedInSupertypeQuery;
    }

    /**
     * Sets the value of the includedInSupertypeQuery property.
     * 
     */
    public void setIncludedInSupertypeQuery(boolean value) {
        this.includedInSupertypeQuery = value;
    }

    /**
     * Gets the value of the controllablePolicy property.
     * 
     */
    public boolean isControllablePolicy() {
        return controllablePolicy;
    }

    /**
     * Sets the value of the controllablePolicy property.
     * 
     */
    public void setControllablePolicy(boolean value) {
        this.controllablePolicy = value;
    }

    /**
     * Gets the value of the controllableACL property.
     * 
     */
    public boolean isControllableACL() {
        return controllableACL;
    }

    /**
     * Sets the value of the controllableACL property.
     * 
     */
    public void setControllableACL(boolean value) {
        this.controllableACL = value;
    }

    /**
     * Gets the value of the propertyDefinition property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the propertyDefinition property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPropertyDefinition().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CmisPropertyHtmlDefinitionType }
     * {@link CmisPropertyXmlDefinitionType }
     * {@link CmisPropertyDateTimeDefinitionType }
     * {@link CmisPropertyUriDefinitionType }
     * {@link CmisPropertyStringDefinitionType }
     * {@link CmisPropertyIntegerDefinitionType }
     * {@link CmisPropertyIdDefinitionType }
     * {@link CmisPropertyXhtmlDefinitionType }
     * {@link CmisPropertyDecimalDefinitionType }
     * {@link CmisPropertyBooleanDefinitionType }
     * 
     * 
     */
    public List<CmisPropertyDefinitionType> getPropertyDefinition() {
        if (propertyDefinition == null) {
            propertyDefinition = new ArrayList<CmisPropertyDefinitionType>();
        }
        return this.propertyDefinition;
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
     * {@link Element }
     * {@link Object }
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
