
package org.alfresco.repo.cmis.ws;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;
import org.w3c.dom.Element;


/**
 * <p>Java class for cmisObjectType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="cmisObjectType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="properties" type="{http://docs.oasis-open.org/ns/cmis/core/200901}cmisPropertiesType" minOccurs="0"/>
 *         &lt;element ref="{http://docs.oasis-open.org/ns/cmis/core/200901}allowableActions" minOccurs="0"/>
 *         &lt;element name="relationship" type="{http://docs.oasis-open.org/ns/cmis/core/200901}cmisObjectType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="changeObject" type="{http://docs.oasis-open.org/ns/cmis/core/200901}cmisChangedObjectType" minOccurs="0"/>
 *         &lt;element name="child" type="{http://docs.oasis-open.org/ns/cmis/core/200901}cmisObjectType" maxOccurs="unbounded" minOccurs="0"/>
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
@XmlType(name = "cmisObjectType", namespace = "http://docs.oasis-open.org/ns/cmis/core/200901", propOrder = {
    "properties",
    "allowableActions",
    "relationship",
    "changeObject",
    "child",
    "any"
})
public class CmisObjectType {

    protected CmisPropertiesType properties;
    protected CmisAllowableActionsType allowableActions;
    protected List<CmisObjectType> relationship;
    protected CmisChangedObjectType changeObject;
    protected List<CmisObjectType> child;
    @XmlAnyElement(lax = true)
    protected List<Object> any;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();

    /**
     * Gets the value of the properties property.
     * 
     * @return
     *     possible object is
     *     {@link CmisPropertiesType }
     *     
     */
    public CmisPropertiesType getProperties() {
        return properties;
    }

    /**
     * Sets the value of the properties property.
     * 
     * @param value
     *     allowed object is
     *     {@link CmisPropertiesType }
     *     
     */
    public void setProperties(CmisPropertiesType value) {
        this.properties = value;
    }

    /**
     * Gets the value of the allowableActions property.
     * 
     * @return
     *     possible object is
     *     {@link CmisAllowableActionsType }
     *     
     */
    public CmisAllowableActionsType getAllowableActions() {
        return allowableActions;
    }

    /**
     * Sets the value of the allowableActions property.
     * 
     * @param value
     *     allowed object is
     *     {@link CmisAllowableActionsType }
     *     
     */
    public void setAllowableActions(CmisAllowableActionsType value) {
        this.allowableActions = value;
    }

    /**
     * Gets the value of the relationship property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the relationship property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRelationship().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CmisObjectType }
     * 
     * 
     */
    public List<CmisObjectType> getRelationship() {
        if (relationship == null) {
            relationship = new ArrayList<CmisObjectType>();
        }
        return this.relationship;
    }

    /**
     * Gets the value of the changeObject property.
     * 
     * @return
     *     possible object is
     *     {@link CmisChangedObjectType }
     *     
     */
    public CmisChangedObjectType getChangeObject() {
        return changeObject;
    }

    /**
     * Sets the value of the changeObject property.
     * 
     * @param value
     *     allowed object is
     *     {@link CmisChangedObjectType }
     *     
     */
    public void setChangeObject(CmisChangedObjectType value) {
        this.changeObject = value;
    }

    /**
     * Gets the value of the child property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the child property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getChild().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CmisObjectType }
     * 
     * 
     */
    public List<CmisObjectType> getChild() {
        if (child == null) {
            child = new ArrayList<CmisObjectType>();
        }
        return this.child;
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
