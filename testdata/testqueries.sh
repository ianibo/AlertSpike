# See if we can find the above record by searching for a square that intersects it
curl -XGET 'http://localhost:9200/alerts/alert/_search' -d '
{
    "query":{
        "bool": {
            "must": {
                "match_all": {}
            },
            "filter": {
                "geo_shape": {
                    "alertShape": {
                        "shape": {
                            "type": "polygon",
                            "coordinates" : [ [-109.5297,40.4554], 
                                              [-109.5298,40.4556], 
                                              [-109.5299,40.4556], 
                                              [-109.5299,40.4554], 
                                              [-109.5297,40.4554] ]
                        }
                    }
                }
            }
        }
    }
}
'



# Lets see if we can find by intersecting circle
curl -XGET 'http://localhost:9200/alerts/alert/_search' -d '
{
    "query":{
        "bool": {
            "must": {
                "match_all": {}
            },
            "filter": {
                "geo_shape": {
                    "alertShape": {
                        "shape": {
                            "type": "circle",
                            "coordinates" : [-109.5288,40.4555],
                            "radius" : "1000m"

                        }
                    }
                }
            }
        }
    }
}
'
