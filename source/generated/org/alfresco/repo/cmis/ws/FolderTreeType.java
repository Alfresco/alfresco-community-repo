
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for folderTreeType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="folderTreeType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.cmis.org/ns/1.0}objectType">
 *       &lt;sequence>
 *         &lt;element name="children" type="{http://www.cmis.org/ns/1.0}childrenType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "folderTreeType", propOrder = {
    "children"
})
public class FolderTreeType
    extends ObjectType
{

    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected ChildrenType children;

    /**
     * Gets the value of the children property.
     * 
     * @return
     *     possible object is
     *     {@link ChildrenType }
     *     
     */
    public ChildrenType getChildren() {
        return children;
    }

    /**
     * Sets the value of the children property.
     * 
     * @param value
     *     allowed object is
     *     {@link ChildrenType }
     *     
     */
    public void setChildren(ChildrenType value) {
        this.children = value;
    }

}
