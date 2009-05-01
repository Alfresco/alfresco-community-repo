
package org.alfresco.repo.cmis.ws;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for cmisPropertyDecimalDefinitionType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="cmisPropertyDecimalDefinitionType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://docs.oasis-open.org/ns/cmis/core/200901}cmisPropertyDefinitionType">
 *       &lt;sequence>
 *         &lt;element name="defaultValue" type="{http://docs.oasis-open.org/ns/cmis/core/200901}cmisChoiceDecimalType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="precision" type="{http://docs.oasis-open.org/ns/cmis/core/200901}enumDecimalPrecision" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "cmisPropertyDecimalDefinitionType", namespace = "http://docs.oasis-open.org/ns/cmis/core/200901", propOrder = {
    "defaultValue",
    "precision"
})
public class CmisPropertyDecimalDefinitionType
    extends CmisPropertyDefinitionType
{

    protected List<CmisChoiceDecimalType> defaultValue;
    protected BigInteger precision;

    /**
     * Gets the value of the defaultValue property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the defaultValue property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDefaultValue().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CmisChoiceDecimalType }
     * 
     * 
     */
    public List<CmisChoiceDecimalType> getDefaultValue() {
        if (defaultValue == null) {
            defaultValue = new ArrayList<CmisChoiceDecimalType>();
        }
        return this.defaultValue;
    }

    /**
     * Gets the value of the precision property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getPrecision() {
        return precision;
    }

    /**
     * Sets the value of the precision property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setPrecision(BigInteger value) {
        this.precision = value;
    }

}
