package capalerts

class AlertProfile {

  String name
  String shortcode
  String shapeType
  String shapeCoordinates
  String radius
  Date dateCreated
  Date lastUpdated

  static constraints = {
    radius nullable:true, blank:false
    lastUpdated nullable:true, blank:false
  }
}
