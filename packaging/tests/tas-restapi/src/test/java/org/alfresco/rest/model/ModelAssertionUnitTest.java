/*-
 * #%L
 * alfresco-tas-restapi
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.rest.model;

import static com.google.common.collect.Sets.newHashSet;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.google.common.collect.ImmutableMap;

import org.alfresco.rest.core.IRestModel;
import org.testng.annotations.Test;

public class ModelAssertionUnitTest
{
	private static final String DEFAULT_ID = "1234";
	private static final String DEFAULT_NAME = "test";

	@Test(groups = "unit")
	public void iCanAssertExistingProperty() {
		Person p = new Person(DEFAULT_ID);
		p.assertThat().field("id").is("1234");
	}

	@Test(groups = "unit")
	public void iCanAssertExistingPropertyNegative() {
		Person p = new Person(DEFAULT_ID);
		p.assertThat().field("id").isNot("12342");
		RestPersonModel rp = new RestPersonModel();

		rp.getFirstName();
	}

	@Test(groups = "unit", expectedExceptions = AssertionError.class)
	public void iHaveOneExceptionThrownWithSelfExplanatoryMessageOnMissingField() {
		Person p = new Person();
		p.assertThat().field("id2").is("12342");

	}

	@Test(groups = "unit")
	public void iCanTakeTheValueOfFieldsThatDoesntHaveGetters() {
		Person p = new Person(DEFAULT_ID, DEFAULT_NAME);

		p.assertThat().field("name").is("test");

	}

	@Test(groups = "unit")
	public void iCanAssertStringIsEmpty() {
		Person p = new Person(DEFAULT_ID);

		// Check no exception when field is empty.
		p.assertThat().field("nickname").isEmpty();

		try {
			p.assertThat().field("id").isEmpty();
			fail("Expected exception to be raised.");
		} catch (AssertionError e) {
			assertTrue(e.getMessage().contains("is NOT empty"), "Expected exception to be about a field not being empty.");
		}
	}

	@Test(groups = "unit")
	public void iCanAssertStringIsNotEmpty() {
		Person p = new Person(DEFAULT_ID);

		// Check no exception when field is not empty.
		p.assertThat().field("id").isNotEmpty();

		try {
			p.assertThat().field("nickname").isNotEmpty();
			fail("Expected exception to be raised.");
		} catch (AssertionError e) {
			assertTrue(e.getMessage().contains("is empty"), "Expected exception to be about a field being empty.");
		}
	}

	@Test(groups = "unit")
	public void iCanAssertCollectionIsEmpty() {
		Person p = new Person();

		// Check no exception when field is empty.
		p.assertThat().field("previousNames").isEmpty();

		try {
			p.assertThat().field("legs").isEmpty();
			fail("Expected exception to be raised.");
		} catch (AssertionError e) {
			assertTrue(e.getMessage().contains("is NOT empty"), "Expected exception to be about a field not being empty.");
		}
	}

	@Test(groups = "unit")
	public void iCanAssertCollectionIsNotEmpty() {
		Person p = new Person();

		// Check no exception when field is not empty.
		p.assertThat().field("legs").isNotEmpty();

		try {
			p.assertThat().field("previousNames").isNotEmpty();
			fail("Expected exception to be raised.");
		} catch (AssertionError e) {
			assertTrue(e.getMessage().contains("is empty"), "Expected exception to be about a field being empty.");
		}
	}

	@Test(groups = "unit")
	public void iCanAssertMapIsEmpty() {
		Person p = new Person();

		// Check no exception when field is empty.
		p.assertThat().field("carrying").isEmpty();

		try {
			p.assertThat().field("clothing").isEmpty();
			fail("Expected exception to be raised.");
		} catch (AssertionError e) {
			assertTrue(e.getMessage().contains("is NOT empty"), "Expected exception to be about a field not being empty.");
		}
	}

	@Test(groups = "unit")
	public void iCanAssertMapIsNotEmpty() {
		Person p = new Person();

		// Check no exception when field is not empty.
		p.assertThat().field("clothing").isNotEmpty();

		try {
			p.assertThat().field("carrying").isNotEmpty();
			fail("Expected exception to be raised.");
		} catch (AssertionError e) {
			assertTrue(e.getMessage().contains("is empty"), "Expected exception to be about a field being empty.");
		}
	}

	@Test(groups = "unit")
	public void iCanAssertIntIsNotEmpty() {
		Person p = new Person();

		p.assertThat().field("age").isNotEmpty();
	}

	@Test(groups = "unit")
	public void testIsEqualTo_noDifferencesNonIgnoredFields()
	{
		Person person = new Person(DEFAULT_ID, DEFAULT_NAME);
		Person otherPerson = new Person(DEFAULT_ID, DEFAULT_NAME);

		person.assertThat().isEqualTo(otherPerson);
	}

	@Test(groups = "unit")
	public void testIsEqualTo_differentNameIgnoreName()
	{
		Person person = new Person(DEFAULT_ID);
		Person otherPerson = new Person(DEFAULT_ID, DEFAULT_NAME);

		person.assertThat().isEqualTo(otherPerson, "name");
	}

	@Test(groups = "unit")
	public void testIsEqualTo_differentIdAndNameIgnoreIdAndName()
	{
		Person person = new Person();
		Person otherPerson = new Person(DEFAULT_ID, DEFAULT_NAME);

		person.assertThat().isEqualTo(otherPerson, "id", "name");
	}

	@Test(groups = "unit")
	public void testIsEqualTo_differentNameNonIgnoredFields()
	{
		Person person = new Person(DEFAULT_ID, DEFAULT_NAME);
		Person otherPerson = new Person(DEFAULT_ID, "otherName");

		try {
			person.assertThat().isEqualTo(otherPerson);
			fail("Expected exception to be raised.");
		} catch (AssertionError e) {
			assertTrue(e.getMessage().contains("are not equal"), "Expected exception to be about a not equal objects.");
		}
	}

	@Test(groups = "unit")
	public void testIsEqualTo_differentIdNonIgnoredFields()
	{
		Person person = new Person();
		Person otherPerson = new Person(DEFAULT_ID);

		try {
			person.assertThat().isEqualTo(otherPerson);
			fail("Expected exception to be raised.");
		} catch (AssertionError e) {
			assertTrue(e.getMessage().contains("are not equal"), "Expected exception to be about a not equal objects.");
		}
	}

	@Test(groups = "unit")
	public void testIsEqualTo_differentIdAndNameIgnoreName()
	{
		Person person = new Person();
		Person otherPerson = new Person(DEFAULT_ID, DEFAULT_NAME);

		try {
			person.assertThat().isEqualTo(otherPerson, "name");
			fail("Expected exception to be raised.");
		} catch (AssertionError e) {
			assertTrue(e.getMessage().contains("are not equal"), "Expected exception to be about a not equal objects.");
		}
	}

	@Test(groups = "unit")
	public void testIsEqualTo_differentNicknameIgnoreIdAndName()
	{
		Person person = new Person();
		Person otherPerson = new Person();
		otherPerson.setNickname("Shaggy");

		try {
			person.assertThat().isEqualTo(otherPerson, "id", "name");
			fail("Expected exception to be raised.");
		} catch (AssertionError e) {
			assertTrue(e.getMessage().contains("are not equal"), "Expected exception to be about a not equal objects.");
		}
	}

	@Test(groups = "unit")
	public void testIsEqualTo_differentPreviousNamesIgnoreId()
	{
		Person person = new Person();
		Person otherPerson = new Person();
		otherPerson.setPreviousNames(List.of("Paul"));

		try {
			person.assertThat().isEqualTo(otherPerson, "id");
			fail("Expected exception to be raised.");
		} catch (AssertionError e) {
			assertTrue(e.getMessage().contains("are not equal"), "Expected exception to be about a not equal objects.");
		}
	}

	public static class Person implements IRestModel<Person> {
		private String id;
		private String name;
		private String nickname = "";
		private int age = 42;
		private Set<String> legs = newHashSet("left", "right");
		private List<String> previousNames = Collections.emptyList();
		private Map<String, String> clothing = ImmutableMap.of("head", "hat");
		private Map<String, String> carrying = Collections.emptyMap();

		public Person()
		{
		}

		public Person(String id)
		{
			this.id = id;
		}

		public Person(String id, String name)
		{
			this.id = id;
			this.name = name;
		}

		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public String getName() { return name;}
		public void setName(String name)
		{
			this.name = name;
		}
		public String getNickname() { return nickname; }
		public void setNickname(String nickname)
		{
			this.nickname = nickname;
		}
		public int getAge() { return age; }
		public Set<String> getLegs() { return legs; }
		public List<String> getPreviousNames() { return previousNames; }
		public void setPreviousNames(List<String> previousNames)
		{
			this.previousNames = previousNames;
		}
		public Map<String, String> getClothing() { return clothing; }
		public Map<String, String> getCarrying() { return carrying; }

		@Override
		public Person onModel() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String toString()
		{
			return "Person{" + "id='" + id + '\'' + ", name='" + name + '\'' + ", nickname='" + nickname + '\'' + ", age=" + age + ", legs=" + legs + ", previousNames="
				+ previousNames + ", clothing=" + clothing + ", carrying=" + carrying + '}';
		}

		@Override
		public boolean equals(Object o)
		{
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			Person person = (Person) o;
			return age == person.age && Objects.equals(id, person.id) && Objects.equals(name, person.name) && Objects.equals(nickname, person.nickname) && Objects.equals(
				legs, person.legs) && Objects.equals(previousNames, person.previousNames) && Objects.equals(clothing, person.clothing) && Objects.equals(carrying, person.carrying);
		}

		@Override
		public int hashCode()
		{
			return Objects.hash(id, name, nickname, age, legs, previousNames, clothing, carrying);
		}
	}
}
