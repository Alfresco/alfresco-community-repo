
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for cmisAccessControlEntryType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="cmisAccessControlEntryType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="principal" type="{http://docs.oasis-open.org/ns/cmis/core/200901}cmisAccessControlPrincipalType"/>
 *         &lt;element name="permission" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="direct" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
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
@XmlType(name = "cmisAccessControlEntryType", namespace = "http://docs.oasis-open.org/ns/cmis/core/200901", propOrder = {
    "principal",
    "permission",
    "direct",
    "any"
})
public class CmisAccessControlEntryType {

    @XmlElement(required = true)
    protected CmisAccessControlPrincipalType principal;
    @XmlElement(required = true)
    protected String permission;
    protected boolean direct;
    @XmlAnyElement(lax = true)
    protected Object any;

    /**
     * Gets the value of the principal property.
     * 
     * @return
     *     possible object is
     *     {@link CmisAccessControlPrincipalType }
     *     
     */
    public CmisAccessControlPrincipalType getPrincipal() {
        return principal;
    }

    /**
     * Sets the value of the principal property.
     * 
     * @param value
     *     allowed object is
     *     {@link CmisAccessControlPrincipalType }
     *     
     */
    public void setPrincipal(CmisAccessControlPrincipalType value) {
        this.principal = value;
    }

    /**
     * Gets the value of the permission property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPermission() {
        return permission;
    }

    /**
     * Sets the value of the permission property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPermission(String value) {
        this.permission = value;
    }

    /**
     * Gets the value of the direct property.
     * 
     */
    public boolean isDirect() {
        return direct;
    }

    /**
     * Sets the value of the direct property.
     * 
     */
    public void setDirect(boolean value) {
        this.direct = value;
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
