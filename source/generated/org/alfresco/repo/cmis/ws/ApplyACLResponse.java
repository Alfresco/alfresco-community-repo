
package org.alfresco.repo.cmis.ws;

import java.util.ArrayList;
import java.util.List;
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
 *         &lt;element name="ACL" type="{http://docs.oasis-open.org/ns/cmis/core/200901}cmisAccessControlListType" maxOccurs="unbounded"/>
 *         &lt;element name="exact" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
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
    "acl",
    "exact"
})
@XmlRootElement(name = "applyACLResponse")
public class ApplyACLResponse {

    @XmlElement(name = "ACL", required = true)
    protected List<CmisAccessControlListType> acl;
    protected boolean exact;

    /**
     * Gets the value of the acl property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the acl property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getACL().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CmisAccessControlListType }
     * 
     * 
     */
    public List<CmisAccessControlListType> getACL() {
        if (acl == null) {
            acl = new ArrayList<CmisAccessControlListType>();
        }
        return this.acl;
    }

    /**
     * Gets the value of the exact property.
     * 
     */
    public boolean isExact() {
        return exact;
    }

    /**
     * Sets the value of the exact property.
     * 
     */
    public void setExact(boolean value) {
        this.exact = value;
    }

}
