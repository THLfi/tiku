[#ftl]<!DOCTYPE html>
[#include "common.ftl" /]

[#if lang??]
[#else]
	[#assign lang="fi" /]
[/#if]
[#setting locale=lang]

[#function message code]
    [#if rc.getMessage(code)??]
        [#return rc.getMessage(code) /]
    [/#if]
    [#return "- ${code} - " /]
[/#function]


<html>
    <head>
        <title>${message("site.error")} - ${message("site.title")}</title>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">


        <link rel="stylesheet" href="${rc.contextPath}/css/bootstrap.min.css">
        <link rel="stylesheet" href="${rc.contextPath}/fonts/source-sans-pro/2.0.10/source-sans-pro.css">
        <link rel="stylesheet" href="${rc.contextPath}/css/style.css">
        <link rel="stylesheet" href="${rc.contextPath}/css/summary.css">
        <!--[if lt IE 9]>
          <script src="${rc.contextPath}/js/html5shiv.js"></script>
          <script src="${rc.contextPath}/js/respond.min.js"></script>
        <![endif]-->


    </head>
    <body class="common">

    <header class="summary-header container">
        <h1>${message("site.error")}</h1>
        <div class="logo">
            <img src="${rc.contextPath}/images/THL_tunnus_pitka_${uiLanguage!'fi'}_RGB.svg" alt="${message('site.thl.logo.alt')}">
        </div>
    </header>

    <div class="stripe">
    </div>
    <div class="summary-body container">
   	 	<div class="summary-content">
			[#if "${status!}" == "404" || "${status!}" == "403" || "${status!}" == "-1000"]
				<h2>${message("site.error.title.${status}")}</h2>
				<p>${message("site.error.message.${status}")}</p>
			[#else]
	      <h2>${message("site.error.title")}</h2>
	     	<p>${message("site.error.message")}</p>
			[/#if]
      </div>
    </div>

    [@footer/]

    <script src="${rc.contextPath}/js/jquery.js"></script>
    <script src="${rc.contextPath}/js/jquery-ui.js"></script>
    <script src="${rc.contextPath}/js/jquery.ui.touch-punch.min.js"></script>
    <script src="${rc.contextPath}/js/bootstrap.bundle.min.js"></script>

    <script src="${rc.contextPath}/js/google-analytics.min.js"></script>
    </body>
</html>
