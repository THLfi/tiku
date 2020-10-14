[#ftl]
<table class="cube table table-hover table-condensed table-bordered">
    [#list pivot.columns as ch]
    <tr>
        <td colspan="${pivot.rows?size + 1}"></td>

        [#assign span = 0 /]
        [#list 0..<pivot.columnCount as c]
            [#if span == 0]
                [#assign span = tableHelper.getColumnSpanAt(ch_index, c) /]
                <th class="column-target" colspan="${span}">[@label pivot.getColumnAt(ch_index, c) /]</th>
            [/#if]
            [#assign span = span - 1 /]
        [/#list]
    </tr>
    [/#list]
    <tr>
        <td colspan="${pivot.rows?size + 1}"></td>
        <th class="column-target" colspan="${pivot.columnCount}">${message("cube.hint.columns")}</th>
    </tr>
    [#if pivot.rowCount == 0]
        <tr>
           <th class="row-target">${message("cube.hint.rows")}</th>
           <td colspan="${pivot.columnCount}"></td>
        </tr>
    [#else]

        [#list 0..<pivot.rowCount as i]
        <tr>
            [#list pivot.rows as r]
                [#if rowspan.next(r_index)]
                    [#assign span = rowspan.assign(r_index, tableHelper.getRowSpanAt(r_index, i)) /]
                    <th class="row-target" rowspan="${span}">[@label pivot.getRowAt(r_index, i) /]</th>
                [/#if]
            [/#list]
            [#if i == 0]
            <th class="row-target" rowspan="${pivot.rowCount}">${message("cube.hint.rows")}</th>
            [/#if]
            <td colspan="${pivot.columnCount}"></td>
        </tr>
       [/#list]
   [/#if]
</table>
