package tr.byildiz.lucenengs;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.spans.SpanPositionRangeQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.store.FSDirectory;

import tr.byildiz.lucenengs.SearchWorker.Results;

public class SearchChromosome {

  public static final boolean DEBUG = DebugConfig.DEBUG;

  public static int n = DefaultConfig.N;

  public static int e = DefaultConfig.E;

  public static boolean withED = DefaultConfig.WITHED;

  public static boolean withHash = DefaultConfig.WITHHASH;

  public static int poolSize = DefaultConfig.POLLSIZE;

  public static int repeat = DefaultConfig.REPEAT;

  public static int kmerLength = DefaultConfig.KMERLENGTH;

  public static boolean verbose = DefaultConfig.VERBOSE;

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
      } else if ("-pool".equals(args[i])) {
        poolSize = Integer.parseInt(args[i + 1]);
        i++;
      } else if ("-verbose".equals(args[i])) {
        verbose = true;
      }
    }

    // in debug mode set all command line parameters
    if (DEBUG) {
      n = DebugConfig.N;
      e = DebugConfig.E;
      poolSize = DebugConfig.POLLSIZE;
      withED = DebugConfig.WITHED;
      withHash = DebugConfig.WITHHASH;
      verbose = DebugConfig.VERBOSE;
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

    System.out.println("Searcing started...\n");
    System.out.println("Search Method: " + searchMethod + "\n");

    // read all bases to memory
    Utils.readBases();

    File queryFile = new File(queryPath);
    if (!queryFile.canRead()) {
      System.out.println("Query file not found or not readable: " + queryPath);
    }

    // get queries
    Scanner scanner = new Scanner(queryFile);
    ArrayList<Query> queryList = new ArrayList<>();
    while (scanner.hasNextLine()) {
      String line = scanner.nextLine();
      line = line.trim();
      // for fasta files
      if ("".equals(line) || line.startsWith(">") || line.contains("N"))
        continue;

      line = line.toLowerCase();

      Query query = null;
      if (searchMethod.equals("AnF"))
        query = prepareAnFQuery(field, line, n, e);
      else if (searchMethod.equals("AnFP") || searchMethod.equals("AnFPE"))
        query = prepareAnFPQuery(field, line, n, e);
      else if (searchMethod.equals("Tocc"))
        query = prepareToccQuery(field, line, n, e);

      queryList.add(query);
    }
    scanner.close();
    Query[] queries = queryList.toArray(new Query[0]);

    File indexDir = new File(indexPath);
    for (int i = 0; i < poolSize; i++) {
      File copyDir = new File(indexPath + "_copy" + i);
      if (!copyDir.exists())
        FileUtils.copyDirectory(indexDir, copyDir);
    }

    Date globalStart = new Date();

    // create thread pool, assign a searcher to each threads
    IndexReader[] readers = new IndexReader[poolSize];
    IndexSearcher[] searchers = new IndexSearcher[poolSize];
    ExecutorService threadPool = Executors.newFixedThreadPool(poolSize);
    Set<Future<Results>> searchResults = new HashSet<>();
    for (int i = 0; i < poolSize; i++) {
      File copyDir = new File(indexPath + "_copy" + i);
      readers[i] = DirectoryReader.open(FSDirectory.open(copyDir));
      searchers[i] = new IndexSearcher(readers[i]);
      SearchWorker worker = new SearchWorker(i, searchers[i], queries,
          SearchChromosome.poolSize, kmerLength, verbose);
      searchResults.add(threadPool.submit(worker));
    }
    threadPool.shutdown();
    while (!threadPool.isTerminated()) {
    }

    // write status for each query
    Date globalEnd = new Date();
    long globalTime = globalEnd.getTime() - globalStart.getTime();

    // close readers
    for (int i = 0; i < poolSize; i++) {
      readers[i].close();
    }

    int totalHits = 0;
    long totalTime = 0;
    for (Future<Results> f : searchResults) {
      Results results = f.get();
      int hits = results.totalHits;
      long time = results.workTime;
      totalHits += hits;
      totalTime += time;
      System.out.println("Search Worker #" + results.threadId + " found "
          + hits + " kmers in " + time);
    }

    System.out.println();
    System.out.println("Total Hits: " + totalHits);
    System.out.println("Total Work Time: " + totalTime);
    System.out.println();
    System.out.println("Overall Time: " + globalTime);
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
    // qmer = Utils.extendKmer(qmer, n);

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
    // qmer = Utils.extendBeginningOfKmer(qmer, n);

    for (int i = 0; i < n; i++) {
      String copy = qmer.substring(i);
      BooleanQuery filter = new BooleanQuery();
      int filterThreshold = -e;
      while (true) {
        String ngram = null;
        if (copy.length() < n) {
          // ngram = padEndOfNgram(copy, n);
          break;
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
    // qmer = Utils.extendBeginningOfKmer(qmer, n);

    for (int i = 0; i < n; i++) {
      String copy = qmer.substring(i);
      BooleanQuery filter = new BooleanQuery();
      int shouldMatchNumber = -e;
      int count = 0;
      while (true) {
        String ngram = null;
        if (copy.length() < n) {
          ngram = Utils.padEndOfNgram(copy, n);
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
}
