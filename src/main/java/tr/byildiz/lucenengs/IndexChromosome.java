package tr.byildiz.lucenengs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

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

  public static final boolean DEBUG = false;

  public static int n = 8;

  public static int kmerLength = 70;

  public static String homePath = System.getProperty("user.home")
      + System.getProperty("file.separator");

  public static String filePath = null;

  public static String indexPath = null;

  public static String field = "contents";

  public static String usage = "Usage: IndexChromosome -index indexPath -file filePath -kmer [70] -n [8]";

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
      }
    }

    // add k-mer length and n to indexPath for prevent confusion
    indexPath += "_" + kmerLength + "_" + n;

    // in debug mode set all command line parameters
    if (DEBUG) {
      n = 8;
      kmerLength = 70;
      indexPath = "/home/byildiz/kmer/index_dna_70_8";
      filePath = "/home/byildiz/kmer/dna.fasta";
    }

    if (indexPath == null || filePath == null) {
      System.out.println(usage);
      System.exit(0);
    }

    // get the start time
    Date start = new Date();
    Date end = null;

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
      // buffer = buffer.delete(0, 1);

      // index k-mer with n-grams
      indexKmer(writer, kmer, n);
      kmerCount++;

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
    writer.forceMerge(1); // same with writer.optimize()
    writer.close();
    System.out.println("Optimization is completed");

    end = new Date();
    System.out.println("Total time: " + (end.getTime() - start.getTime()));
    System.out.println("Total " + kmerCount + " kmer indexed");
  }

  static void indexKmer(IndexWriter writer, String kmer, int n)
      throws IOException {

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
    while (true) {
      if (kmer.length() < n) {
        break;
      }
      buffer.append(kmer.substring(0, n) + " ");
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
