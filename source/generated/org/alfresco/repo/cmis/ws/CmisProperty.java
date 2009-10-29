
package org.alfresco.repo.cmis.ws;

import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;


/**
 * <p>Java class for cmisProperty complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="cmisProperty">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attGroup ref="{http://docs.oasis-open.org/ns/cmis/core/200901}cmisUndefinedAttribute"/>
 *       &lt;attribute name="pdid" use="required" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="localname" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="displayname" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "cmisProperty", namespace = "http://docs.oasis-open.org/ns/cmis/core/200901")
@XmlSeeAlso({
    CmisPropertyDecimal.class,
    CmisPropertyInteger.class,
    CmisPropertyXhtml.class,
    CmisPropertyDateTime.class,
    CmisPropertyUri.class,
    CmisPropertyXml.class,
    CmisPropertyHtml.class,
    CmisPropertyBoolean.class,
    CmisPropertyString.class,
    CmisPropertyId.class
})
public class CmisProperty {

    @XmlAttribute(required = true)
    @XmlSchemaType(name = "anySimpleType")
    protected String pdid;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String localname;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String displayname;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();

    /**
     * Gets the value of the pdid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPdid() {
        return pdid;
    }

    /**
     * Sets the value of the pdid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPdid(String value) {
        this.pdid = value;
    }

    /**
     * Gets the value of the localname property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLocalname() {
        return localname;
    }

    /**
     * Sets the value of the localname property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLocalname(String value) {
        this.localname = value;
    }

    /**
     * Gets the value of the displayname property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDisplayname() {
        return displayname;
    }

    /**
     * Sets the value of the displayname property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDisplayname(String value) {
        this.displayname = value;
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
