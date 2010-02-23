
package org.alfresco.repo.cmis.ws;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;
import org.w3c.dom.Element;


/**
 * <p>Java class for cmisRepositoryInfoType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="cmisRepositoryInfoType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="repositoryId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="repositoryName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="repositoryDescription" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="vendorName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="productName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="productVersion" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="rootFolderId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="latestChangeLogToken" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="capabilities" type="{http://docs.oasis-open.org/ns/cmis/core/200908/}cmisRepositoryCapabilitiesType"/>
 *         &lt;element name="aclCapability" type="{http://docs.oasis-open.org/ns/cmis/core/200908/}cmisACLCapabilityType" minOccurs="0"/>
 *         &lt;element name="cmisVersionSupported" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="thinClientURI" type="{http://www.w3.org/2001/XMLSchema}anyURI" minOccurs="0"/>
 *         &lt;element name="changesIncomplete" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="changesOnType" type="{http://docs.oasis-open.org/ns/cmis/core/200908/}enumBaseObjectTypeIds" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="principalAnonymous" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="principalAnyone" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;any/>
 *       &lt;/sequence>
 *       &lt;attGroup ref="{http://docs.oasis-open.org/ns/cmis/core/200908/}cmisUndefinedAttribute"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "cmisRepositoryInfoType", namespace = "http://docs.oasis-open.org/ns/cmis/core/200908/", propOrder = {
    "repositoryId",
    "repositoryName",
    "repositoryDescription",
    "vendorName",
    "productName",
    "productVersion",
    "rootFolderId",
    "latestChangeLogToken",
    "capabilities",
    "aclCapability",
    "cmisVersionSupported",
    "thinClientURI",
    "changesIncomplete",
    "changesOnType",
    "principalAnonymous",
    "principalAnyone",
    "any"
})
public class CmisRepositoryInfoType {

    @XmlElement(required = true)
    protected String repositoryId;
    @XmlElement(required = true)
    protected String repositoryName;
    @XmlElement(required = true)
    protected String repositoryDescription;
    @XmlElement(required = true)
    protected String vendorName;
    @XmlElement(required = true)
    protected String productName;
    @XmlElement(required = true)
    protected String productVersion;
    @XmlElement(required = true)
    protected String rootFolderId;
    protected String latestChangeLogToken;
    @XmlElement(required = true)
    protected CmisRepositoryCapabilitiesType capabilities;
    protected CmisACLCapabilityType aclCapability;
    @XmlElement(required = true)
    protected String cmisVersionSupported;
    @XmlSchemaType(name = "anyURI")
    protected String thinClientURI;
    protected Boolean changesIncomplete;
    protected List<EnumBaseObjectTypeIds> changesOnType;
    protected String principalAnonymous;
    protected String principalAnyone;
    @XmlAnyElement(lax = true)
    protected List<Object> any;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();

    /**
     * Gets the value of the repositoryId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRepositoryId() {
        return repositoryId;
    }

    /**
     * Sets the value of the repositoryId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRepositoryId(String value) {
        this.repositoryId = value;
    }

    /**
     * Gets the value of the repositoryName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRepositoryName() {
        return repositoryName;
    }

    /**
     * Sets the value of the repositoryName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRepositoryName(String value) {
        this.repositoryName = value;
    }

    /**
     * Gets the value of the repositoryDescription property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRepositoryDescription() {
        return repositoryDescription;
    }

    /**
     * Sets the value of the repositoryDescription property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRepositoryDescription(String value) {
        this.repositoryDescription = value;
    }

    /**
     * Gets the value of the vendorName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVendorName() {
        return vendorName;
    }

    /**
     * Sets the value of the vendorName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVendorName(String value) {
        this.vendorName = value;
    }

    /**
     * Gets the value of the productName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProductName() {
        return productName;
    }

    /**
     * Sets the value of the productName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProductName(String value) {
        this.productName = value;
    }

    /**
     * Gets the value of the productVersion property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProductVersion() {
        return productVersion;
    }

    /**
     * Sets the value of the productVersion property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProductVersion(String value) {
        this.productVersion = value;
    }

    /**
     * Gets the value of the rootFolderId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRootFolderId() {
        return rootFolderId;
    }

    /**
     * Sets the value of the rootFolderId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRootFolderId(String value) {
        this.rootFolderId = value;
    }

    /**
     * Gets the value of the latestChangeLogToken property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLatestChangeLogToken() {
        return latestChangeLogToken;
    }

    /**
     * Sets the value of the latestChangeLogToken property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLatestChangeLogToken(String value) {
        this.latestChangeLogToken = value;
    }

    /**
     * Gets the value of the capabilities property.
     * 
     * @return
     *     possible object is
     *     {@link CmisRepositoryCapabilitiesType }
     *     
     */
    public CmisRepositoryCapabilitiesType getCapabilities() {
        return capabilities;
    }

    /**
     * Sets the value of the capabilities property.
     * 
     * @param value
     *     allowed object is
     *     {@link CmisRepositoryCapabilitiesType }
     *     
     */
    public void setCapabilities(CmisRepositoryCapabilitiesType value) {
        this.capabilities = value;
    }

    /**
     * Gets the value of the aclCapability property.
     * 
     * @return
     *     possible object is
     *     {@link CmisACLCapabilityType }
     *     
     */
    public CmisACLCapabilityType getAclCapability() {
        return aclCapability;
    }

    /**
     * Sets the value of the aclCapability property.
     * 
     * @param value
     *     allowed object is
     *     {@link CmisACLCapabilityType }
     *     
     */
    public void setAclCapability(CmisACLCapabilityType value) {
        this.aclCapability = value;
    }

    /**
     * Gets the value of the cmisVersionSupported property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCmisVersionSupported() {
        return cmisVersionSupported;
    }

    /**
     * Sets the value of the cmisVersionSupported property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCmisVersionSupported(String value) {
        this.cmisVersionSupported = value;
    }

    /**
     * Gets the value of the thinClientURI property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getThinClientURI() {
        return thinClientURI;
    }

    /**
     * Sets the value of the thinClientURI property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setThinClientURI(String value) {
        this.thinClientURI = value;
    }

    /**
     * Gets the value of the changesIncomplete property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isChangesIncomplete() {
        return changesIncomplete;
    }

    /**
     * Sets the value of the changesIncomplete property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setChangesIncomplete(Boolean value) {
        this.changesIncomplete = value;
    }

    /**
     * Gets the value of the changesOnType property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the changesOnType property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getChangesOnType().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link EnumBaseObjectTypeIds }
     * 
     * 
     */
    public List<EnumBaseObjectTypeIds> getChangesOnType() {
        if (changesOnType == null) {
            changesOnType = new ArrayList<EnumBaseObjectTypeIds>();
        }
        return this.changesOnType;
    }

    /**
     * Gets the value of the principalAnonymous property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPrincipalAnonymous() {
        return principalAnonymous;
    }

    /**
     * Sets the value of the principalAnonymous property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPrincipalAnonymous(String value) {
        this.principalAnonymous = value;
    }

    /**
     * Gets the value of the principalAnyone property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPrincipalAnyone() {
        return principalAnyone;
    }

    /**
     * Sets the value of the principalAnyone property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPrincipalAnyone(String value) {
        this.principalAnyone = value;
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

    /**
     * Gets a map that contains attributes that aren't bound to any typed property on this class.
     * 
     * <p>
     * the map is keyed by the name of the attribute and 
     * the value is the string value of the attribute.
     * 
     * the map returned by this method is live, and you can add new attribute
     * by updating the map directly. Because of this design, there's no setter.
     * 
     * 
     * @return
     *     always non-null
     */
    public Map<QName, String> getOtherAttributes() {
        return otherAttributes;
    }

}
