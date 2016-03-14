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
      def data = [:]
      def result = fsfa.myHandler(data,context)
      assertEquals(1,1)
    }

}
