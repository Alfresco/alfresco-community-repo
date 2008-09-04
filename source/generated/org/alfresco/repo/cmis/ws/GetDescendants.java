
package org.alfresco.repo.cmis.ws;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getDescendants element declaration.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;element name="getDescendants">
 *   &lt;complexType>
 *     &lt;complexContent>
 *       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *         &lt;sequence>
 *           &lt;element name="repositoryId" type="{http://www.cmis.org/ns/1.0}ID"/>
 *           &lt;element name="folderId" type="{http://www.cmis.org/ns/1.0}ID"/>
 *           &lt;element name="type" type="{http://www.cmis.org/ns/1.0}typesOfFileableObjectsEnum" minOccurs="0"/>
 *           &lt;element name="depth" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *           &lt;element ref="{http://www.cmis.org/ns/1.0}filter" minOccurs="0"/>
 *           &lt;element name="includeAllowableActions" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;/sequence>
 *       &lt;/restriction>
 *     &lt;/complexContent>
 *   &lt;/complexType>
 * &lt;/element>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "repositoryId",
    "folderId",
    "type",
    "depth",
    "filter",
    "includeAllowableActions"
})
@XmlRootElement(name = "getDescendants")
public class GetDescendants {

    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected String repositoryId;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected String folderId;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected TypesOfFileableObjectsEnum type;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected BigInteger depth;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected String filter;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected Boolean includeAllowableActions;

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
     * Gets the value of the folderId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFolderId() {
        return folderId;
    }

    /**
     * Sets the value of the folderId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFolderId(String value) {
        this.folderId = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link TypesOfFileableObjectsEnum }
     *     
     */
    public TypesOfFileableObjectsEnum getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link TypesOfFileableObjectsEnum }
     *     
     */
    public void setType(TypesOfFileableObjectsEnum value) {
        this.type = value;
    }

    /**
     * Gets the value of the depth property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getDepth() {
        return depth;
    }

    /**
     * Sets the value of the depth property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setDepth(BigInteger value) {
        this.depth = value;
    }

    /**
     * Gets the value of the filter property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFilter() {
        return filter;
    }

    /**
     * Sets the value of the filter property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFilter(String value) {
        this.filter = value;
    }

    /**
     * Gets the value of the includeAllowableActions property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIncludeAllowableActions() {
        return includeAllowableActions;
    }

    /**
     * Sets the value of the includeAllowableActions property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIncludeAllowableActions(Boolean value) {
        this.includeAllowableActions = value;
    }

}
