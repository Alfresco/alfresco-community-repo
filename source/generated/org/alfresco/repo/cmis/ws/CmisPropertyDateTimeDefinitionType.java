
package org.alfresco.repo.cmis.ws;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for cmisPropertyDateTimeDefinitionType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="cmisPropertyDateTimeDefinitionType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://docs.oasis-open.org/ns/cmis/core/200908/}cmisPropertyDefinitionType">
 *       &lt;sequence>
 *         &lt;element name="defaultValue" type="{http://docs.oasis-open.org/ns/cmis/core/200908/}cmisPropertyDateTime" minOccurs="0"/>
 *         &lt;element name="resolution" type="{http://docs.oasis-open.org/ns/cmis/core/200908/}enumDateTimeResolution" minOccurs="0"/>
 *         &lt;element name="choice" type="{http://docs.oasis-open.org/ns/cmis/core/200908/}cmisChoiceDateTime" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "cmisPropertyDateTimeDefinitionType", namespace = "http://docs.oasis-open.org/ns/cmis/core/200908/", propOrder = {
    "defaultValue",
    "resolution",
    "choice"
})
public class CmisPropertyDateTimeDefinitionType
    extends CmisPropertyDefinitionType
{

    protected CmisPropertyDateTime defaultValue;
    protected EnumDateTimeResolution resolution;
    protected List<CmisChoiceDateTime> choice;

    /**
     * Gets the value of the defaultValue property.
     * 
     * @return
     *     possible object is
     *     {@link CmisPropertyDateTime }
     *     
     */
    public CmisPropertyDateTime getDefaultValue() {
        return defaultValue;
    }

    /**
     * Sets the value of the defaultValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link CmisPropertyDateTime }
     *     
     */
    public void setDefaultValue(CmisPropertyDateTime value) {
        this.defaultValue = value;
    }

    /**
     * Gets the value of the resolution property.
     * 
     * @return
     *     possible object is
     *     {@link EnumDateTimeResolution }
     *     
     */
    public EnumDateTimeResolution getResolution() {
        return resolution;
    }

    /**
     * Sets the value of the resolution property.
     * 
     * @param value
     *     allowed object is
     *     {@link EnumDateTimeResolution }
     *     
     */
    public void setResolution(EnumDateTimeResolution value) {
        this.resolution = value;
    }

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
     * {@link CmisChoiceDateTime }
     * 
     * 
     */
    public List<CmisChoiceDateTime> getChoice() {
        if (choice == null) {
            choice = new ArrayList<CmisChoiceDateTime>();
        }
        return this.choice;
    }

}
