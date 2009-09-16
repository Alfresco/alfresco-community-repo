
package org.alfresco.repo.audit.model._3;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Application complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Application">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.alfresco.org/repo/audit/model/3.2}AuditPath">
 *       &lt;sequence>
 *         &lt;element name="PathMappings" type="{http://www.alfresco.org/repo/audit/model/3.2}PathMappings" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="name" use="required" type="{http://www.alfresco.org/repo/audit/model/3.2}NameAttribute" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Application", propOrder = {
    "pathMappings"
})
public class Application
    extends AuditPath
{

    @XmlElement(name = "PathMappings")
    protected List<PathMappings> pathMappings;
    @XmlAttribute(required = true)
    protected String name;

    /**
     * Gets the value of the pathMappings property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the pathMappings property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPathMappings().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PathMappings }
     * 
     * 
     */
    public List<PathMappings> getPathMappings() {
        if (pathMappings == null) {
            pathMappings = new ArrayList<PathMappings>();
        }
        return this.pathMappings;
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

}
