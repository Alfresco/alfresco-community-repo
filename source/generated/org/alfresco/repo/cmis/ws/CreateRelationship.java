
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
 *         &lt;element name="repositoryId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="typeId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="properties" type="{http://docs.oasis-open.org/ns/cmis/core/200901}cmisPropertiesType"/>
 *         &lt;element name="sourceObjectId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="targetObjectId" type="{http://www.w3.org/2001/XMLSchema}string"/>
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
    "repositoryId",
    "typeId",
    "properties",
    "sourceObjectId",
    "targetObjectId"
})
@XmlRootElement(name = "createRelationship")
public class CreateRelationship {

    @XmlElement(required = true)
    protected String repositoryId;
    @XmlElement(required = true)
    protected String typeId;
    @XmlElement(required = true)
    protected CmisPropertiesType properties;
    @XmlElement(required = true)
    protected String sourceObjectId;
    @XmlElement(required = true)
    protected String targetObjectId;

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
     * Gets the value of the properties property.
     * 
     * @return
     *     possible object is
     *     {@link CmisPropertiesType }
     *     
     */
    public CmisPropertiesType getProperties() {
        return properties;
    }

    /**
     * Sets the value of the properties property.
     * 
     * @param value
     *     allowed object is
     *     {@link CmisPropertiesType }
     *     
     */
    public void setProperties(CmisPropertiesType value) {
        this.properties = value;
    }

    /**
     * Gets the value of the sourceObjectId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSourceObjectId() {
        return sourceObjectId;
    }

    /**
     * Sets the value of the sourceObjectId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSourceObjectId(String value) {
        this.sourceObjectId = value;
    }

    /**
     * Gets the value of the targetObjectId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTargetObjectId() {
        return targetObjectId;
    }

    /**
     * Sets the value of the targetObjectId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTargetObjectId(String value) {
        this.targetObjectId = value;
    }

}
