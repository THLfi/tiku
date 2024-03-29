[#ftl]<!DOCTYPE html>
[#include "common.ftl" /]

[#setting locale=lang]

<html lang="${lang!'fi'}">
    <head>
        <title>${cubeLabel.getValue(lang)} - ${message("site.title")}</title>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">

        <link rel="stylesheet" href="${rc.contextPath}/css/bootstrap.min.css">
        <link rel="stylesheet" href="${rc.contextPath}/fonts/source-sans-pro/2.0.10/source-sans-pro.css">
        <link rel="stylesheet" href="${rc.contextPath}/css/style.css">
        <link href="${rc.contextPath}/images/favicon.ico" rel="shortcut icon" type="image/x-icon">

        <!--[if lt IE 9]>
          <script src="${rc.contextPath}/js/html5shiv.js"></script>
          <script src="${rc.contextPath}/js/respond.min.js"></script>
        <![endif]-->
        <script src="${rc.contextPath}/js/jquery.js"></script>
        <script src="${rc.contextPath}/js/jquery-ui.js"></script>
        <script src="${rc.contextPath}/js/jquery.ui.touch-punch.min.js"></script>
        <script src="${rc.contextPath}/js/bootstrap.bundle.min.js"></script>

        <script src="${rc.contextPath}/js/login-password-validation.min.js"></script>

        <style>


        </style>
    </head>
    <body class="common">

    <div class="pivot-header" role="banner">
        <h1>${cubeLabel.getValue(lang)}</h1>
        <div class="logo">
            <img src="${rc.contextPath}/images/THL_tunnus_pitka_${uiLanguage!'fi'}_RGB.svg" alt="${message('site.thl.logo.alt')}">
        </div>
        <div class="toolbar-control">
        </div>
    </div>
    <div class="pivot-body">
        <div class="pivot-sidebar">

        </div>

        <div class="pivot-content">
               <form class="form col-sm-6" method="POST">

                    <p>${message("login.intro")}</p>

                    [#if authenticationFailed]
                        <div class="row">
                            <div class="alert alert-danger">
                                ${message("login.failed")}
                            </div>
                        </div>
                    [/#if]

                   <div id="password-validation-error" class="row" hidden>
                       <div class="alert alert-danger">
                           ${message("login.password.invalid")}
                       </div>
                   </div>

                    <input type="hidden" name="csrf" value="${csrf!}">
                    <div class="form-group login-grp">
                        <label class="login-lbl" for="password">${message("login.password")}</label>
                        <input class="form-control" type="password" name="password" id="password" autofocus>
                    </div>
                    <button id="submitbutton" type="submit" class="btn btn-primary login-btn">${message("login.login")}</button>
               </form>
        </div>

    </div>
	[@footer/]

    <script src="${rc.contextPath}/js/google-analytics.min.js"></script>
    </body>
</html>
