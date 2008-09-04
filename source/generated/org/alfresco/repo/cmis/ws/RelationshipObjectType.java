
package org.alfresco.repo.cmis.ws;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for relationshipObjectType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="relationshipObjectType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.cmis.org/ns/1.0}objectTypeBase">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.cmis.org/ns/1.0}sourceOID" minOccurs="0"/>
 *         &lt;element ref="{http://www.cmis.org/ns/1.0}targetOID" minOccurs="0"/>
 *         &lt;element ref="{http://www.cmis.org/ns/1.0}property" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "relationshipObjectType", propOrder = {
    "sourceOID",
    "targetOID",
    "property"
})
public class RelationshipObjectType
    extends ObjectTypeBase
{

    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected String sourceOID;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected String targetOID;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected List<Property> property;

    /**
     * Gets the value of the sourceOID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSourceOID() {
        return sourceOID;
    }

    /**
     * Sets the value of the sourceOID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSourceOID(String value) {
        this.sourceOID = value;
    }

    /**
     * Gets the value of the targetOID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTargetOID() {
        return targetOID;
    }

    /**
     * Sets the value of the targetOID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTargetOID(String value) {
        this.targetOID = value;
    }

    /**
     * Gets the value of the property property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the property property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getProperty().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Property }
     * 
     * 
     */
    public List<Property> getProperty() {
        if (property == null) {
            property = new ArrayList<Property>();
        }
        return this.property;
    }

}
