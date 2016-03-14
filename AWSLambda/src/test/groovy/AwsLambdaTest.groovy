package org.gradle

import org.junit.Test
import alerts.*

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull
import groovy.util.Expando

class AwsLambdaTest {

    @Test public void initialTest() {

      println("Running initialTest");

      // Mock up a context
      def context = new Expando() as com.amazonaws.services.lambda.runtime.Context

      FindSubsForAlert fsfa = new FindSubsForAlert()
      def data = [
        shapeType:'polygon',
        shapeCoordinates: [ [ [-109.5297,40.4554], [-109.5298,40.4556], [-109.5299,40.4556], [-109.5299,40.4554], [-109.5297,40.4554] ] ]
      ]

      def result = fsfa.myHandler(data,context)

      println("Result: ${result}");

      assertEquals(1,1)
    }

}
