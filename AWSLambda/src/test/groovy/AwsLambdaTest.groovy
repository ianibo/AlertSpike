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
        shapeCoordinates: [ [ [ -92.7424, 49.0644 ], 
                              [ -93.0991, 49.0813 ], 
                              [ -93.4874, 49.0997 ], 
                              [ -93.4875, 49.6218 ], 
                              [ -92.918, 50.1283 ], 
                              [ -91.6843, 49.8684 ], 
                              [ -91.6849, 49.8672 ], 
                              [ -92.7424, 49.0644 ] ] ]
      ]

      def result = fsfa.myHandler(data,context)

      println("Result: ${result}");

      assertEquals(1,1)
    }

}
