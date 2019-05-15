package org.alfresco.rest.model;

import org.alfresco.rest.core.IRestModel;
import org.alfresco.rest.core.assertion.ModelAssertion;
import org.alfresco.utility.model.TestModel;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author cmocanu 
 * Base Path
 *         {@linkplain /alfresco/api/-default-/public/alfresco/versions/1}
 */

public class RestRenditionInfoModel extends TestModel implements IRestModel<RestRenditionInfoModel> {

	@Override
	public ModelAssertion<RestRenditionInfoModel> and() {
		return assertThat();
	}

	@Override
	public ModelAssertion<RestRenditionInfoModel> assertThat() {
		return new ModelAssertion<RestRenditionInfoModel>(this);
	}

	@JsonProperty(value = "entry")
	RestRenditionInfoModel model;

	@Override
	public RestRenditionInfoModel onModel() {
		return model;
	}
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public RestContentModel getContent() {
		return content;
	}

	public void setContent(RestContentModel content) {
		this.content = content;
	}

	private String id;
	private RestContentModel content;
	private String status;

}
