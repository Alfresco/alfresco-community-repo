/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.web.scripts.thumbnail;

import java.io.InputStream;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.util.GUID;
import org.json.JSONObject;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Unit test to test thumbnail web script API
 * 
 * @author Roy Wetherall
 */
public class ThumbnailServiceTest extends BaseWebScriptTest
{    
    private NodeRef testRoot;
    private NodeRef pdfNode;
    private NodeRef jpgNode;
    
    private NodeService nodeService;
    private FileFolderService fileFolderService;
    private ContentService contentService;
    private Repository repositoryHelper;
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();       
 
        this.nodeService = (NodeService)getServer().getApplicationContext().getBean("NodeService");
        this.fileFolderService = (FileFolderService)getServer().getApplicationContext().getBean("FileFolderService");
        this.contentService = (ContentService)getServer().getApplicationContext().getBean("ContentService");
        this.repositoryHelper = (Repository)getServer().getApplicationContext().getBean("repositoryHelper");
        
        this.testRoot = this.repositoryHelper.getCompanyHome();
        
        // Get test content
        InputStream pdfStream = ThumbnailServiceTest.class.getClassLoader().getResourceAsStream("org/alfresco/repo/web/scripts/thumbnail/test_doc.pdf");        
        assertNotNull(pdfStream);
        InputStream jpgStream = ThumbnailServiceTest.class.getClassLoader().getResourceAsStream("org/alfresco/repo/web/scripts/thumbnail/test_image.jpg");
        assertNotNull(jpgStream);
        
        String guid = GUID.generate();
        
        // Create new nodes and set test content
        FileInfo fileInfoPdf = this.fileFolderService.create(this.testRoot, "test_doc" + guid + ".pdf", ContentModel.TYPE_CONTENT);
        this.pdfNode = fileInfoPdf.getNodeRef();
        ContentWriter contentWriter = this.contentService.getWriter(fileInfoPdf.getNodeRef(), ContentModel.PROP_CONTENT, true);
        contentWriter.setEncoding("UTF-8");
        contentWriter.setMimetype(MimetypeMap.MIMETYPE_PDF);
        contentWriter.putContent(pdfStream);
        
        FileInfo fileInfoJpg = this.fileFolderService.create(this.testRoot, "test_image" + guid + ".jpg", ContentModel.TYPE_CONTENT);
        this.jpgNode = fileInfoJpg.getNodeRef();
        contentWriter = this.contentService.getWriter(fileInfoJpg.getNodeRef(), ContentModel.PROP_CONTENT, true);
        contentWriter.setEncoding("UTF-8");
        contentWriter.setMimetype(MimetypeMap.MIMETYPE_IMAGE_JPEG);
        contentWriter.putContent(jpgStream);        
    }    
    
    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();

    }
    
    public void testCreateThumbnail() throws Exception
    {
        String url = "/api/node/" + pdfNode.getStoreRef().getProtocol() + "/" + pdfNode.getStoreRef().getIdentifier() + "/" + pdfNode.getId() + "/content/thumbnails";
        System.out.println(url);
        
        JSONObject tn = new JSONObject();
        tn.put("thumbnailName", "webpreview");
        System.out.println(tn.toString());
        
        MockHttpServletResponse response = this.postRequest(url, 200, tn.toString(), "application/json");
        //JSONObject result = new JSONObject(response.getContentAsString());
        
        System.out.println(response.getContentAsString());
    }
    
    
   
}
