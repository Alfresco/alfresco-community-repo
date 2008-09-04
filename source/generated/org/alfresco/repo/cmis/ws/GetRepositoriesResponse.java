
package org.alfresco.repo.cmis.ws;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getRepositoriesResponse element declaration.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;element name="getRepositoriesResponse">
 *   &lt;complexType>
 *     &lt;complexContent>
 *       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *         &lt;sequence>
 *           &lt;element name="repository" type="{http://www.cmis.org/ns/1.0}repositoryType" maxOccurs="unbounded" minOccurs="0"/>
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
    "repository"
})
@XmlRootElement(name = "getRepositoriesResponse")
public class GetRepositoriesResponse {

    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected List<RepositoryType> repository;

    /**
     * Gets the value of the repository property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the repository property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRepository().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link RepositoryType }
     * 
     * 
     */
    public List<RepositoryType> getRepository() {
        if (repository == null) {
            repository = new ArrayList<RepositoryType>();
        }
        return this.repository;
    }

}
