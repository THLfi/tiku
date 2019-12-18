[#ftl]<!DOCTYPE html>

[#setting locale=lang]
[#function message code]
    [#if rc.getMessage(code)??]
        [#return rc.getMessage(code) /]
    [/#if]
    [#return "- ${code} - " /]
[/#function]

<html>
    <head>
        <title>${cubeLabel.getValue(lang)} - ${message("site.title")}</title>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">

        <link rel="stylesheet" href="${rc.contextPath}/resources/css/bootstrap.min.css" />
        <link rel="stylesheet"
    href="https://fonts.googleapis.com/css?family=Source+Sans+Pro:400,400italic,600,600italic,700,700italic" />
        <link rel="stylesheet" href="${rc.contextPath}/resources/css/style.css" />

        <!--[if lt IE 9]>
          <script src="${rc.contextPath}/resources/js/html5shiv.js"></script>
          <script src="${rc.contextPath}/resources/js/respond.min.js"></script>
        <![endif]-->
        <style>


        </style>
    </head>
    <body>

    <div class="pivot-header">
        <h1>${cubeLabel.getValue(lang)}</h1>
        <div class="logo">
            <img src="${rc.contextPath}/resources/img/THL_tunnus_pitka_${uiLanguage!"fi"}_RGB.svg">
        </div>
        <div class="toolbar-control">
        </div>
    </div>
    <div class="pivot-body">
        <div class="pivot-sidebar">

        </div>

        <div class="pivot-content">
               <form class="form col-xs-6" method="POST">

                    <p>${message("login.intro")}

                    [#if authenticationFailed]
                        <div class="row">
                            <div class="alert alert-danger">
                                ${message("login.failed")}
                            </div>
                        </div>
                    [/#if]
                    <div class="form-group">
                        <label for="pwd">${message("login.password")}</label>
                        <input class="form-control" type="password" name="password" id="pwd" autofocus />
                    </div>
                    <button type="submit" class="btn btn-primary">${message("login.login")}</button>
               </form>
        </div>

    </div>

    <div class="pivot-footer">
      &copy; ${message("site.company")} ${.now?string("yyyy")}
    </div>

    </body>
</html>
