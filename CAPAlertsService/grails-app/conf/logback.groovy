import grails.util.BuildSettings
import grails.util.Environment

// See http://logback.qos.ch/manual/groovy.html for details on configuration
appender('STDOUT', ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%level %logger - %msg%n"
    }
}

root(INFO, ['STDOUT'])
logger 'grails.app.controllers', DEBUG
// logger 'grails.app.controllers.uk', DEBUG
// logger 'grails.plugin.springsecurity', WARN
// logger 'org.springframework.security', WARN
// logger 'org.springframework.web', WARN
// logger 'grails.artefact.Interceptor', WARN, ['STDOUT'], false

logger 'capalerts', DEBUG
logger 'grails.app.init.BootStrap', DEBUG
// logger 'grails.artefact.Interceptor', WARN


def targetDir = BuildSettings.TARGET_DIR
if (Environment.isDevelopmentMode() && targetDir) {
    appender("FULL_STACKTRACE", FileAppender) {
        file = "${targetDir}/stacktrace.log"
        append = true
        encoder(PatternLayoutEncoder) {
            pattern = "%level %logger - %msg%n"
        }
    }
    logger("StackTrace", ERROR, ['FULL_STACKTRACE'], false)
}
