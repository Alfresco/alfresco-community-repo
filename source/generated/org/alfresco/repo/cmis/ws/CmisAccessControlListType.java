
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for cmisAccessControlListType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="cmisAccessControlListType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="permission" type="{http://docs.oasis-open.org/ns/cmis/core/200901}cmisAccessControlEntryType"/>
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
@XmlType(name = "cmisAccessControlListType", namespace = "http://docs.oasis-open.org/ns/cmis/core/200901", propOrder = {
    "permission",
    "any"
})
public class CmisAccessControlListType {

    @XmlElement(required = true)
    protected CmisAccessControlEntryType permission;
    @XmlAnyElement(lax = true)
    protected Object any;

    /**
     * Gets the value of the permission property.
     * 
     * @return
     *     possible object is
     *     {@link CmisAccessControlEntryType }
     *     
     */
    public CmisAccessControlEntryType getPermission() {
        return permission;
    }

    /**
     * Sets the value of the permission property.
     * 
     * @param value
     *     allowed object is
     *     {@link CmisAccessControlEntryType }
     *     
     */
    public void setPermission(CmisAccessControlEntryType value) {
        this.permission = value;
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
