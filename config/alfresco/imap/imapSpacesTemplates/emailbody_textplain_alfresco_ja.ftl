------------------------------------------------------------------------------
文書名:   ${document.name}
------------------------------------------------------------------------------

   <#if document.properties.title?exists>
タイトル:   ${document.properties.title}
   <#else>
タイトル:   なし
   </#if>
   <#if document.properties.description?exists>
説明:     ${document.properties.description}
   <#else>
説明:     なし
   </#if>
作成者:    ${document.properties.creator}
作成日時:  ${document.properties.created?datetime}
変更者:    ${document.properties.modifier}
変更日時:  ${document.properties.modified?datetime}
サイズ:     ${document.size / 1024} KB


コンテンツのリンク

コンテンツフォルダ:    ${contentFolderUrl}
コンテンツの URL:    ${contextUrl}/service/api/node/content/${document.storeType}/${document.storeId}/${document.id}/${document.name}
ダウンロード用 URL:  ${contextUrl}/service/api/node/content/${document.storeType}/${document.storeId}/${document.id}/${document.name}?a=true
WebDAV の URL:   ${contextUrl}${document.webdavUrl}
