class UrlMappings {

    static mappings = {

        "/profile" ( controller:'profile', action:'index' )
        "/profile/$id/$action" ( controller:'profile' )

        "/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
        }


        "/"(view:"/index")
        "500"(view:'/error')
        "404"(view:'/notFound')
    }
}
