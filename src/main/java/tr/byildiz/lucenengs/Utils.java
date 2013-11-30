package tr.byildiz.lucenengs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Properties;

import org.apache.log4j.Logger;

public class Utils {

  public static Properties prop = new Properties();

  // static {
  // try {
  // prop.load(new FileInputStream("config.properties"));
  // } catch (Exception e) {
  // e.printStackTrace();
  // }
  // }

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

  public static synchronized String getBases(int offset, int length) {
    if (SearchChromosome.bases == null)
      Utils.readBases();

    return SearchChromosome.bases.substring(offset, offset + length);
  }

  /**
   * return n length of n-gram padded end of given n-gram with "x"
   * 
   * @param ngram
   * @param n
   * @return padded n-gram
   */
  public static String padEndOfNgram(String ngram, int n) {
    if (n <= ngram.length())
      return ngram;
    StringBuilder buffer = new StringBuilder(ngram);
    for (int i = ngram.length(); i < n; i++) {
      buffer.append("x");
    }
    return buffer.toString();
  }

  /**
   * finds edit distance (aka. Levenstein distance) between s1 and s2
   * 
   * @param s1
   * @param s2
   * @return edit distance
   */
  public static int editDistance(String s1, String s2) {
    s1 = s1.toLowerCase();
    s2 = s2.toLowerCase();

    int l1 = s1.length();
    int l2 = s2.length();

    if (l1 == 0)
      return l2;

    if (l2 == 0)
      return l1;

    int[][] d = new int[l1 + 1][l2 + 1];

    for (int i = 0; i <= l1; i++) {
      d[i][0] = i;
    }

    for (int j = 0; j <= l2; j++) {
      d[0][j] = j;
    }

    for (int j = 1; j <= l2; j++) {
      for (int i = 1; i <= l1; i++) {
        if (s1.charAt(i - 1) == s2.charAt(j - 1))
          d[i][j] = d[i - 1][j - 1];
        else
          d[i][j] = Math.min(d[i - 1][j] + 1,
              Math.min(d[i][j - 1] + 1, d[i - 1][j - 1] + 1));
      }
    }

    return d[l1][l2];
  }

  public static int countLines(String path) throws Exception {
    File file = new File(path);
    LineNumberReader lineCounter = new LineNumberReader(new FileReader(file));
    lineCounter.skip(Long.MAX_VALUE);
    int count = lineCounter.getLineNumber();
    lineCounter.close();
    return count;
  }

  public static void readBases() {
    if (SearchChromosome.bases != null)
      return;

    try {
      String basesPath = SearchChromosome.indexPath + ".txt";
      File basesFile = new File(basesPath);
      BufferedReader basesReader = new BufferedReader(new FileReader(basesFile));
      SearchChromosome.bases = basesReader.readLine();
      basesReader.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static String readFasta(String filePath) throws IOException {
    File file = new File(filePath);
    BufferedReader reader = new BufferedReader(new FileReader(file));
    StringBuilder buffer = new StringBuilder();
    while (true) {
      String line = reader.readLine();
      if (line == null) {
        break;
      }
      line = line.trim();
      // for fasta files
      if (line.startsWith(">") || line.contains("N"))
        continue;
      buffer.append(line);
    }
    reader.close();

    return buffer.toString();
  }
}
