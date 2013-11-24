package tr.byildiz.lucenengs;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AppTest extends TestCase {
  public AppTest(String testName) {
    super(testName);
  }

  public static Test suite() {
    return new TestSuite(AppTest.class);
  }

  public void testHash() {
    assertEquals(27, Utils.compress("AATGC"));
    assertEquals(111, Utils.compress("AATGCC"));
    assertEquals(444, Utils.compress("AATGCCA"));
  }
}
