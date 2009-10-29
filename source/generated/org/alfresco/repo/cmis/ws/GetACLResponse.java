
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="acl" type="{http://docs.oasis-open.org/ns/cmis/core/200901}cmisAccessControlListType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "acl"
})
@XmlRootElement(name = "getACLResponse")
public class GetACLResponse {

    @XmlElement(required = true)
    protected CmisAccessControlListType acl;

    /**
     * Gets the value of the acl property.
     * 
     * @return
     *     possible object is
     *     {@link CmisAccessControlListType }
     *     
     */
    public CmisAccessControlListType getAcl() {
        return acl;
    }

    /**
     * Sets the value of the acl property.
     * 
     * @param value
     *     allowed object is
     *     {@link CmisAccessControlListType }
     *     
     */
    public void setAcl(CmisAccessControlListType value) {
        this.acl = value;
    }

}
