package tr.byildiz.lucenengs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class IndexChromosome {

  public static final boolean DEBUG = DebugConfig.DEBUG;

  public static int n = DefaultConfig.N;

  public static int e = DefaultConfig.E;

  public static boolean withED = DefaultConfig.WITHED;

  public static boolean withHash = DefaultConfig.WITHHASH;

  public static int kmerLength = DefaultConfig.KMERLENGTH;

  public static String homePath = DefaultConfig.HOMEPATH;

  public static String filePath = DefaultConfig.FILEPATH;

  public static String indexPath = DefaultConfig.INDEXPATH;

  public static String field = DefaultConfig.FIELD;

  public static int[] indexParts = DefaultConfig.INDEXPARTS;

  public static String usage = "Usage: IndexChromosome -index indexPath -file filePath -kmer [70] -n [8] -e [4]";

  public static void main(String[] args) throws IOException {
    // parse system arguments
    for (int i = 0; i < args.length; i++) {
      if ("-index".equals(args[i])) {
        indexPath = homePath + args[i + 1];
        i++;
      } else if ("-file".equals(args[i])) {
        filePath = homePath + args[i + 1];
        i++;
      } else if ("-kmer".equals(args[i])) {
        kmerLength = Integer.parseInt(args[i + 1]);
        i++;
      } else if ("-n".equals(args[i])) {
        n = Integer.parseInt(args[i + 1]);
        i++;
      } else if ("-e".equals(args[i])) {
        // if edit distance is given, so index with edit distance
        withED = true;
        e = Integer.parseInt(args[i + 1]);
        i++;
      }
    }

    // in debug mode set all command line parameters
    if (DEBUG) {
      n = DebugConfig.N;
      e = DebugConfig.E;
      withED = DebugConfig.WITHED;
      withHash = DebugConfig.WITHHASH;
      kmerLength = DebugConfig.KMERLENGTH;
      indexPath = DebugConfig.INDEXPATH;
      filePath = DebugConfig.FILEPATH;
    }

    if (indexPath == null || filePath == null) {
      System.out.println(usage);
      System.exit(0);
    }

    // add k-mer length and n to indexPath for prevent confusion
    indexPath += "_" + kmerLength + "_" + n;
    if (withED)
      indexPath += "_withED";
    if (withHash)
      indexPath += "_withHash";

    // get the start time
    Date start = new Date();
    Date end = null;

    System.out.println("Indexing started...\n");
    if (withED)
      System.out.println("Indexing with edit distance\n");
    if (withHash)
      System.out.println("Indexing with hash distance\n");

    Directory dir = FSDirectory.open(new File(indexPath));
    Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_31);
    IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_31, analyzer);

    // create new index and if exist overwrite them
    iwc.setOpenMode(OpenMode.CREATE);

    IndexWriter writer = new IndexWriter(dir, iwc);

    File file = new File(filePath);
    BufferedReader reader = new BufferedReader(new FileReader(file));
    StringBuilder buffer = new StringBuilder();
    int kmerCount = 0;
    int partPointer = 0;
    boolean exit = false;
    while (true) {
      while (buffer.length() < kmerLength + kmerCount) {
        String line = reader.readLine();
        if (line == null) {
          exit = true;
          break;
        }
        line = line.trim();
        // for fasta files
        if (line.startsWith(">") || line.contains("N"))
          continue;
        buffer.append(line);
      }
      if (exit)
        break;
      String kmer = buffer.substring(kmerCount, kmerCount + kmerLength);

      // index k-mer with n-grams
      indexKmer(writer, kmer);
      kmerCount++;

      // save a copy of current index after commit all created docs when
      // kmerCount equals to each amount of index parts.
      int partAmout = indexParts[partPointer] * 1000000;
      if (partAmout == kmerCount) {
        System.out.println("Part " + (partPointer + 1) + " creating started");

        writer.forceMerge(1);
        writer.commit();

        String partPath = indexPath + "_part" + indexParts[partPointer];

        File indexDirectory = new File(indexPath);
        File partDirectory = new File(partPath);
        FileUtils.copyDirectory(indexDirectory, partDirectory);

        System.out.println("Part " + (partPointer + 1) + " was created with "
            + partAmout + " amount of k-mers");

        partPointer++;
      }

      if (kmerCount % 10000 == 0) {
        end = new Date();
        double passed = (double) (end.getTime() - start.getTime()) / 1000;
        double estimated = (double) passed * (file.length() - kmerCount)
            / kmerCount;
        double completed = (double) kmerCount * 100 / file.length();
        System.out.format("%%%.1f of indexing is completed%n", completed);
        System.out.format("%d k-mer indexed in %.1f seconds%n", kmerCount,
            passed);
        System.out.format("Estimated remaining time is %.1f seconds%n",
            estimated);
        System.out.println();
      }
    }
    reader.close();
    System.out.println("Indexing is completed");

    // store all bases to use while searching
    String basesPath = indexPath + ".txt";
    File basesFile = new File(basesPath);
    BufferedWriter basesWriter = new BufferedWriter(new FileWriter(basesFile));
    basesWriter.write(buffer.toString());
    basesWriter.flush();
    basesWriter.close();

    System.out.println("Optimizing index...");
    // before closing index writer optimize index (costly)
    // same with writer.optimize(), but optimize is deprecated
    writer.forceMerge(1);
    writer.close();
    System.out.println("Optimization is completed");

    end = new Date();
    System.out.println("Total time: " + (end.getTime() - start.getTime()));
    System.out.println("Total " + kmerCount + " kmer indexed");
  }

  static void indexKmer(IndexWriter writer, String kmer) throws IOException {
    Document doc = new Document();

    // // store raw kmer for future use
    // Field kmerField = new Field("kmer", kmer, Field.Store.YES,
    // Field.Index.NOT_ANALYZED_NO_NORMS);
    // kmerField.setIndexOptions(IndexOptions.DOCS_ONLY);
    // doc.add(kmerField);

    // first pad the given k-mer
    kmer = extendKmer(kmer, n);

    // create a string contains all possible n-grams in given k-mer
    StringBuilder buffer = new StringBuilder();
    for (int i = 0;; i++) {
      if (kmer.length() < n) {
        break;
      }
      String ngram = kmer.substring(0, n);
      if (withED) {
        for (int j = Math.max(i - e, 0); j <= i + e; j++) {
          String term = ngram + j;
          if (withHash)
            term = term.hashCode() + "";
          buffer.append(term + " ");
        }
      } else {
        String term = ngram;
        if (withHash)
          term = ngram.hashCode() + "";
        buffer.append(term + " ");
      }
      kmer = kmer.substring(1);
    }

    // add created string as contents of doc
    doc.add(new Field(field, buffer.toString(), Field.Store.NO,
        Field.Index.ANALYZED));
    writer.addDocument(doc);
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
