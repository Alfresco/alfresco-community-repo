
package org.alfresco.repo.cmis.ws;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for query element declaration.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;element name="query">
 *   &lt;complexType>
 *     &lt;complexContent>
 *       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *         &lt;sequence>
 *           &lt;element name="repositoryId" type="{http://www.cmis.org/ns/1.0}ID"/>
 *           &lt;element name="statement" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *           &lt;element name="searchAllVersions" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *           &lt;element name="includeAllowableActions" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
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
    "repositoryId",
    "statement",
    "searchAllVersions",
    "includeAllowableActions",
    "maxItems",
    "skipCount"
})
@XmlRootElement(name = "query")
public class Query {

    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected String repositoryId;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected String statement;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected Boolean searchAllVersions;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected Boolean includeAllowableActions;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected BigInteger maxItems;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected BigInteger skipCount;

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
     * Gets the value of the statement property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStatement() {
        return statement;
    }

    /**
     * Sets the value of the statement property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStatement(String value) {
        this.statement = value;
    }

    /**
     * Gets the value of the searchAllVersions property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isSearchAllVersions() {
        return searchAllVersions;
    }

    /**
     * Sets the value of the searchAllVersions property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSearchAllVersions(Boolean value) {
        this.searchAllVersions = value;
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
