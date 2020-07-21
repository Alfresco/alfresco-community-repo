------------------------------------------------------------------------------
文書名:   ${document.name?html}
------------------------------------------------------------------------------

   <#if document.properties.title?exists>
タイトル:   ${document.properties.title?html}
   <#else>
タイトル:   なし
   </#if>
   <#if document.properties.description?exists>
説明:     ${document.properties.description?html}
   <#else>
説明:     なし
   </#if>
作成者:    ${document.properties.creator?html}
作成日時:  ${document.properties.created?datetime}
変更者:    ${document.properties.modifier?html}
変更日時:  ${document.properties.modified?datetime}
サイズ:     ${document.size / 1024} KB


コンテンツのリンク

コンテンツフォルダ:    ${contentFolderUrl?html}
コンテンツの URL:    ${contextUrl}/service/api/node/content/${document.storeType}/${document.storeId}/${document.id}/${document.name?html}
ダウンロード用 URL:  ${contextUrl}/service/api/node/content/${document.storeType}/${document.storeId}/${document.id}/${document.name?html}?a=true
WebDAV の URL:   ${contextUrl}${document.webdavUrl?html}
