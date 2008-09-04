
package org.alfresco.repo.cmis.ws;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for repositoryInfoType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="repositoryInfoType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="repositoryId" type="{http://www.cmis.org/ns/1.0}ID"/>
 *         &lt;element name="repositoryName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="repositoryURI" type="{http://www.w3.org/2001/XMLSchema}anyURI"/>
 *         &lt;element name="repositoryDescription" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="rootFolderId" type="{http://www.cmis.org/ns/1.0}ID"/>
 *         &lt;element name="vendorName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="productName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="productVersion" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="cmisVersionsSupported" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="repositorySpecificInformation" type="{http://www.cmis.org/ns/1.0}XML" minOccurs="0"/>
 *         &lt;element name="capabilities" type="{http://www.cmis.org/ns/1.0}capabilitiesType"/>
 *         &lt;element name="relatedRepositories" type="{http://www.cmis.org/ns/1.0}relatedRepositoriesType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "repositoryInfoType", propOrder = {
    "repositoryId",
    "repositoryName",
    "repositoryURI",
    "repositoryDescription",
    "rootFolderId",
    "vendorName",
    "productName",
    "productVersion",
    "cmisVersionsSupported",
    "repositorySpecificInformation",
    "capabilities",
    "relatedRepositories"
})
public class RepositoryInfoType {

    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected String repositoryId;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected String repositoryName;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected String repositoryURI;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected String repositoryDescription;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected String rootFolderId;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected String vendorName;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected String productName;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected String productVersion;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected String cmisVersionsSupported;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected XML repositorySpecificInformation;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected CapabilitiesType capabilities;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected List<RelatedRepositoriesType> relatedRepositories;

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
     * Gets the value of the repositoryURI property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRepositoryURI() {
        return repositoryURI;
    }

    /**
     * Sets the value of the repositoryURI property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRepositoryURI(String value) {
        this.repositoryURI = value;
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
     * Gets the value of the cmisVersionsSupported property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCmisVersionsSupported() {
        return cmisVersionsSupported;
    }

    /**
     * Sets the value of the cmisVersionsSupported property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCmisVersionsSupported(String value) {
        this.cmisVersionsSupported = value;
    }

    /**
     * Gets the value of the repositorySpecificInformation property.
     * 
     * @return
     *     possible object is
     *     {@link XML }
     *     
     */
    public XML getRepositorySpecificInformation() {
        return repositorySpecificInformation;
    }

    /**
     * Sets the value of the repositorySpecificInformation property.
     * 
     * @param value
     *     allowed object is
     *     {@link XML }
     *     
     */
    public void setRepositorySpecificInformation(XML value) {
        this.repositorySpecificInformation = value;
    }

    /**
     * Gets the value of the capabilities property.
     * 
     * @return
     *     possible object is
     *     {@link CapabilitiesType }
     *     
     */
    public CapabilitiesType getCapabilities() {
        return capabilities;
    }

    /**
     * Sets the value of the capabilities property.
     * 
     * @param value
     *     allowed object is
     *     {@link CapabilitiesType }
     *     
     */
    public void setCapabilities(CapabilitiesType value) {
        this.capabilities = value;
    }

    /**
     * Gets the value of the relatedRepositories property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the relatedRepositories property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRelatedRepositories().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link RelatedRepositoriesType }
     * 
     * 
     */
    public List<RelatedRepositoriesType> getRelatedRepositories() {
        if (relatedRepositories == null) {
            relatedRepositories = new ArrayList<RelatedRepositoriesType>();
        }
        return this.relatedRepositories;
    }

}
