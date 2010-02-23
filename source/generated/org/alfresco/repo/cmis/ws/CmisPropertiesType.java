
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
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;
import org.w3c.dom.Element;


/**
 * <p>Java class for cmisPropertiesType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="cmisPropertiesType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element name="propertyBoolean" type="{http://docs.oasis-open.org/ns/cmis/core/200908/}cmisPropertyBoolean"/>
 *           &lt;element name="propertyId" type="{http://docs.oasis-open.org/ns/cmis/core/200908/}cmisPropertyId"/>
 *           &lt;element name="propertyInteger" type="{http://docs.oasis-open.org/ns/cmis/core/200908/}cmisPropertyInteger"/>
 *           &lt;element name="propertyDateTime" type="{http://docs.oasis-open.org/ns/cmis/core/200908/}cmisPropertyDateTime"/>
 *           &lt;element name="propertyDecimal" type="{http://docs.oasis-open.org/ns/cmis/core/200908/}cmisPropertyDecimal"/>
 *           &lt;element name="propertyHtml" type="{http://docs.oasis-open.org/ns/cmis/core/200908/}cmisPropertyHtml"/>
 *           &lt;element name="propertyString" type="{http://docs.oasis-open.org/ns/cmis/core/200908/}cmisPropertyString"/>
 *           &lt;element name="propertyUri" type="{http://docs.oasis-open.org/ns/cmis/core/200908/}cmisPropertyUri"/>
 *         &lt;/choice>
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
@XmlType(name = "cmisPropertiesType", namespace = "http://docs.oasis-open.org/ns/cmis/core/200908/", propOrder = {
    "property",
    "any"
})
public class CmisPropertiesType {

    @XmlElements({
        @XmlElement(name = "propertyHtml", type = CmisPropertyHtml.class, nillable = true),
        @XmlElement(name = "propertyDateTime", type = CmisPropertyDateTime.class, nillable = true),
        @XmlElement(name = "propertyUri", type = CmisPropertyUri.class, nillable = true),
        @XmlElement(name = "propertyBoolean", type = CmisPropertyBoolean.class, nillable = true),
        @XmlElement(name = "propertyString", type = CmisPropertyString.class, nillable = true),
        @XmlElement(name = "propertyId", type = CmisPropertyId.class, nillable = true),
        @XmlElement(name = "propertyDecimal", type = CmisPropertyDecimal.class, nillable = true),
        @XmlElement(name = "propertyInteger", type = CmisPropertyInteger.class, nillable = true)
    })
    protected List<CmisProperty> property;
    @XmlAnyElement(lax = true)
    protected List<Object> any;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();

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
     * {@link CmisPropertyHtml }
     * {@link CmisPropertyDateTime }
     * {@link CmisPropertyUri }
     * {@link CmisPropertyBoolean }
     * {@link CmisPropertyString }
     * {@link CmisPropertyId }
     * {@link CmisPropertyDecimal }
     * {@link CmisPropertyInteger }
     * 
     * 
     */
    public List<CmisProperty> getProperty() {
        if (property == null) {
            property = new ArrayList<CmisProperty>();
        }
        return this.property;
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
