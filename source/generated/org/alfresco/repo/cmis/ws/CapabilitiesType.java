
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
 *         &lt;element name="capabilityUpdatePWC" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="capabilityAllVersionsSearchable" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
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
    "capabilityUpdatePWC",
    "capabilityAllVersionsSearchable"
})
public class CapabilitiesType {

    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected boolean capabilityMultifiling;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected boolean capabilityUpdatePWC;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected boolean capabilityAllVersionsSearchable;

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
     * Gets the value of the capabilityUpdatePWC property.
     * 
     */
    public boolean isCapabilityUpdatePWC() {
        return capabilityUpdatePWC;
    }

    /**
     * Sets the value of the capabilityUpdatePWC property.
     * 
     */
    public void setCapabilityUpdatePWC(boolean value) {
        this.capabilityUpdatePWC = value;
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

}
