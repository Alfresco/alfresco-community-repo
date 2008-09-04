
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for capabilitiesType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="capabilitiesType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="capabilityMultifiling" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="capabilityUnfiling" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="capabilityVersionSpecificFiling" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="capabilityPWCUpdatable" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="capabilityAllVersionsSearchable" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="capabilityJoin" type="{http://www.cmis.org/ns/1.0}joinEnum"/>
 *         &lt;element name="capabilityFulltext" type="{http://www.cmis.org/ns/1.0}fulltextEnum"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "capabilitiesType", propOrder = {
    "capabilityMultifiling",
    "capabilityUnfiling",
    "capabilityVersionSpecificFiling",
    "capabilityPWCUpdatable",
    "capabilityAllVersionsSearchable",
    "capabilityJoin",
    "capabilityFulltext"
})
public class CapabilitiesType {

    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected boolean capabilityMultifiling;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected boolean capabilityUnfiling;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected boolean capabilityVersionSpecificFiling;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected boolean capabilityPWCUpdatable;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected boolean capabilityAllVersionsSearchable;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected JoinEnum capabilityJoin;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected FulltextEnum capabilityFulltext;

    /**
     * Gets the value of the capabilityMultifiling property.
     * 
     */
    public boolean isCapabilityMultifiling() {
        return capabilityMultifiling;
    }

    /**
     * Sets the value of the capabilityMultifiling property.
     * 
     */
    public void setCapabilityMultifiling(boolean value) {
        this.capabilityMultifiling = value;
    }

    /**
     * Gets the value of the capabilityUnfiling property.
     * 
     */
    public boolean isCapabilityUnfiling() {
        return capabilityUnfiling;
    }

    /**
     * Sets the value of the capabilityUnfiling property.
     * 
     */
    public void setCapabilityUnfiling(boolean value) {
        this.capabilityUnfiling = value;
    }

    /**
     * Gets the value of the capabilityVersionSpecificFiling property.
     * 
     */
    public boolean isCapabilityVersionSpecificFiling() {
        return capabilityVersionSpecificFiling;
    }

    /**
     * Sets the value of the capabilityVersionSpecificFiling property.
     * 
     */
    public void setCapabilityVersionSpecificFiling(boolean value) {
        this.capabilityVersionSpecificFiling = value;
    }

    /**
     * Gets the value of the capabilityPWCUpdatable property.
     * 
     */
    public boolean isCapabilityPWCUpdatable() {
        return capabilityPWCUpdatable;
    }

    /**
     * Sets the value of the capabilityPWCUpdatable property.
     * 
     */
    public void setCapabilityPWCUpdatable(boolean value) {
        this.capabilityPWCUpdatable = value;
    }

    /**
     * Gets the value of the capabilityAllVersionsSearchable property.
     * 
     */
    public boolean isCapabilityAllVersionsSearchable() {
        return capabilityAllVersionsSearchable;
    }

    /**
     * Sets the value of the capabilityAllVersionsSearchable property.
     * 
     */
    public void setCapabilityAllVersionsSearchable(boolean value) {
        this.capabilityAllVersionsSearchable = value;
    }

    /**
     * Gets the value of the capabilityJoin property.
     * 
     * @return
     *     possible object is
     *     {@link JoinEnum }
     *     
     */
    public JoinEnum getCapabilityJoin() {
        return capabilityJoin;
    }

    /**
     * Sets the value of the capabilityJoin property.
     * 
     * @param value
     *     allowed object is
     *     {@link JoinEnum }
     *     
     */
    public void setCapabilityJoin(JoinEnum value) {
        this.capabilityJoin = value;
    }

    /**
     * Gets the value of the capabilityFulltext property.
     * 
     * @return
     *     possible object is
     *     {@link FulltextEnum }
     *     
     */
    public FulltextEnum getCapabilityFulltext() {
        return capabilityFulltext;
    }

    /**
     * Sets the value of the capabilityFulltext property.
     * 
     * @param value
     *     allowed object is
     *     {@link FulltextEnum }
     *     
     */
    public void setCapabilityFulltext(FulltextEnum value) {
        this.capabilityFulltext = value;
    }

}
