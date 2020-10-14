[#ftl][#setting locale="fi"]
[#function value val]
    [#if val?matches("\\d+,\\d+")][#return val?replace(",",".")?number?string("0.00")/][/#if]
    [#if val?matches("\\d+")][#return val?number?string("0")/][/#if]
    [#return val /]
[/#function]
[#if pivot.columns?size > 0 || pivot.rows?size > 0]
[#if pivot.columns?last?size > 0 || size.filteredRows?last?size > 0]
[#if jsonp]thl.pivot.fromJsonStat([/#if]
{
    "dataset" : {
        "label" : "THL pivot cube",
        "dimension" : {
            "id" : [
                [#list pivot.rows as ch][#if ch.nodes?size>0][#if ch_index > 0],[/#if]"${ch.dimension.id}"[/#if][/#list]
                [#list pivot.columns as ch][#if ch.nodes?size>0][#if pivot.rows?size > 0 || ch_index > 0],[/#if]"${ch.dimension.id}"[/#if][/#list]
                [#if showCi || showSampleSize], "tiku_vtype"[/#if]
            ],
            "size": [
                [#list pivot.rows as ch][#if ch_index > 0],[/#if]${ch.nodes?size}[/#list]
                [#list pivot.columns as ch][#if pivot.rows?size > 0 ||ch_index > 0],[/#if]${ch.nodes?size}[/#list]
                [#if showCi || showSampleSize], 4[/#if]

            ]
            [#list pivot.columns as ch]
            [#if ch?size > 0]
            ,"${ch.dimension.id}" : {
                "category" : {
                "index" : {
                    [#list ch.nodes as c]
                        [#if c_index > 0],[/#if] "[#if surrogate]${c.surrogateId}[#else]${c.id}[/#if]" : ${c_index}
                    [/#list]

                }}
            }
            [/#if]
            [/#list]
             [#list pivot.rows as ch]
             [#if ch?size > 0]
            ,"${ch.dimension.id}" : {
                "category" : {
                "index" : {
                    [#list ch.nodes as c]
                        [#if c_index > 0],[/#if] "[#if surrogate]${c.surrogateId}[#else]${c.id}[/#if]" : ${c_index}
                    [/#list]
                }}
            }
            [/#if]
            [/#list]
            [#if showCi || showSampleSize]
              ,"tiku_vtype": {
                "category": {
                  "index": {
                    "v": 0,
                    "ci_lower": 1,
                    "ci_upper": 2,
                    "n": 3
                  }
                }
              }
            [/#if]
        },
        "value" : {[#assign hasComma = false/]
            [#list pivot.iterator() as cell][#if cell.value??]
              [#if hasComma],[#else][#assign hasComma=true/][/#if]
              [#if showCi || showSampleSize]
                "${cell.position * 4}": ${value(cell.value!)?replace(",",".")?replace("..","\"..\"")}
                [#if cell.confidenceLowerLimit??],"${cell.position * 4 + 1}": ${cell.confidenceLowerLimit}[/#if]
                [#if cell.confidenceUpperLimit??],"${cell.position * 4 + 2}": ${cell.confidenceUpperLimit}[/#if]
                [#if cell.sampleSize??],"${cell.position * 4 + 3}": ${cell.sampleSize}[/#if]

              [#else]
                "${cell.position}": ${value(cell.value!)?replace(",",".")?replace("..","\"..\"")}
              [/#if]
            [/#if][/#list]
        }
    }
}
[#if jsonp]);[/#if]
[/#if][/#if]
