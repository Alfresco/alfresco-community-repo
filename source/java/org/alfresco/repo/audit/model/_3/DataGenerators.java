
package org.alfresco.repo.audit.model._3;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DataGenerators complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DataGenerators">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="DataGenerator" type="{http://www.alfresco.org/repo/audit/model/3.2}DataGenerator" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DataGenerators", propOrder = {
    "dataGenerator"
})
public class DataGenerators {

    @XmlElement(name = "DataGenerator", required = true)
    protected List<DataGenerator> dataGenerator;

    /**
     * Gets the value of the dataGenerator property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the dataGenerator property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDataGenerator().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DataGenerator }
     * 
     * 
     */
    public List<DataGenerator> getDataGenerator() {
        if (dataGenerator == null) {
            dataGenerator = new ArrayList<DataGenerator>();
        }
        return this.dataGenerator;
    }

}
