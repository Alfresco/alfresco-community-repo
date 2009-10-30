
package org.alfresco.repo.cmis.ws;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.w3c.dom.Element;


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
 *         &lt;element name="principal" type="{http://docs.oasis-open.org/ns/cmis/core/200908/}cmisAccessControlPrincipalType"/>
 *         &lt;element name="permission" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"/>
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
@XmlType(name = "cmisAccessControlEntryType", namespace = "http://docs.oasis-open.org/ns/cmis/core/200908/", propOrder = {
    "principal",
    "permission",
    "direct",
    "any"
})
public class CmisAccessControlEntryType {

    @XmlElement(required = true)
    protected CmisAccessControlPrincipalType principal;
    @XmlElement(required = true)
    protected List<String> permission;
    protected boolean direct;
    @XmlAnyElement(lax = true)
    protected List<Object> any;

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
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the permission property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPermission().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getPermission() {
        if (permission == null) {
            permission = new ArrayList<String>();
        }
        return this.permission;
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

}
