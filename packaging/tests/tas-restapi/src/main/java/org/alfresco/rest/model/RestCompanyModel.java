package org.alfresco.rest.model;

import org.alfresco.rest.core.assertion.ModelAssertion;
import org.alfresco.utility.model.TestModel;

/**
  "company": {
      "organization": "myCompany",
      "address1": "Iasi",
      "address2": "City",
      "address3": "trei",
      "postcode": "1234556",
      "telephone": "564510213453",
      "fax": "541",
      "email": "qLZ41D2QnXQcqySNK09J@yahoo.com"
    }
 */
public class RestCompanyModel extends TestModel
{
    private String organization;
    private String address1;
    private String address2;
    private String address3;
    private String postcode;
    private String telephone;
    private String fax;
    private String email;

    public ModelAssertion<RestCompanyModel> assertThat()
    {
        return new ModelAssertion<RestCompanyModel>(this);
    }

    public ModelAssertion<RestCompanyModel> and()
    {
        return assertThat();
    }
    
    public String getOrganization()
    {
        return organization;
    }

    public void setOrganization(String organization)
    {
        this.organization = organization;
    }

    public String getAddress1()
    {
        return address1;
    }

    public void setAddress1(String address1)
    {
        this.address1 = address1;
    }

    public String getAddress2()
    {
        return address2;
    }

    public void setAddress2(String address2)
    {
        this.address2 = address2;
    }

    public String getAddress3()
    {
        return address3;
    }

    public void setAddress3(String address3)
    {
        this.address3 = address3;
    }

    public String getPostcode()
    {
        return postcode;
    }

    public void setPostcode(String postcode)
    {
        this.postcode = postcode;
    }

    public String getTelephone()
    {
        return telephone;
    }

    public void setTelephone(String telephone)
    {
        this.telephone = telephone;
    }

    public String getFax()
    {
        return fax;
    }

    public void setFax(String fax)
    {
        this.fax = fax;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

}