
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getRelationshipsResponse element declaration.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;element name="getRelationshipsResponse">
 *   &lt;complexType>
 *     &lt;complexContent>
 *       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *         &lt;sequence>
 *           &lt;element ref="{http://www.cmis.org/ns/1.0}relationshipCollection"/>
 *           &lt;element ref="{http://www.cmis.org/ns/1.0}hasMoreItems"/>
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
    "relationshipCollection",
    "hasMoreItems"
})
@XmlRootElement(name = "getRelationshipsResponse")
public class GetRelationshipsResponse {

    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected RelationshipCollection relationshipCollection;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected boolean hasMoreItems;

    /**
     * Gets the value of the relationshipCollection property.
     * 
     * @return
     *     possible object is
     *     {@link RelationshipCollection }
     *     
     */
    public RelationshipCollection getRelationshipCollection() {
        return relationshipCollection;
    }

    /**
     * Sets the value of the relationshipCollection property.
     * 
     * @param value
     *     allowed object is
     *     {@link RelationshipCollection }
     *     
     */
    public void setRelationshipCollection(RelationshipCollection value) {
        this.relationshipCollection = value;
    }

    /**
     * Gets the value of the hasMoreItems property.
     * 
     */
    public boolean isHasMoreItems() {
        return hasMoreItems;
    }

    /**
     * Sets the value of the hasMoreItems property.
     * 
     */
    public void setHasMoreItems(boolean value) {
        this.hasMoreItems = value;
    }

}
