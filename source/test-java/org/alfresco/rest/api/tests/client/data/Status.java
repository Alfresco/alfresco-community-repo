package org.alfresco.rest.api.tests.client.data;

import static org.junit.Assert.assertTrue;

import java.io.Serializable;

public class Status implements Serializable, ExpectedComparison
{
	private static final long serialVersionUID = 7187248670785910438L;

	public static enum State
	{
		OK, ERROR;
	};

	private State state;
	private String error;

	public Status(State state)
	{
		this.state = state;
	}

	public Status(String error)
	{
		this.state = State.ERROR;
		this.error = error;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public State getState()
	{
		return state;
	}

	public String getError()
	{
		return error;
	}

	@Override
	public String toString()
	{
		return "Status [state=" + state + ", error=" + error + "]";
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((error == null) ? 0 : error.hashCode());
		result = prime * result + ((state == null) ? 0 : state.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Status other = (Status) obj;
		if (error == null) {
			if (other.error != null)
				return false;
		} else if (!error.equals(other.error))
			return false;
		if (state != other.state)
			return false;
		return true;
	}

	@Override
	public void expected(Object o)
	{
		assertTrue(o instanceof Status);
		
		Status other = (Status)o;
		AssertUtil.assertEquals("state", state, other.getState());
		AssertUtil.assertEquals("error", error, other.getError());
	}
}
