package tr.byildiz.lucenengs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.LineNumberReader;
import java.util.Date;
import java.util.Scanner;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.spans.SpanPositionRangeQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.store.FSDirectory;

public class SearchChromosome {

  public static final boolean DEBUG = DebugConfig.DEBUG;

  public static int n = DefaultConfig.N;

  public static int e = DefaultConfig.E;

  public static boolean withED = DefaultConfig.WITHED;

  public static boolean withHash = DefaultConfig.WITHHASH;

  public static int repeat = DefaultConfig.REPEAT;

  public static int kmerLength = DefaultConfig.KMERLENGTH;

  public static boolean more = DefaultConfig.MORE;

  public static String homePath = DefaultConfig.HOMEPATH;

  public static String queryKmer = null;

  public static String indexPath = DefaultConfig.INDEXPATH;

  public static String queryPath = DefaultConfig.QUERYPATH;

  public static String resultsPath = DefaultConfig.RESULTSPATH;

  public static String searchMethod = DefaultConfig.SEARCHMETHOD;

  public static String field = DefaultConfig.FIELD;

  public static String bases = null;

  public static String usage = "Usage: SearchChromosome -method ([AnF],AnFP,AnFPE,Tocc) -index indexPath -query queryPath -results resultsPath -kmer kmerLength -repeat [1] -more -hash -n [8] -e [4]";

