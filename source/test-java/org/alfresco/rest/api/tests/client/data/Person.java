package org.alfresco.rest.api.tests.client.data;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.text.Collator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.rest.api.tests.client.PublicApiClient.ExpectedPaging;
import org.alfresco.rest.api.tests.client.PublicApiClient.ListResponse;
import org.alfresco.service.namespace.QName;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sun.star.uno.RuntimeException;

public class Person implements Serializable, Comparable<Person>, ExpectedComparison
{
	private static final long serialVersionUID = 3185698391792389751L;

	private String id;
	private Boolean enabled;
	private String username;
	private String firstName;
	private String lastName;
	private Company company;
	private String skype;
	private String location;
	private String tel;
	private String mob;
	private String instantmsg;
	private String google;

	private static Collator collator = Collator.getInstance();

	public Person(String id, String username, Boolean enabled, String firstName, String lastName,
			Company company, String skype, String location, String tel,
			String mob, String instantmsg, String google)
	{
		super();
		if(id == null)
		{
			throw new IllegalArgumentException();
		}
		this.id = id;
		this.username = username;
		this.enabled = enabled;
		this.firstName = firstName;
		this.lastName = lastName;
		this.company = company;
		this.skype = skype;
		this.location = location;
		this.tel = tel;
		this.mob = mob;
		this.instantmsg = instantmsg;
		this.google = google;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public String getEmail() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getSkype() {
		return skype;
	}

	public void setSkype(String skype) {
		this.skype = skype;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getTel() {
		return tel;
	}

	public void setTel(String tel) {
		this.tel = tel;
	}

	public String getMob() {
		return mob;
	}

	public void setMob(String mob) {
		this.mob = mob;
	}

	public String getInstantmsg() {
		return instantmsg;
	}

	public void setInstantmsg(String instantmsg) {
		this.instantmsg = instantmsg;
	}

	public String getGoogle() {
		return google;
	}

	public void setGoogle(String google) {
		this.google = google;
	}

	public String getFirstName()
	{
		return firstName;
	}

	public Company getCompany()
	{
		return company;
	}

	public void setCompany(Company company)
	{
		this.company = company;
	}

	public void setFirstName(String firstName)
	{
		this.firstName = firstName;
	}

	public String getLastName() 
	{
		return lastName;
	}

	public void setLastName(String lastName)
	{
		this.lastName = lastName;
	}
	
	@Override
	public String toString()
	{
		return "Person [" + (id != null ? "id=" + id + ", " : "")
				+ (enabled != null ? "enabled=" + enabled + ", " : "")
				+ (username != null ? "email=" + username + ", " : "")
				+ (firstName != null ? "firstName=" + firstName + ", " : "")
				+ (lastName != null ? "lastName=" + lastName + ", " : "")
				+ (company != null ? "company=" + company + ", " : "")
				+ (skype != null ? "skype=" + skype + ", " : "")
				+ (location != null ? "location=" + location + ", " : "")
				+ (tel != null ? "tel=" + tel + ", " : "")
				+ (mob != null ? "mob=" + mob + ", " : "")
				+ (instantmsg != null ? "instantmsg=" + instantmsg + ", " : "")
				+ (google != null ? "google=" + google + ", " : "")
				+ "]";
	}

	@SuppressWarnings("unchecked")
	public JSONObject toJSON(boolean fullVisibility)
	{
		JSONObject personJson = new JSONObject();
		personJson.put("id", getId());
		personJson.put("firstName", getFirstName());
		personJson.put("lastName", getLastName());
		if(fullVisibility)
		{
			personJson.put("skypeId", "skype");
			personJson.put("location", "location");
			personJson.put("telephone", "tel");
			personJson.put("mobile", "mob");
			personJson.put("instantMessageId", "instantmsg");
			personJson.put("googleId", "google");

			personJson.put("company", company.toJSON());

		}
		return personJson;
	}
	
	public static Person parsePerson(JSONObject jsonObject)
	{
		Boolean enabled = (Boolean)jsonObject.get("enabled");
		String location = (String)jsonObject.get("location");
		String instantMessageId = (String)jsonObject.get("instantMessageId");
		String googleId = (String)jsonObject.get("googleId");
		String skypeId = (String)jsonObject.get("skypeId");
		String email = (String)jsonObject.get("email");
		String telephone = (String)jsonObject.get("telephone");
		String mobile = (String)jsonObject.get("mobile");
		String userId = (String)jsonObject.get("id");
		String firstName = (String)jsonObject.get("firstName");
		String lastName = (String)jsonObject.get("lastName");
		Boolean emailNotificationsEnabled = (Boolean)jsonObject.get("emailNotificationsEnabled");
		JSONObject companyJSON = (JSONObject)jsonObject.get("company");
		Company company = null;
		if(companyJSON != null)
		{
			String organization = (String)companyJSON.get("organization");
			String address1 = (String)companyJSON.get("address1");
			String address2 = (String)companyJSON.get("address2");
			String address3 = (String)companyJSON.get("address3");
			String postcode = (String)companyJSON.get("postcode");
			String companyTelephone = (String)companyJSON.get("telephone");
			String fax = (String)companyJSON.get("fax");
			String companyEmail = (String)companyJSON.get("email");
			company = new Company(organization, address1, address2, address3, postcode, companyTelephone, fax, companyEmail);
		}
		Person person = new Person(userId, email, enabled, firstName, lastName, company, skypeId, location, telephone, mobile, instantMessageId, googleId);
		return person;
	}
	
	public Person restriced()
	{
		Person p = new Person(getId(), getEmail(), getEnabled(), getFirstName(), getLastName(), null, null, null, null, null, null, null);
		return p;
	}

	private static class UserContext
	{
		private String networkId;
		private String personId;

		UserContext(String networkId, String personId)
		{
			super();
			this.networkId = networkId;
			this.personId = personId;
		}

		String getNetworkId()
		{
			return networkId;
		}

		String getPersonId()
		{
			return personId;
		}
	}

	private static ThreadLocal<UserContext> userContext = new ThreadLocal<UserContext>();
	public static void setUserContext(String personId)
	{
		String networkId = Person.getNetworkId(personId);
		userContext.set(new UserContext(networkId, personId));
	}
	
	public static void clearUserContext()
	{
		userContext.set(null);
	}
	
	public static UserContext gettUserContext()
	{
		return userContext.get();
	}
	
	public static String getNetworkId(String personId)
	{
		int idx = personId.indexOf("@");
		return(idx == -1 ? TenantService.DEFAULT_DOMAIN : personId.substring(idx + 1));
	}

	private String getNetworkId()
	{
		return Person.getNetworkId(id);
	}
	
	public boolean isVisible()
	{
		boolean ret = true;

		UserContext uc = gettUserContext();
		String networkId = getNetworkId();
		if(uc != null)
		{
			if(!networkId.equals(uc.getNetworkId()))
			{
				ret = false;
			}
		}

		return ret;
	}
	
	@Override
	public void expected(Object o)
	{
		assertTrue("o is an instance of " + o.getClass(), o instanceof Person);

		Person other = (Person)o;
		
		AssertUtil.assertEquals("userId", id, other.getId());
		AssertUtil.assertEquals("firstName", firstName, other.getFirstName());
		AssertUtil.assertEquals("lastName", lastName, other.getLastName());
		AssertUtil.assertEquals("enabled", enabled, other.getEnabled());

		if(isVisible())
		{
			AssertUtil.assertEquals("skype", skype, other.getSkype());
			AssertUtil.assertEquals("location", location, other.getLocation());
			AssertUtil.assertEquals("tel", tel, other.getTel());
			AssertUtil.assertEquals("mobile", mob, other.getMob());
			AssertUtil.assertEquals("instanceMessageId", instantmsg, other.getInstantmsg());
			AssertUtil.assertEquals("googleId", google, other.getGoogle());
			if(company != null)
			{
				company.expected(getCompany());
			}
		}
	}
	
	public Map<QName, Serializable> getProperties()
	{
    	final Map<QName, Serializable> props = new HashMap<QName, Serializable>();
    	
    	if(firstName != null)
    	{
    		props.put(ContentModel.PROP_FIRSTNAME, firstName);
    	}
    	
    	if(lastName != null)
    	{
    		props.put(ContentModel.PROP_LASTNAME, lastName);
    	}
    	
    	if(skype != null)
    	{
    		props.put(ContentModel.PROP_SKYPE, skype);
    	}
    	
    	if(location != null)
    	{
    		props.put(ContentModel.PROP_LOCATION, location);
    	}
    	
    	if(tel != null)
    	{
    		props.put(ContentModel.PROP_TELEPHONE, tel);
    	}

    	if(username != null)
    	{
    		props.put(ContentModel.PROP_USERNAME, username);
    	}
    	
    	if(mob != null)
    	{
    		props.put(ContentModel.PROP_MOBILE, mob);
    	}
    	
    	if(instantmsg != null)
    	{
    		props.put(ContentModel.PROP_INSTANTMSG, instantmsg);
    	}
    	
    	if(google != null)
    	{
    		props.put(ContentModel.PROP_GOOGLEUSERNAME, google);
    	}

    	if(company != null)
    	{
        	if(company.getOrganization() != null)
        	{
        		props.put(ContentModel.PROP_ORGANIZATION, company.getOrganization());
        	}
        	
        	if(company.getAddress1() != null)
        	{
        		props.put(ContentModel.PROP_COMPANYADDRESS1, company.getAddress1());
        	}
	    	
	    	if(company.getAddress2() != null)
	    	{
	    		props.put(ContentModel.PROP_COMPANYADDRESS2, company.getAddress2());
	    	}
	    	
	    	if(company.getAddress3() != null)
	    	{
	    		props.put(ContentModel.PROP_COMPANYADDRESS3, company.getAddress3());
	    	}
	    	
	    	if(company.getPostcode() != null)
	    	{
	    		props.put(ContentModel.PROP_COMPANYPOSTCODE, company.getPostcode());
	    	}
	    	
	    	if(company.getTelephone() != null)
	    	{
	    		props.put(ContentModel.PROP_COMPANYTELEPHONE, company.getTelephone());
	    	}
	    	
	    	if(company.getFax() != null)
	    	{
	    		props.put(ContentModel.PROP_COMPANYFAX, company.getFax());
	    	}
	    	
	    	if(company.getEmail() != null)
	    	{
	    		props.put(ContentModel.PROP_COMPANYEMAIL, company.getEmail());
	    	}
    	}

    	return props;
	}
	
	@Override
	public int compareTo(Person o)
	{
		int ret = Person.collator.compare(lastName, o.getLastName());
		if(ret == 0)
		{
			ret = Person.collator.compare(firstName, o.getFirstName());
		}
		return ret;
	}

	public static ListResponse<Person> parsePeople(JSONObject jsonObject)
	{
		List<Person> people = new ArrayList<Person>();

		JSONObject jsonList = (JSONObject)jsonObject.get("list");
		assertNotNull(jsonList);

		JSONArray jsonEntries = (JSONArray)jsonList.get("entries");
		assertNotNull(jsonEntries);

		for(int i = 0; i < jsonEntries.size(); i++)
		{
			JSONObject jsonEntry = (JSONObject)jsonEntries.get(i);
			JSONObject entry = (JSONObject)jsonEntry.get("entry");
			people.add(parsePerson(entry));
		}

		ExpectedPaging paging = ExpectedPaging.parsePagination(jsonList);
		ListResponse<Person> resp = new ListResponse<Person>(paging, people);
		return resp;
	}
	
}
