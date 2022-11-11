[#ftl]<!DOCTYPE html>
[#setting locale="fi"]
[#assign dimensionSeparator = '-' /]
[#assign nodeSeparator = '.' /]
[#function value val]
    [#return val /]
[/#function]

[#function message code]
    [#if rc.getMessage(code)??]
        [#return rc.getMessage(code, "[${code}]") /]
    [/#if]
    [#return "- ${code} - " /]
[/#function]

[#macro label e][#if e?? && e.label??]${e.label.getValue(lang)} [#else]???[/#if][/#macro]

[#include "cube/cube-dimension-dropdown.ftl" /]


<html lang="${lang!'fi'}">
    <head>
        <title>[#if cubeLabel??]${cubeLabel.getValue(lang)}[#else]n/a[/#if] - ${message("site.title")}</title>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">

        <link rel="stylesheet" href="${rc.contextPath}/css/bootstrap.min.css">
        <link rel="stylesheet" href="${rc.contextPath}/css/fontawesome.min.css">
        <link rel="stylesheet" href="${rc.contextPath}/css/solid.min.css">
        <link rel="stylesheet" href="${rc.contextPath}/fonts/source-sans-pro/2.0.10/source-sans-pro.css">
        <link rel="stylesheet" href="${rc.contextPath}/css/jquery.stickytable.min.css">
        <link rel="stylesheet" href="${rc.contextPath}/css/style.css?v=${buildTimestamp}">
        <link href="${rc.contextPath}/images/favicon.ico" rel="shortcut icon" type="image/x-icon">
        <!--[if lt IE 9]>
          <script src="${rc.contextPath}/js/html5shiv.js"></script>
          <script src="${rc.contextPath}/js/respond.min.js"></script>
        <![endif]-->

        <script src="${rc.contextPath}/js/bootstrap.bundle.min.js"></script>
    </head>
    <body>

    <div class="pivot-header" role="banner" >
        <h1>
          [#if cubeLabel??]${cubeLabel.getValue(lang)}[#else]n/a[/#if]
          [#if env != "prod"]
          <span class="environment">${env}</span>
          [/#if]
        </h1>
        <div class="logo">
            <img src="${rc.contextPath}/images/THL_tunnus_pitka_${uiLanguage!'fi'}_RGB.svg" alt="${message('site.thl.logo.alt')}">
        </div>
        
        <div id="languages" role="navigation">
        
          [#if supportedLanguages?? && supportedLanguages?size > 1]
            <ul>
             <li><div class="hide-xl btn-group vlcube"></div></li>
            [#list supportedLanguages as x]
              [#if x != lang]
                <li><a href="${rc.contextPath}/${cubeRequest.getCubeUrl(x)}">${x}</a></li>
              [#else]
                <li class="active"><a href="${rc.contextPath}/${cubeRequest.cubeUrl}">${x}</a></li>
              [/#if]
            [/#list]
          [/#if]
          </ul>
        </div>
        [#include "cube/cube-form.ftl" /]
        <div id="toolbar" class="toolbar-control" role="navigation" aria-labelledby="toolbar">
            [#include "cube/cube-toolbar.ftl" /]
        </div>
    </div>

    <div class="pivot-body">
    <div class="pivot-sidebar" role="region">
         <button class="browser-toggle btn btn-default">
            <i class="fas fa-sliders-h"></i>
            <span class="sr-only">${message("cube.options")}</span>
         </button>
         <ul class="tree-browser">

         </ul>

    </div>

    <div class="pivot-content" role="main">

        <div class="quick-info alert alert-warning alert-dismissible fade show d-none" role="alert">
            <div class="col-sm-11">${message("cube.quick-info")}</div>

            <button id="close-quick-info" type="button" class="close" data-bs-dismiss="alert" aria-label="Sulje">
                <span aria-hidden="true">&times;</span>
            </button>
        </div>

        <div class="float-end">
            <span class="fas fa-expand-alt fa-expand-alt-cube"></span>
        </div>

        [#include "cube/cube-filter.ftl" /]

        [#if pivot.columns?size == 0 || pivot.rows?size == 0]
            [#include "cube/cube-manual.ftl" /]
            [#include "cube/cube-placeholder.ftl" /]
        [#else]
            [#if pivot.columnCount == 0 && pivot.rowCount == 0]
                <p class="alert alert-warning">
                    ${message("cube.error.no-data")}
                </p>
            [#else]
            	[#include "cube/cube-table.ftl" /]
            [/#if]
        [/#if]

        [#include "cube/cube-modal.ftl" /]
    </div>

    </div>

    <div class="pivot-footer" role="contentinfo">
        &copy; ${message("site.company")} ${.now?string("yyyy")}[#if isOpenData], ${message("site.license.cc")}[/#if]. ${message("cube.updated")} ${runDate?string("dd.MM.yyyy")}
    </div>

    <script src="${rc.contextPath}/js/jquery.js"></script>
    <script src="${rc.contextPath}/js/jquery-ui.js"></script>
    <script src="${rc.contextPath}/js/jquery.ui.touch-punch.min.js"></script>
    <script src="${rc.contextPath}/js/jquery.stickytable.js"></script>

    <script nonce="${cspNonce}">
        [#include "cube/cube-script.ftl" /]
    </script>

    <script src="${rc.contextPath}/js/cube.min.js?v=${buildTimestamp}"></script>
    <script src="${rc.contextPath}/${dimensionsUrl}"></script>
    <script src="${rc.contextPath}/js/google-analytics.min.js"></script>

    </body>
</html>
