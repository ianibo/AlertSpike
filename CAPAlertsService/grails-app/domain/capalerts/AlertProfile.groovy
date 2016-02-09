package capalerts

class AlertProfile {

  String name
  String shortcode
  String shapeType
  String shapeCoordinates
  String radius

  static constraints = {
    radius nullable:true, blank:false
  }
}
