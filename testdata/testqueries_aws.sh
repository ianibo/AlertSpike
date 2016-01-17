# See if we can find the above record by searching for a square that intersects it
curl -XGET 'http://52.31.77.192/es/alerts/alert/_search' -d '
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
                            "coordinates" : [ [-109.5297,40.4554], [-109.5298,40.4556], [-109.5299,40.4556], [-109.5299,40.4554], [-109.5297,40.4554] ]
                        }
                    }
                }
            }
        },
        "inner_hits":{
        }
    }
}
'



# Lets see if we can find by intersecting circle
curl -XGET 'http://52.31.77.192/es/alerts/alert/_search' -d '
{
    "query":{
        "bool": {
            "must": {
                "match_all": {}
            },
            "filter": {
                "geo_shape": {
                    "areas.alertShape": {
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


curl -XGET 'http://52.31.77.192/es/alerts/alert/_search' -d '
{
    "query":{
        "bool": {
            "must": {
                "match_all": {}
            },
            "filter": {
                "geo_shape": {
                    "areas.alertShape": {
                        "shape": {
                            "type": "polygon",
                            "coordinates" : [ [ [-108,50], [-110,50.00], [-110,51], [-108,51], [-108,50.00] ] ] 
                        }
                    }
                }
            }
        },
        "inner_hits":{
        }
    }
}
'

