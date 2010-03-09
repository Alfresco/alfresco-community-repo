
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
 *         &lt;element name="ACL" type="{http://docs.oasis-open.org/ns/cmis/messaging/200908/}cmisACLType"/>
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
@XmlRootElement(name = "applyACLResponse")
public class ApplyACLResponse {

    @XmlElement(name = "ACL", required = true)
    protected CmisACLType acl;

    /**
     * Gets the value of the acl property.
     * 
     * @return
     *     possible object is
     *     {@link CmisACLType }
     *     
     */
    public CmisACLType getACL() {
        return acl;
    }

    /**
     * Sets the value of the acl property.
     * 
     * @param value
     *     allowed object is
     *     {@link CmisACLType }
     *     
     */
    public void setACL(CmisACLType value) {
        this.acl = value;
    }

}
