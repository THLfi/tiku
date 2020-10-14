[#ftl]<!DOCTYPE html>
[#include "common.ftl" /]

[#setting locale=lang]

<html>
    <head>
        <title>${cubeLabel.getValue(lang)} - ${message("site.title")}</title>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">

        <link rel="stylesheet" href="${resourceUrl}/css/bootstrap.min.css" />
        <link rel="stylesheet"
    href="https://fonts.googleapis.com/css?family=Source+Sans+Pro:400,400italic,600,600italic,700,700italic" />
        <link rel="stylesheet" href="${rc.contextPath}/css/style.css" />

        <!--[if lt IE 9]>
          <script src="${resourceUrl}/js/html5shiv.js"></script>
          <script src="${resourceUrl}/js/respond.min.js"></script>
        <![endif]-->
        <style>


        </style>
    </head>
    <body class="common">

    <div class="pivot-header">
        <h1>${cubeLabel.getValue(lang)}</h1>
        <div class="logo">
            <img src="${resourceUrl}/images/THL_tunnus_pitka_${uiLanguage!"fi"}_RGB.svg">
        </div>
        <div class="toolbar-control">
        </div>
    </div>
    <div class="pivot-body">
        <div class="pivot-sidebar">

        </div>

        <div class="pivot-content">
               <form class="form col-xs-6" method="POST">

                    <p>${message("login.intro")}</p>

                    [#if authenticationFailed]
                        <div class="row">
                            <div class="alert alert-danger">
                                ${message("login.failed")}
                            </div>
                        </div>
                    [/#if]
                    <input type="hidden" name="csrf" value="${csrf!}" />
                    <div class="form-group">
                        <label for="pwd">${message("login.password")}</label>
                        <input class="form-control" type="password" name="password" id="pwd" autofocus />
                    </div>
                    <button type="submit" class="btn btn-primary">${message("login.login")}</button>
               </form>
        </div>

    </div>
	[@footer/]

    </body>
</html>
