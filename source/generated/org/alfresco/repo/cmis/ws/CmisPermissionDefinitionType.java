
package org.alfresco.repo.cmis.ws;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for cmisPermissionDefinitionType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="cmisPermissionDefinitionType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}language" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="aggregating" type="{http://docs.oasis-open.org/ns/cmis/core/200901}cmisPermissionDefinitionType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="aggregated" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="abstract" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "cmisPermissionDefinitionType", namespace = "http://docs.oasis-open.org/ns/cmis/core/200901", propOrder = {
    "description",
    "aggregating"
})
public class CmisPermissionDefinitionType {

    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "language")
    protected List<String> description;
    protected List<CmisPermissionDefinitionType> aggregating;
    @XmlAttribute
    protected String name;
    @XmlAttribute
    protected Boolean aggregated;
    @XmlAttribute(name = "abstract")
    protected Boolean _abstract;

    /**
     * Gets the value of the description property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the description property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDescription().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getDescription() {
        if (description == null) {
            description = new ArrayList<String>();
        }
        return this.description;
    }

    /**
     * Gets the value of the aggregating property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the aggregating property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAggregating().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CmisPermissionDefinitionType }
     * 
     * 
     */
    public List<CmisPermissionDefinitionType> getAggregating() {
        if (aggregating == null) {
            aggregating = new ArrayList<CmisPermissionDefinitionType>();
        }
        return this.aggregating;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the aggregated property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isAggregated() {
        return aggregated;
    }

    /**
     * Sets the value of the aggregated property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAggregated(Boolean value) {
        this.aggregated = value;
    }

    /**
     * Gets the value of the abstract property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isAbstract() {
        return _abstract;
    }

    /**
     * Sets the value of the abstract property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAbstract(Boolean value) {
        this._abstract = value;
    }

}
