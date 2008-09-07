
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for cmisPropertyId complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="cmisPropertyId">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.cmis.org/2008/05}cmisProperty">
 *       &lt;sequence>
 *         &lt;element name="value" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute ref="{http://www.cmis.org/2008/05}propertyType default="id""/>
 *       &lt;attribute ref="{http://www.cmis.org/2008/05}href"/>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "cmisPropertyId", propOrder = {
    "value"
})
public class CmisPropertyId
    extends CmisProperty
{

    protected String value;
    @XmlAttribute(namespace = "http://www.cmis.org/2008/05")
    protected EnumPropertyType propertyType;
    @XmlAttribute(namespace = "http://www.cmis.org/2008/05")
    @XmlSchemaType(name = "anyURI")
    protected String href;

    /**
     * Gets the value of the value property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValue(String value) {
        this.value = value;
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
        if (propertyType == null) {
            return EnumPropertyType.ID;
        } else {
            return propertyType;
        }
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
     * Gets the value of the href property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHref() {
        return href;
    }

    /**
     * Sets the value of the href property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHref(String value) {
        this.href = value;
    }

}
