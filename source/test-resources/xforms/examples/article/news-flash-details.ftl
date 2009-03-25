<#ftl ns_prefixes={"D":"http://www.alfresco.org/alfresco/article","xsi":"http://www.w3.org/2001/XMLSchema-instance"}>
<style>
	.article_details {
		background: #fff;
		padding: 10px;
		position: relative;
		font-family: helvetica;
		font-size: 12px;
	}

	.article_title{
		padding: 8px;
		font-size: 20px;
		color: #666;
	}
	
	.article_author {
		background: #fff;
		border: 1px solid #e2e1e1;
		position: relative;
		padding:10px;
	}
	

	.article_teaser {
		clear: left;
		margin: 10px 0 10px 0px;
	}

</style>
<div class="article_details">

	<div class="article_title">
		${news_flash.title}
	</div>	


	<div class="article_teaser">
		<span>${news_flash.location} ${news_flash.date}</span> - ${news_flash.body}
	</div>

</div>


