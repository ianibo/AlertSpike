@Grapes([
    // @GrabResolver(name='central', root='http://central.maven.org/maven2/'),
    @GrabResolver(name='mvnRepository', root='http://central.maven.org/maven2/'),
    @Grab(group='org.slf4j', module='slf4j-api', version='1.7.6'),
    @Grab(group='org.slf4j', module='jcl-over-slf4j', version='1.7.6'),
    @Grab(group='net.sourceforge.nekohtml', module='nekohtml', version='1.9.14'),
    @Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.5.1'),
    @Grab(group='xerces', module='xercesImpl', version='2.9.1')
])

// http://www.sheffieldairmap.org/view_map.html
// http://uk-air.defra.gov.uk/networks/site-info?site_id=SHE2

import groovyx.net.http.*
import static groovyx.net.http.ContentType.URLENC
import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*
import groovyx.net.http.*
import org.apache.http.entity.mime.*
import org.apache.http.entity.mime.content.*
import java.nio.charset.Charset
import static groovy.json.JsonOutput.*
import java.text.SimpleDateFormat



// Here we list all the fields from the cap object that we want to map into a JSON document for
// ES to index. the addField method will look at the variant and if the DEFAULT variant is the same,
// will add that field (If the value is different). Not sure this is the best way to go -- mixing
// locales in IR is always a tricky thing - will probably need to be reviewed and improved many times.
infoFields = [
    language:[element:'cap:language',    langstring:true,    json_element:'language'],
    category:[element:'cap:category',    langstring:true,    json_element:'category'],
       event:[element:'cap:event',       langstring:true,    json_element:'event'],
      source:[element:'cap:source',      langstring:true,    json_element:'source'],
       scope:[element:'cap:scope',       langstring:true,    json_element:'scope'],
    headline:[element:'cap:headline',    langstring:true,    json_element:'headline'],
 description:[element:'cap:description', langstring:true,    json_element:'description'],
 instruction:[element:'cap:instruction', langstring:true,    json_element:'instruction'],
         web:[element:'cap:web',         langstring:true,    json_element:'web']
];



println("Run as groovy -Dgroovy.grape.autoDownload=false  ./ial.groovy\nTo avoid startup lag");
doUpdate('https://alerts.internetalerts.org/feed')



def doUpdate(baseurl) {
  try {
    def feed = new XmlSlurper().parse(baseurl) 
    feed.entry.each { entry ->
      // println(entry.title)
      // println(entry.id)
      // println(entry.updated)
      processEntry(entry.title,entry.id,entry.updated,entry.link.@href.text())
    }
  }
  catch ( Exception e ) {
    println("ERROR....(${baseurl})"+e.message);
  }
}

def processEntry(title, id, timestamp, url) {

  def es_record = [:]
  es_record.areas = []
  def default_langcode = 'en'

  println("process ${url}");
  def entry = new XmlSlurper()
                    .parse(url)
                    .declareNamespace(cap: 'urn:oasis:names:tc:emergency:cap:1.2', test: 'urn:oasis:names:tc:emergency:captest:1.2')
  // println("Identifier ${entry.'cap:identifier'}")
  entry.'cap:info'.each { info ->

    // Looks like each info section is repeated with a language code.. 
    def entry_lang = info.'cap:language'.text()
    def langcode = default_langcode
    if ( entry_lang.trim().length() > 0 ) {
      langcode=entry_lang.substring(0,2);
    }

    // Cycle through the fields we want to extract, and see if each property is present
    infoFields.each { k,v ->
      def value = info."${v.element}"?.text()
      if ( value && ( value.trim().length() > 0 ) ) {
        // println("Element ${v.element} present, lang is ${entry_locale}, value is ${value}");
        addOrAppendElement(es_record, v.json_element, value, langcode, default_langcode, v.langstring)
      }
    }

    info.'cap:parameter'.each { param ->
      // println("${param.'cap:valueName'} = ${param.'cap:value'}")
    }
    info.'cap:area'.each { area_xml ->
      // println("${area.'cap:areaDesc'} -- ${area.'cap:polygon'}");
      def area = extractArea(area_xml)

      // What we *should* do here is to see if we have the area already, but with a different langstring variant,
      // and add a variant label to the area rather than duplicating the whole area. But it's a POC, so lets live with it
      es_record.areas.add(area);
    }

    println("Add ${toJson(es_record)} ");
  }
}

def addOrAppendElement(basemap, elementname, value, langcode, default_langcode, is_langstring) {
  // println("addOrAppendElement(${basemap},${elementname},${value}");
  if ( is_langstring ) {
    addString(basemap,elementname+'_'+langcode,value);
    if ( langcode == default_langcode ) {
      addString(basemap,elementname,value);
    }
  }
  else {
    addString(basemap,elementname,value);
  }
}

def addString(basemap,elementname,value) {
  if ( basemap[elementname] == null ) {
    // no value currently
    basemap[elementname] = value
  }
  else if ( basemap[elementname] instanceof List ) {
    // property is already a list, add another value
    basemap[elementname] = basemap[elementname].add(value)
  }
  else {
    // Convert scalar to list
    basemap[elementname] = [basemap[elementname], value]
  }
}

def extractArea(area_xml) {
  // Take a cap:area - cap:areaDesc label and geometry such as cap:circle and convert
  def result = [:]
  result.label = area_xml.'cap:areaDesc'.text()

  def cap_circle = area_xml.'cap:circle'.text().trim()
  if ( cap_circle.length() > 0 ) {
    def stage1 = cap_circle.split(' '); // Split on space to get radius. ES Circle defaults to meters as a unit. CAP seems to be different.
    def stage2 = stage1.split(','); // Split cap records lat,lon. ES expects X,Y so we have to flip
    result.geom=[type:'circle', coordinates:[stage2[1],  stage2[0]], radius:stage1[1]]
    result.fingerPrint = 'circle'+cap_circle
  }

  def cap_polygon = area_xml.'cap:polygon'.text().trim()
  if ( cap_polygon.length() > 0 ) {
    def stage1 = cap_polygon.split(' ');  // Split components of polygon
    StringWriter sw = new StringWriter()
    stage1.each {
      def stage2 = it.split(','); // Sply lat,lon so we can flip to X,Y
      sw.write(stage2[1]);
      sw.write(',');
      sw.write(stage2[0]);
      sw.write(' ');
    }
    result.geom=[type:'polygon', coordinates:sw.toString()]

    result.fingerPrint = 'polygon'+cap_polygon
  }

  return result
}
