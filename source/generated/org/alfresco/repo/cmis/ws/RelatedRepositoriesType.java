
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for relatedRepositoriesType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="relatedRepositoriesType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="repositoryID" type="{http://www.cmis.org/ns/1.0}ID"/>
 *         &lt;element name="repositoryName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="repositoryURI" type="{http://www.w3.org/2001/XMLSchema}anyURI"/>
 *         &lt;element name="repositoryRelationshipName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "relatedRepositoriesType", propOrder = {
    "repositoryID",
    "repositoryName",
    "repositoryURI",
    "repositoryRelationshipName"
})
public class RelatedRepositoriesType {

    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected String repositoryID;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected String repositoryName;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected String repositoryURI;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected String repositoryRelationshipName;

    /**
     * Gets the value of the repositoryID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRepositoryID() {
        return repositoryID;
    }

    /**
     * Sets the value of the repositoryID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRepositoryID(String value) {
        this.repositoryID = value;
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
     * Gets the value of the repositoryRelationshipName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRepositoryRelationshipName() {
        return repositoryRelationshipName;
    }

    /**
     * Sets the value of the repositoryRelationshipName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRepositoryRelationshipName(String value) {
        this.repositoryRelationshipName = value;
    }

}
