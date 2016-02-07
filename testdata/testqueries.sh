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
                    "areas.alertShape": {
                        "shape": {
                            "type": "circle",
                            "coordinates" : [-93,50],
                            "radius" : "1000m"
                        },
                        "relation" : "intersects"
                    }
                }
            }
        }
    }
}
'

curl -XGET 'http://localhost:9200/alerts/alert/_search' -d '
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
                        },
                        "relation" : "intersects"
                    }
                }
            }
        }
    }
}
'



## GOT THIS WORKING!!!

curl -XGET 'http://localhost:9200/alerts/alert/_search' -d '
{
    "query":{
        "bool": {
            "must": {
                "match_all": {}
            },
            "filter": {
                "nested":{
                    "path":"areas",
                    "filter" : {
                        "geo_shape": {
                            "areas.alertShape": {
                                "shape": {
                                    "type": "polygon",
                                    "coordinates" : [ [ [-90,48], [-95,48], [-95,51], [-90,51], [-90,48] ] ]
                                },
                                "relation" : "intersects"
                            }
                        }
                    }
                }
            }
        }
    }
}
'

# vs

curl -XGET 'http://localhost:9200/alerts/alert/_search' -d '
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
                                    "coordinates" : [ [ [-90,48], [-95,48], [-95,51], [-90,51], [-90,48] ] ]
                                },
                                "relation" : "intersects"
                            }
                        }
                    }
        }
    }
}
'


# Global filter query - seems to work OK


curl -XGET 'http://localhost:9200/alerts/alert/_search' -d '
{
    "query":{
        "bool": {
            "must": {
                "match_all": {}
            },
            "filter": {
                "nested":{
                    "path":"areas",
                    "filter" : {
                        "geo_shape": {
                            "areas.alertShape": {
                                "shape": {
                                    "type": "polygon",
                                    "coordinates" : [ [ [ 179, 89 ], [ -179, 89 ], [ -179, -89 ], [ 179, -89 ], [ 179, 89 ] ] ]
                                },
                                "relation" : "intersects"
                            }
                        }
                    }
                }
            }
        }
    }
}
'

