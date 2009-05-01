
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for cmisChangedObjectType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="cmisChangedObjectType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="changeType" type="{http://docs.oasis-open.org/ns/cmis/core/200901}enumTypeOfChanges"/>
 *         &lt;element name="changeTime" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;any/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "cmisChangedObjectType", namespace = "http://docs.oasis-open.org/ns/cmis/core/200901", propOrder = {
    "changeType",
    "changeTime",
    "any"
})
public class CmisChangedObjectType {

    @XmlElement(required = true)
    protected EnumTypeOfChanges changeType;
    @XmlElement(required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar changeTime;
    @XmlAnyElement(lax = true)
    protected Object any;

    /**
     * Gets the value of the changeType property.
     * 
     * @return
     *     possible object is
     *     {@link EnumTypeOfChanges }
     *     
     */
    public EnumTypeOfChanges getChangeType() {
        return changeType;
    }

    /**
     * Sets the value of the changeType property.
     * 
     * @param value
     *     allowed object is
     *     {@link EnumTypeOfChanges }
     *     
     */
    public void setChangeType(EnumTypeOfChanges value) {
        this.changeType = value;
    }

    /**
     * Gets the value of the changeTime property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getChangeTime() {
        return changeTime;
    }

    /**
     * Sets the value of the changeTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setChangeTime(XMLGregorianCalendar value) {
        this.changeTime = value;
    }

    /**
     * Gets the value of the any property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getAny() {
        return any;
    }

    /**
     * Sets the value of the any property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setAny(Object value) {
        this.any = value;
    }

}