  public static void main(String[] args) throws Exception {
    // parse system arguments
    for (int i = 0; i < args.length; i++) {
      if ("-index".equals(args[i])) {
        indexPath = homePath + args[i + 1];
        i++;
      } else if ("-e".equals(args[i])) {
        e = Integer.parseInt(args[i + 1]);
        i++;
      } else if ("-n".equals(args[i])) {
        n = Integer.parseInt(args[i + 1]);
        i++;
      } else if ("-query".equals(args[i])) {
        queryPath = homePath + args[i + 1];
        i++;
      } else if ("-method".equals(args[i])) {
        searchMethod = args[i + 1];
        resultsPath = homePath + searchMethod + "_results.txt";
        i++;
      } else if ("-repeat".equals(args[i])) {
        repeat = Integer.parseInt(args[i + 1]);
        i++;
      } else if ("-more".equals(args[i])) {
        more = true;
      } else if ("-hash".equals(args[i])) {
        withHash = true;
      } else if ("-ed".equals(args[i])) {
        withED = true;
      } else if ("-results".equals(args[i])) {
        resultsPath = homePath + args[i + 1];
        i++;
      } else if ("-kmer".equals(args[i])) {
        kmerLength = Integer.parseInt(args[i + 1]);
        i++;
      }
    }

    // in debug mode set all command line parameters
    if (DEBUG) {
      n = DebugConfig.N;
      e = DebugConfig.E;
      withED = DebugConfig.WITHED;
      withHash = DebugConfig.WITHHASH;
      more = DebugConfig.MORE;
      repeat = DebugConfig.REPEAT;
      kmerLength = DebugConfig.KMERLENGTH;
      searchMethod = DebugConfig.SEARCHMETHOD;
      indexPath = DebugConfig.INDEXPATH;
      queryPath = DebugConfig.QUERYPATH;
      resultsPath = DebugConfig.RESULTSPATH;
    }

    if (indexPath == null || queryPath == null || resultsPath == null) {
      System.out.println(usage);
      System.exit(0);
    }

    // add k-mer length and n to indexPath for prevent confusion
    indexPath += "_" + kmerLength + "_" + n;
    if (withED)
      indexPath += "_withED";
    if (withHash)
      indexPath += "_withHash";

    Date globalStart = new Date();

    System.out.println("Searcing started...\n");
    System.out.println("Search Method: " + searchMethod + "\n");

    long allTime = 0;
    long allSearchTime = 0;
    long allEdTime = 0;
    int searchCount = 0;
    int queryCount = countLines(queryPath);

    // read all bases to memory
    readBases();

    File indexDir = new File(indexPath);
    IndexReader reader = IndexReader.open(FSDirectory.open(indexDir));
    IndexSearcher searcher = new IndexSearcher(reader);

    File queryFile = new File(queryPath);
    if (!queryFile.canRead()) {
      System.out.println("Query file not found or not readable: " + queryPath);
    }

    File resultsFile = new File(resultsPath);
    BufferedWriter writer = new BufferedWriter(new FileWriter(resultsFile));

    Scanner scanner = new Scanner(queryFile);
    while (scanner.hasNextLine()) {
      queryKmer = scanner.nextLine();
      if (queryKmer.equals(""))
        continue;

      writer.append("TESTS FOR QUERY: " + queryKmer + "\n");
      writer.newLine();

      long totalEdTime = 0;
      long edTime = 0;
      long searchTime = 0;
      long totalSearchTime = 0;
      long totalTime = 0;
      int numHits = 0;
      int numFounds = 0;
      ScoreDoc[] hits = null;
      boolean[] founds = null;
      int[] distances = null;
      for (int r = 0; r < repeat; r++) {
        // all bases should be lower case because lucene index in lower case
        String qmer = queryKmer.toLowerCase();

        Query query = null;
        if (searchMethod.equals("AnF"))
          query = prepareAnFQuery(field, qmer, n, e);
        else if (searchMethod.equals("AnFP") || searchMethod.equals("AnFPE"))
          query = prepareAnFPQuery(field, qmer, n, e);
        else if (searchMethod.equals("Tocc"))
          query = prepareToccQuery(field, qmer, n, e);

        Date start = new Date();

        TopDocs results = searcher.search(query, Integer.MAX_VALUE);

        // search time
        Date end = new Date();
        searchTime = end.getTime() - start.getTime();
        // set start again for edit distance time
        start = new Date();

        hits = results.scoreDocs;
        numHits = results.totalHits;

        // calculate edit distances of found k-mers
        founds = new boolean[numHits];
        distances = new int[numHits];
        for (int i = 0; i < numHits; i++) {
          int docID = hits[i].doc;
          String kmer = getBases(docID, kmerLength);
          distances[i] = editDistance(queryKmer, kmer);
          if (distances[i] <= e) {
            founds[i] = editDistance(queryKmer, kmer) <= e;
            // just for first repeat
            if (r == 0)
              numFounds++;
          }
        }

        // edit distance time
        end = new Date();
        edTime = end.getTime() - start.getTime();

        totalSearchTime += searchTime;
        totalEdTime += edTime;
        totalTime += searchTime + edTime;

        writer.append((r + 1) + ". repeat time: " + edTime + "\n");
      } // repeat loop
      writer.newLine();

      // write out found k-mers
      if (more) {
        for (int i = 0; i < numHits; i++) {
          int docID = hits[i].doc;
          // Document doc = searcher.doc(docID);
          // String kmer = doc.get("kmer");
          String kmer = getBases(docID, kmerLength);
          if (founds[i]) {
            writer.append((i + 1) + ". found: " + kmer + " " + distances[i]);
            writer.newLine();
          } else {
            writer.append((i + 1) + ". false: " + kmer + " " + distances[i]);
            writer.newLine();
          }
        }
        writer.newLine();
      }
      allSearchTime += totalSearchTime;
      allEdTime += totalEdTime;
      allTime += totalTime;
      // count each searching
      searchCount += repeat;

      double meanSearhTime = (double) totalSearchTime / repeat;
      double meanEdTime = (double) totalEdTime / repeat;
      double meanTime = (double) totalTime / repeat;

      // numHits: after filter k-mer
      // numFounds: after real edit distance comparison
      writer.append(numHits + " total collected k-mers" + "\n");
      writer.append(numFounds + " total found k-mers" + "\n");
      writer.newLine();

      writer.append("SEARCH TIME: " + totalSearchTime + "\n");
      writer.append("ED TIME: " + totalEdTime + "\n");
      writer.append("TOTAL TIME: " + totalTime + "\n");
      writer.append("SEARCH MEAN TIME: " + meanSearhTime + "\n");
      writer.append("ED MEAN TIME: " + meanEdTime + "\n");
      writer.append("TOTAL MEAN TIME: " + meanTime + "\n");
      writer.newLine();

      // write status for each query
      Date globalEnd = new Date();
      double elapsed = (double) (globalEnd.getTime() - globalStart.getTime()) / 1000;
      double remaining = elapsed * (queryCount - searchCount) / searchCount;
      System.out.format("Search count:\t%d\n", searchCount);
      System.out.format("Elapsed time:\t%.1f seconds\n", elapsed);
      System.out.format("Remaining time:\t%.1f seconds\n", remaining);
      System.out.println();

      writer.append("\n\n");
    } // query loop

    double allSearchMean = (double) allSearchTime / searchCount;
    double allEdMean = (double) allEdTime / searchCount;
    double allMean = (double) allTime / searchCount;

    writer.append("TOTAL SEARCH TIME: " + allSearchTime + "\n");
    writer.append("TOTAL ED TIME: " + allEdTime + "\n");
    writer.append("GLOBAL TOTAL TIME: " + allTime + "\n");
    writer.append("TOTAL MEAN SEARCH TIME: " + allSearchMean + "\n");
    writer.append("TOTAL MEAN ED TIME: " + allEdMean + "\n");
    writer.append("GLOBAL MEAN TIME: " + allMean + "\n");
    writer.append("\n\n\n");

    writer.flush();
    writer.close();
    scanner.close();
    searcher.close();
    reader.close();
  }

