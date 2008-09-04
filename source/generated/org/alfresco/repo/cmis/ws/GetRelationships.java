
package org.alfresco.repo.cmis.ws;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getRelationships element declaration.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;element name="getRelationships">
 *   &lt;complexType>
 *     &lt;complexContent>
 *       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *         &lt;sequence>
 *           &lt;element name="objectId" type="{http://www.cmis.org/ns/1.0}objectID"/>
 *           &lt;element name="direction" type="{http://www.cmis.org/ns/1.0}relationshipDirectionEnum"/>
 *           &lt;element name="typeId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *           &lt;element name="includeSubRelationshipTypes" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *           &lt;element ref="{http://www.cmis.org/ns/1.0}filter" minOccurs="0"/>
 *           &lt;element ref="{http://www.cmis.org/ns/1.0}maxItems" minOccurs="0"/>
 *           &lt;element ref="{http://www.cmis.org/ns/1.0}skipCount" minOccurs="0"/>
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
    "objectId",
    "direction",
    "typeId",
    "includeSubRelationshipTypes",
    "filter",
    "maxItems",
    "skipCount"
})
@XmlRootElement(name = "getRelationships")
public class GetRelationships {

    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected String objectId;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected RelationshipDirectionEnum direction;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected String typeId;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected Boolean includeSubRelationshipTypes;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected String filter;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected BigInteger maxItems;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected BigInteger skipCount;

    /**
     * Gets the value of the objectId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getObjectId() {
        return objectId;
    }

    /**
     * Sets the value of the objectId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setObjectId(String value) {
        this.objectId = value;
    }

    /**
     * Gets the value of the direction property.
     * 
     * @return
     *     possible object is
     *     {@link RelationshipDirectionEnum }
     *     
     */
    public RelationshipDirectionEnum getDirection() {
        return direction;
    }

    /**
     * Sets the value of the direction property.
     * 
     * @param value
     *     allowed object is
     *     {@link RelationshipDirectionEnum }
     *     
     */
    public void setDirection(RelationshipDirectionEnum value) {
        this.direction = value;
    }

    /**
     * Gets the value of the typeId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTypeId() {
        return typeId;
    }

    /**
     * Sets the value of the typeId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTypeId(String value) {
        this.typeId = value;
    }

    /**
     * Gets the value of the includeSubRelationshipTypes property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIncludeSubRelationshipTypes() {
        return includeSubRelationshipTypes;
    }

    /**
     * Sets the value of the includeSubRelationshipTypes property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIncludeSubRelationshipTypes(Boolean value) {
        this.includeSubRelationshipTypes = value;
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
     * Gets the value of the maxItems property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getMaxItems() {
        return maxItems;
    }

    /**
     * Sets the value of the maxItems property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setMaxItems(BigInteger value) {
        this.maxItems = value;
    }

    /**
     * Gets the value of the skipCount property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getSkipCount() {
        return skipCount;
    }

    /**
     * Sets the value of the skipCount property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setSkipCount(BigInteger value) {
        this.skipCount = value;
    }

}
