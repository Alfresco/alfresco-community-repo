
package org.alfresco.repo.cmis.ws;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for cmisACLCapabilityType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="cmisACLCapabilityType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="setType" type="{http://docs.oasis-open.org/ns/cmis/core/200901}enumACLPropagation"/>
 *         &lt;element name="permissions" type="{http://docs.oasis-open.org/ns/cmis/core/200901}cmisPermissionDefinition" maxOccurs="unbounded"/>
 *         &lt;element name="mapping" type="{http://docs.oasis-open.org/ns/cmis/core/200901}cmisPermissionMapping" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "cmisACLCapabilityType", namespace = "http://docs.oasis-open.org/ns/cmis/core/200901", propOrder = {
    "setType",
    "permissions",
    "mapping"
})
public class CmisACLCapabilityType {

    @XmlElement(required = true)
    protected EnumACLPropagation setType;
    @XmlElement(required = true)
    protected List<CmisPermissionDefinition> permissions;
    protected List<CmisPermissionMapping> mapping;

    /**
     * Gets the value of the setType property.
     * 
     * @return
     *     possible object is
     *     {@link EnumACLPropagation }
     *     
     */
    public EnumACLPropagation getSetType() {
        return setType;
    }

    /**
     * Sets the value of the setType property.
     * 
     * @param value
     *     allowed object is
     *     {@link EnumACLPropagation }
     *     
     */
    public void setSetType(EnumACLPropagation value) {
        this.setType = value;
    }

    /**
     * Gets the value of the permissions property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the permissions property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPermissions().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CmisPermissionDefinition }
     * 
     * 
     */
    public List<CmisPermissionDefinition> getPermissions() {
        if (permissions == null) {
            permissions = new ArrayList<CmisPermissionDefinition>();
        }
        return this.permissions;
    }

    /**
     * Gets the value of the mapping property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the mapping property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMapping().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CmisPermissionMapping }
     * 
     * 
     */
    public List<CmisPermissionMapping> getMapping() {
        if (mapping == null) {
            mapping = new ArrayList<CmisPermissionMapping>();
        }
        return this.mapping;
    }

}