  public static Query prepareToccQuery(String field, String qmer, int n, int e) {
    int length = qmer.length();
    int T = length + n - 1 - e * n;
    // if number of should match zero or negative than return a true query
    if (T <= 0) {
      MatchAllDocsQuery trueQuery = new MatchAllDocsQuery();
      return trueQuery;
    }

    BooleanQuery query = new BooleanQuery();
    query.setMinimumNumberShouldMatch(T);

    // use beginning and end extended of query k-mer with n - 1 times "x"
    qmer = IndexChromosome.extendKmer(qmer, n);

    while (true) {
      if (qmer.length() < n) {
        break;
      }
      String ngram = qmer.substring(0, n);
      if (withHash)
        ngram = ngram.hashCode() + "";
      // create a new term query and increase the number of should match
      Term term = new Term(field, ngram);
      TermQuery termQuery = new TermQuery(term);
      query.add(termQuery, BooleanClause.Occur.SHOULD);

      // trim one char at the beginning
      qmer = qmer.substring(1);
    }
    return query;
  }

  public static Query prepareAnFQuery(String field, String qmer, int n, int e) {
    BooleanQuery query = new BooleanQuery();

    // use beginning extended of query k-mer with n - 1 times "x"
    qmer = IndexChromosome.extendBeginningOfKmer(qmer, n);

    for (int i = 0; i < n; i++) {
      String copy = qmer.substring(i);
      BooleanQuery filter = new BooleanQuery();
      int filterThreshold = -e;
      while (true) {
        String ngram = null;
        if (copy.length() < n) {
          ngram = padEndOfNgram(copy, n);
        } else {
          ngram = copy.substring(0, n);
        }
        if (withHash)
          ngram = ngram.hashCode() + "";
        // create a new term query and increase the number of should match
        Term term = new Term(field, ngram);
        TermQuery termQuery = new TermQuery(term);
        filter.add(termQuery, BooleanClause.Occur.SHOULD);
        filterThreshold++;

        if (copy.length() <= n)
          break;

        // trim the used part of copy query k-mer
        copy = copy.substring(n);
      }
      // if number of should match is less than or equal to zero than the filter
      // is always true, so add query a MatchAllDocsQuery
      if (filterThreshold <= 0) {
        MatchAllDocsQuery trueQuery = new MatchAllDocsQuery();
        query.add(trueQuery, BooleanClause.Occur.MUST);
      } else {
        filter.setMinimumNumberShouldMatch(filterThreshold);
        query.add(filter, BooleanClause.Occur.MUST);
      }
    }
    return query;
  }

  public static Query prepareAnFPQuery(String field, String qmer, int n, int e) {
    BooleanQuery query = new BooleanQuery();

    // use beginning extended of query k-mer with n - 1 times "x"
    qmer = IndexChromosome.extendBeginningOfKmer(qmer, n);

    for (int i = 0; i < n; i++) {
      String copy = qmer.substring(i);
      BooleanQuery filter = new BooleanQuery();
      int shouldMatchNumber = -e;
      int count = 0;
      while (true) {
        String ngram = null;
        if (copy.length() < n) {
          ngram = padEndOfNgram(copy, n);
        } else {
          ngram = copy.substring(0, n);
        }
        // calculate the position of n-gram in query
        int position = i + (count * n);

        if (withED) {
          // create term with position of n-gram
          ngram = ngram + position;
          if (withHash)
            ngram = ngram.hashCode() + "";
          Term term = new Term(field, ngram);
          TermQuery termQuery = new TermQuery(term);
          filter.add(termQuery, BooleanClause.Occur.SHOULD);
        } else {
          if (withHash)
            ngram = ngram.hashCode() + "";
          Term term = new Term(field, ngram);

          // create a new span range query
          int start = position - e;
          int end = position + e;
          SpanTermQuery termQuery = new SpanTermQuery(term);
          SpanPositionRangeQuery rangeQuery = new SpanPositionRangeQuery(
              termQuery, start, end);
          filter.add(rangeQuery, BooleanClause.Occur.SHOULD);
        }

        // after add a new should match increase the number of should match
        shouldMatchNumber++;

        if (copy.length() <= n)
          break;

        // trim the used part of copy query k-mer
        copy = copy.substring(n);
        // increase loop count
        count++;
      }
      // if number of should match is less than or equal to zero than the filter
      // is always true, so add query a MatchAllDocsQuery
      if (shouldMatchNumber <= 0) {
        MatchAllDocsQuery trueQuery = new MatchAllDocsQuery();
        query.add(trueQuery, BooleanClause.Occur.MUST);
      } else {
        filter.setMinimumNumberShouldMatch(shouldMatchNumber);
        query.add(filter, BooleanClause.Occur.MUST);
      }
    }
    return query;
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

  private static void readBases() {
    if (bases != null)
      return;

    try {
      String basesPath = indexPath + ".txt";
      File basesFile = new File(basesPath);
      BufferedReader basesReader = new BufferedReader(new FileReader(basesFile));
      bases = basesReader.readLine();
      basesReader.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static String getBases(int offset, int length) {
    if (bases == null)
      readBases();

    return bases.substring(offset, offset + length);
  }
}
