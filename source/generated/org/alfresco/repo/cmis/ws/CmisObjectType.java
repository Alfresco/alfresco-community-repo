
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
 *         &lt;element name="properties" type="{http://docs.oasis-open.org/ns/cmis/core/200908/}cmisPropertiesType" minOccurs="0"/>
 *         &lt;element name="allowableActions" type="{http://docs.oasis-open.org/ns/cmis/core/200908/}cmisAllowableActionsType" minOccurs="0"/>
 *         &lt;element name="relationship" type="{http://docs.oasis-open.org/ns/cmis/core/200908/}cmisObjectType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="changeEventInfo" type="{http://docs.oasis-open.org/ns/cmis/core/200908/}cmisChangeEventType" minOccurs="0"/>
 *         &lt;element name="acl" type="{http://docs.oasis-open.org/ns/cmis/core/200908/}cmisAccessControlListType" minOccurs="0"/>
 *         &lt;element name="exactACL" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="policyIds" type="{http://docs.oasis-open.org/ns/cmis/core/200908/}cmisListOfIdsType" minOccurs="0"/>
 *         &lt;element name="rendition" type="{http://docs.oasis-open.org/ns/cmis/core/200908/}cmisRenditionType" maxOccurs="unbounded" minOccurs="0"/>
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
@XmlType(name = "cmisObjectType", namespace = "http://docs.oasis-open.org/ns/cmis/core/200908/", propOrder = {
    "properties",
    "allowableActions",
    "relationship",
    "changeEventInfo",
    "acl",
    "exactACL",
    "policyIds",
    "rendition",
    "any"
})
public class CmisObjectType {

    protected CmisPropertiesType properties;
    protected CmisAllowableActionsType allowableActions;
    protected List<CmisObjectType> relationship;
    protected CmisChangeEventType changeEventInfo;
    protected CmisAccessControlListType acl;
    protected Boolean exactACL;
    protected CmisListOfIdsType policyIds;
    protected List<CmisRenditionType> rendition;
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
     * Gets the value of the changeEventInfo property.
     * 
     * @return
     *     possible object is
     *     {@link CmisChangeEventType }
     *     
     */
    public CmisChangeEventType getChangeEventInfo() {
        return changeEventInfo;
    }

    /**
     * Sets the value of the changeEventInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link CmisChangeEventType }
     *     
     */
    public void setChangeEventInfo(CmisChangeEventType value) {
        this.changeEventInfo = value;
    }

    /**
     * Gets the value of the acl property.
     * 
     * @return
     *     possible object is
     *     {@link CmisAccessControlListType }
     *     
     */
    public CmisAccessControlListType getAcl() {
        return acl;
    }

    /**
     * Sets the value of the acl property.
     * 
     * @param value
     *     allowed object is
     *     {@link CmisAccessControlListType }
     *     
     */
    public void setAcl(CmisAccessControlListType value) {
        this.acl = value;
    }

    /**
     * Gets the value of the exactACL property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isExactACL() {
        return exactACL;
    }

    /**
     * Sets the value of the exactACL property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setExactACL(Boolean value) {
        this.exactACL = value;
    }

    /**
     * Gets the value of the policyIds property.
     * 
     * @return
     *     possible object is
     *     {@link CmisListOfIdsType }
     *     
     */
    public CmisListOfIdsType getPolicyIds() {
        return policyIds;
    }

    /**
     * Sets the value of the policyIds property.
     * 
     * @param value
     *     allowed object is
     *     {@link CmisListOfIdsType }
     *     
     */
    public void setPolicyIds(CmisListOfIdsType value) {
        this.policyIds = value;
    }

    /**
     * Gets the value of the rendition property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the rendition property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRendition().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CmisRenditionType }
     * 
     * 
     */
    public List<CmisRenditionType> getRendition() {
        if (rendition == null) {
            rendition = new ArrayList<CmisRenditionType>();
        }
        return this.rendition;
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
