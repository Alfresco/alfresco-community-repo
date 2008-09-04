
package org.alfresco.repo.cmis.ws;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getTypes element declaration.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;element name="getTypes">
 *   &lt;complexType>
 *     &lt;complexContent>
 *       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *         &lt;sequence>
 *           &lt;element name="type" type="{http://www.cmis.org/ns/1.0}getTypesEnum"/>
 *           &lt;element name="returnPropertyDefinitions" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *           &lt;element ref="{http://www.cmis.org/ns/1.0}maxItems" minOccurs="0"/>
 *           &lt;element ref="{http://www.cmis.org/ns/1.0}skipCount" minOccurs="0"/>
 *         &lt;/sequence>
 *       &lt;/restriction>
 *     &lt;/complexContent>
 *   &lt;/complexType>
 * &lt;/element>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "type",
    "returnPropertyDefinitions",
    "maxItems",
    "skipCount"
})
@XmlRootElement(name = "getTypes")
public class GetTypes {

    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected GetTypesEnum type;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected Boolean returnPropertyDefinitions;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected BigInteger maxItems;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected BigInteger skipCount;

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link GetTypesEnum }
     *     
     */
    public GetTypesEnum getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link GetTypesEnum }
     *     
     */
    public void setType(GetTypesEnum value) {
        this.type = value;
    }

    /**
     * Gets the value of the returnPropertyDefinitions property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isReturnPropertyDefinitions() {
        return returnPropertyDefinitions;
    }

    /**
     * Sets the value of the returnPropertyDefinitions property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setReturnPropertyDefinitions(Boolean value) {
        this.returnPropertyDefinitions = value;
    }

    /**
     * Gets the value of the maxItems property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getMaxItems() {
        return maxItems;
    }

    /**
     * Sets the value of the maxItems property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setMaxItems(BigInteger value) {
        this.maxItems = value;
    }

    /**
     * Gets the value of the skipCount property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getSkipCount() {
        return skipCount;
    }

    /**
     * Sets the value of the skipCount property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setSkipCount(BigInteger value) {
        this.skipCount = value;
    }

}
