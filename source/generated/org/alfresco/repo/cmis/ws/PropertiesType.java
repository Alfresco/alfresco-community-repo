
package org.alfresco.repo.cmis.ws;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for propertiesType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="propertiesType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="propertyBoolean" type="{http://www.cmis.org/ns/1.0}propertyBooleanType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="propertyDateTime" type="{http://www.cmis.org/ns/1.0}propertyDateTimeType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="propertyDecimal" type="{http://www.cmis.org/ns/1.0}propertyDecimalType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="propertyHTML" type="{http://www.cmis.org/ns/1.0}propertyHTMLType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="propertyID" type="{http://www.cmis.org/ns/1.0}propertyIDType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="propertyInteger" type="{http://www.cmis.org/ns/1.0}propertyIntegerType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="propertyString" type="{http://www.cmis.org/ns/1.0}propertyStringType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="propertyURI" type="{http://www.cmis.org/ns/1.0}propertyURIType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="propertyXML" type="{http://www.cmis.org/ns/1.0}propertyXMLType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "propertiesType", propOrder = {
    "propertyBoolean",
    "propertyDateTime",
    "propertyDecimal",
    "propertyHTML",
    "propertyID",
    "propertyInteger",
    "propertyString",
    "propertyURI",
    "propertyXML"
})
public class PropertiesType {

    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected List<PropertyBooleanType> propertyBoolean;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected List<PropertyDateTimeType> propertyDateTime;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected List<PropertyDecimalType> propertyDecimal;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected List<PropertyHTMLType> propertyHTML;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected List<PropertyIDType> propertyID;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected List<PropertyIntegerType> propertyInteger;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected List<PropertyStringType> propertyString;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected List<PropertyURIType> propertyURI;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected List<PropertyXMLType> propertyXML;

    /**
     * Gets the value of the propertyBoolean property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the propertyBoolean property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPropertyBoolean().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PropertyBooleanType }
     * 
     * 
     */
    public List<PropertyBooleanType> getPropertyBoolean() {
        if (propertyBoolean == null) {
            propertyBoolean = new ArrayList<PropertyBooleanType>();
        }
        return this.propertyBoolean;
    }

    /**
     * Gets the value of the propertyDateTime property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the propertyDateTime property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPropertyDateTime().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PropertyDateTimeType }
     * 
     * 
     */
    public List<PropertyDateTimeType> getPropertyDateTime() {
        if (propertyDateTime == null) {
            propertyDateTime = new ArrayList<PropertyDateTimeType>();
        }
        return this.propertyDateTime;
    }

    /**
     * Gets the value of the propertyDecimal property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the propertyDecimal property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPropertyDecimal().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PropertyDecimalType }
     * 
     * 
     */
    public List<PropertyDecimalType> getPropertyDecimal() {
        if (propertyDecimal == null) {
            propertyDecimal = new ArrayList<PropertyDecimalType>();
        }
        return this.propertyDecimal;
    }

    /**
     * Gets the value of the propertyHTML property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the propertyHTML property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPropertyHTML().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PropertyHTMLType }
     * 
     * 
     */
    public List<PropertyHTMLType> getPropertyHTML() {
        if (propertyHTML == null) {
            propertyHTML = new ArrayList<PropertyHTMLType>();
        }
        return this.propertyHTML;
    }

    /**
     * Gets the value of the propertyID property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the propertyID property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPropertyID().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PropertyIDType }
     * 
     * 
     */
    public List<PropertyIDType> getPropertyID() {
        if (propertyID == null) {
            propertyID = new ArrayList<PropertyIDType>();
        }
        return this.propertyID;
    }

    /**
     * Gets the value of the propertyInteger property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the propertyInteger property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPropertyInteger().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PropertyIntegerType }
     * 
     * 
     */
    public List<PropertyIntegerType> getPropertyInteger() {
        if (propertyInteger == null) {
            propertyInteger = new ArrayList<PropertyIntegerType>();
        }
        return this.propertyInteger;
    }

    /**
     * Gets the value of the propertyString property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the propertyString property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPropertyString().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PropertyStringType }
     * 
     * 
     */
    public List<PropertyStringType> getPropertyString() {
        if (propertyString == null) {
            propertyString = new ArrayList<PropertyStringType>();
        }
        return this.propertyString;
    }

    /**
     * Gets the value of the propertyURI property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the propertyURI property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPropertyURI().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PropertyURIType }
     * 
     * 
     */
    public List<PropertyURIType> getPropertyURI() {
        if (propertyURI == null) {
            propertyURI = new ArrayList<PropertyURIType>();
        }
        return this.propertyURI;
    }

    /**
     * Gets the value of the propertyXML property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the propertyXML property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPropertyXML().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PropertyXMLType }
     * 
     * 
     */
    public List<PropertyXMLType> getPropertyXML() {
        if (propertyXML == null) {
            propertyXML = new ArrayList<PropertyXMLType>();
        }
        return this.propertyXML;
    }

}
