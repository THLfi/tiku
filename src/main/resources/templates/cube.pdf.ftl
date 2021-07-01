[#ftl]<?xml version='1.0' encoding='UTF-8'?>
<!DOCTYPE html>

[#setting locale="fi"]
[#function value val]
    [#if val?matches("\\d+,\\d+")][#return val?replace(",",".")?number?string(",##0.00")/][/#if]
    [#if val?matches("\\d+")][#return val?number?string(",##0")/][/#if]
    [#return val /]
[/#function]
[#function message code]
    [#return messageSource.getMessage(code) /]
[/#function]
[#macro label e][#if e?? && e.label??]${e.label.getValue(lang)}[#else]???[/#if][/#macro]
<html>
    <head>
        <title>THL Pivot</title>
        <style>
        body {
            font: 10px/14px arial sans-serif;
        }
        header, footer {
            display: block;
        }
        header {
            position: running(header);
            top: 0;
        }
        footer {
            position: running(footer);
            text-align: footer;
            bottom: 0;
        }

        @page {
            size: A4 landscape;
            @top-left {
                font: 10px arial, sans-serif;
                content: "[#if cubeLabel??]${cubeLabel.getValue(lang)}[#else]n/a[/#if]";
            }
            @top-right {
                font: 10px arial, sans-serif;
                content: counter(page) " (" counter(pages) ")";
            }
            @bottom-center {
                font: 10px arial, sans-serif;
                content: "&#169; ${message("site.company")} ${.now?string("yyyy")} [#if isOpenData], ${message("site.license.cc")?replace("</?a[^>]*>", "", "r")}[/#if]. ${message("cube.updated")} ${updated?string("dd.MM.yyyy")}";
            }
        }

        /* Rules for page breaks inside tables */
          table { page-break-after:auto;
               -fs-table-paginate: paginate; }
          tr    { page-break-inside:avoid; }
          td    { page-break-inside:avoid; page-break-after:auto; }
          thead { display:table-header-group; }
          tfoot { display:table-footer-group; }

        /* Rules for styling tables */
        table {
            border-collapse: collapse;
        }
        td, th {
            padding: 2px 5px;
            vertical-align: top;
        }
        td {
            text-align: right;
        }
        thead tr:last-child th {
            border-bottom: 1px solid #000;
        }

        </style>
        <link href="${resourceUrl}/images/favicon.ico" rel="shortcut icon" type="image/x-icon" />
    </head>
    <body>

    <header>
        <h1>[#if cubeLabel??]${cubeLabel.getValue(lang)}[#else]n/a[/#if]</h1>
    </header>

    <div class="pivot-content">


 [#if filters?size > 0 && (!filters[0].dimension.isMeasure() || multipleMeasuresShown)] 
    <h4>${message("cube.filter.selected")}</h4>
    [/#if]
    <dl>
        [#list dimensions as d]
             [#list filters as f]
                    [#if f.dimension.isMeasure() && !multipleMeasuresShown]
                    [#elseif f.dimension.id == d.id]
                        <dt>[@label d /]</dt>
                        <dd><span class="label label-default" dim-ref="${d.id}">[@label f /] <span class="fas fa-minus-circle"></span></span></dd>
                        [#break /]
                    [/#if]
            [/#list]
        [/#list]
    </dl>
[#macro table startColumn endColumn]
    <table class="cube table table-hover table-condensed table-bordered">
    <thead>
    [#list pivot.columns as ch]
       <tr>
            [#if ch_index == 0]
            <td colspan="${pivot.rows?size}" rowspan="${pivot.columns?size}" style="text-align:left; font-weight: bold;">
                [#if !multipleMeasuresShown]
                      [#list filters as f]
                        [#if f.dimension.isMeasure()]
                            [@label f /]
                        [/#if]
                      [/#list]
                [/#if]
            </td>
            [/#if]

            [#assign span = 0 /]
            [#list startColumn..<endColumn as c]
                [#if span == 0]
                    [#assign span = tableHelper.getColumnSpanAt(ch_index, c) /]
                    [#assign hn = pivot.getColumnAt(ch_index, c) /]
                    <th colspan="${span}">
                        [@label hn /] [#if sc?? && hn.code??](${hn.code!})[/#if]
                    </th>
                [/#if]
                [#assign span = span - 1 /]
            [/#list]
        </tr>
    [/#list]
    </thead>
    <tbody>
    <tr>
        [#assign rowNum = -1 /]
        [#list pivot.iterator() as cell]
            [#assign columnNumber = cell.columnNumber]
            [#if columnNumber > startColumn - 1 && columnNumber < endColumn]
              [#if columnNumber == startColumn]
                    [#assign rowNum = rowNum + 1 /]
                    [#if rowNum > 0]
    </tr>
    <tr>
                    [/#if]
                    [#list pivot.rows as r]
                        [#if rowspan.next(r_index)]
                            [#assign rh = pivot.getRowAt(r_index, rowNum)]
                            [#assign span = rowspan.assign(r_index, tableHelper.getRowSpanAt(r_index, rowNum)) /]
                <th[#if span > 1] rowspan="${span}"[/#if]>[@label rh /] [#if sc?? && rh.code??](${rh.code!})[/#if]</th>
                        [/#if]
                    [/#list]
                [/#if]
                <td>${value(cell.i18nValue!)}</td>
            [/#if]
        [/#list]
    </tr>
    </tbody>
    </table>
[/#macro]


    [#-- Split wide page into multiple narrower pages --]
    [#list tableBounds as bounds]
        [@table bounds[0] bounds[1] /]
    [/#list]


    </div>


    </body>
</html>
