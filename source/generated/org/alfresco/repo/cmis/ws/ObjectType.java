
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for objectType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="objectType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="properties" type="{http://www.cmis.org/ns/1.0}propertiesType" minOccurs="0"/>
 *         &lt;element name="allowableActions" type="{http://www.cmis.org/ns/1.0}allowableActionsType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "objectType", propOrder = {
    "properties",
    "allowableActions"
})
public class ObjectType {

    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected PropertiesType properties;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected AllowableActionsType allowableActions;

    /**
     * Gets the value of the properties property.
     * 
     * @return
     *     possible object is
     *     {@link PropertiesType }
     *     
     */
    public PropertiesType getProperties() {
        return properties;
    }

    /**
     * Sets the value of the properties property.
     * 
     * @param value
     *     allowed object is
     *     {@link PropertiesType }
     *     
     */
    public void setProperties(PropertiesType value) {
        this.properties = value;
    }

    /**
     * Gets the value of the allowableActions property.
     * 
     * @return
     *     possible object is
     *     {@link AllowableActionsType }
     *     
     */
    public AllowableActionsType getAllowableActions() {
        return allowableActions;
    }

    /**
     * Sets the value of the allowableActions property.
     * 
     * @param value
     *     allowed object is
     *     {@link AllowableActionsType }
     *     
     */
    public void setAllowableActions(AllowableActionsType value) {
        this.allowableActions = value;
    }

}
