package tr.byildiz.lucenengs;

import org.apache.log4j.Logger;

public class Utils {

  public static Logger log = Logger.getLogger("MainLogger");

  public static int compress(String term) {
    if (term == null || term.length() == 0)
      return 0;
    int ret = 0;
    for (int i = 0; i < term.length(); i++) {
      int val = 0;
      switch (term.charAt(i)) {
        case 'A':
        case 'a':
          val = 0;
          break;
        case 'T':
        case 't':
          val = 1;
          break;
        case 'G':
        case 'g':
          val = 2;
          break;
        case 'C':
        case 'c':
          val = 3;
          break;
      }
      ret <<= 2;
      ret += val;
    }
    return ret;
  }

  // length unknown
  public static String decompress() {
    return null;
  }

  /**
   * inserts n - 1 "x" chars at the beginning and end of the given query k-mer
   * 
   * @param queryKmer
   * @param n
   * @return extended query k-mer
   */
  public static String extendKmer(String queryKmer, int n) {
    StringBuilder buffer = new StringBuilder(queryKmer);
    for (int i = 1; i < n; i++) {
      buffer.insert(0, "x");
      buffer.append("x");
    }
    return buffer.toString();
  }

  /**
   * inserts n - 1 "x" chars at the beginning of the given query k-mer
   * 
   * @param queryKmer
   * @param n
   * @return extended query k-mer
   */
  public static String extendBeginningOfKmer(String queryKmer, int n) {
    StringBuilder buffer = new StringBuilder(queryKmer);
    for (int i = 1; i < n; i++) {
      buffer.insert(0, "x");
    }
    return buffer.toString();
  }
}
