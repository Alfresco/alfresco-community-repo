package org.alfresco.webdav;

import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FileType;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.report.Bug;
import org.alfresco.utility.report.Bug.Status;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.apache.commons.httpclient.HttpStatus;
import org.testng.annotations.Test;

public class RequestResponseTests extends WebDavTest
{
	@Bug(id = "MNT-17475", status = Status.FIXED)
	@Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE })
	@TestRail(section = { TestGroup.PROTOCOLS, TestGroup.WEBDAV }, executionType = ExecutionType.REGRESSION,
	         description = "Verify that the Content-Disposition response header value contains filename as attachment")
	public void contentDispositionResponseHeaderValueIsCorrect() throws Exception
	{
		FolderModel guestHomeFolder = FolderModel.getGuestHomeFolderModel();
		FileModel testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
		
		webDavProtocol.authenticateUser(dataUser.getAdminUser()).usingResource(guestHomeFolder).createFile(testFile)
				.and().assertThat().hasStatus(HttpStatus.SC_CREATED).and().assertThat().existsInWebdav().then()
				.assertThat().hasResponseHeaderValue(WebDavWrapper.RESPONSE_HEADER_CONTENT_TYPE, "text/plain")
				.and().assertThat().hasResponseHeaderValue(WebDavWrapper.RESPONSE_HEADER_CONTENT_DISPOSITION,
						"attachment; filename=\"" + testFile.getName() + "\"; filename*=UTF-8'");

	}
	
}
