curl -XGET 'http://localhost:9200/alertssubscriptions/_search' -d '
{
    "query":{
        "bool": {
            "must": {
                "match_all": {}
            },
            "filter": {
                "geo_shape": {
                    "subshape": {
                        "shape": {
                            "type": "polygon",
                            "coordinates" : [ [ [-109.5297,40.4554], [-109.5298,40.4556], [-109.5299,40.4556], [-109.5299,40.4554], [-109.5297,40.4554] ] ]
                        },
                        "relation" : "INTERSECTS"
                    }
                }
            }
        }
    }
}
'

