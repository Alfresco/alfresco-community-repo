
package org.alfresco.repo.cmis.ws;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;


/**
 * <p>Java class for cmisChoiceType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="cmisChoiceType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://docs.oasis-open.org/ns/cmis/core/200901}choice" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attGroup ref="{http://docs.oasis-open.org/ns/cmis/core/200901}cmisUndefinedAttribute"/>
 *       &lt;attribute ref="{http://docs.oasis-open.org/ns/cmis/core/200901}key"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "cmisChoiceType", namespace = "http://docs.oasis-open.org/ns/cmis/core/200901", propOrder = {
    "choice"
})
@XmlSeeAlso({
    CmisChoiceDateTimeType.class,
    CmisChoiceStringType.class,
    CmisChoiceDecimalType.class,
    CmisChoiceUriType.class,
    CmisChoiceIntegerType.class,
    CmisChoiceXmlType.class,
    CmisChoiceBooleanType.class,
    CmisChoiceIdType.class,
    CmisChoiceHtmlType.class
})
public abstract class CmisChoiceType {

    @XmlElementRef(name = "choice", namespace = "http://docs.oasis-open.org/ns/cmis/core/200901", type = JAXBElement.class)
    protected List<JAXBElement<? extends CmisChoiceType>> choice;
    @XmlAttribute(namespace = "http://docs.oasis-open.org/ns/cmis/core/200901")
    protected String key;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();

    /**
     * Gets the value of the choice property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the choice property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getChoice().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link CmisChoiceUriType }{@code >}
     * {@link JAXBElement }{@code <}{@link CmisChoiceIntegerType }{@code >}
     * {@link JAXBElement }{@code <}{@link CmisChoiceXmlType }{@code >}
     * {@link JAXBElement }{@code <}{@link CmisChoiceHtmlType }{@code >}
     * {@link JAXBElement }{@code <}{@link CmisChoiceStringType }{@code >}
     * {@link JAXBElement }{@code <}{@link CmisChoiceDateTimeType }{@code >}
     * {@link JAXBElement }{@code <}{@link CmisChoiceIdType }{@code >}
     * {@link JAXBElement }{@code <}{@link CmisChoiceBooleanType }{@code >}
     * {@link JAXBElement }{@code <}{@link CmisChoiceDecimalType }{@code >}
     * {@link JAXBElement }{@code <}{@link CmisChoiceType }{@code >}
     * 
     * 
     */
    public List<JAXBElement<? extends CmisChoiceType>> getChoice() {
        if (choice == null) {
            choice = new ArrayList<JAXBElement<? extends CmisChoiceType>>();
        }
        return this.choice;
    }

    /**
     * Gets the value of the key property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getKey() {
        return key;
    }

    /**
     * Sets the value of the key property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setKey(String value) {
        this.key = value;
    }

    /**
     * Gets a map that contains attributes that aren't bound to any typed property on this class.
     * 
     * <p>
     * the map is keyed by the name of the attribute and 
     * the value is the string value of the attribute.
     * 
     * the map returned by this method is live, and you can add new attribute
     * by updating the map directly. Because of this design, there's no setter.
     * 
     * 
     * @return
     *     always non-null
     */
    public Map<QName, String> getOtherAttributes() {
        return otherAttributes;
    }

}
