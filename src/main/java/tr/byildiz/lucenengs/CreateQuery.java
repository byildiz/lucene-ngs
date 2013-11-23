package tr.byildiz.lucenengs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Random;

public class CreateQuery {

  public static final boolean DEBUG = false;

  public static int n = 100;

  public static int e = 4;

  public static int kmerLength = 70;

  public static String homePath = System.getProperty("user.home")
      + System.getProperty("file.separator");

  public static String filePath = null;

  public static String queryPath = null;

  public static Random random = new Random(System.currentTimeMillis());

  public static String usage = "Usage: CreateQuery -n [100] -e [4] -kmer [70] -file filePath -out queryPath";

  public static void main(String[] args) throws Exception {
    // in debug mode set all command line parameters
    if (DEBUG) {
      n = 100;
      e = 4;
      kmerLength = 70;
      filePath = "/home/byildiz/kmer/dna.fasta";
      queryPath = "/home/byildiz/kmer/query.txt";
    }

    // parse system arguments
    for (int i = 0; i < args.length; i++) {
      if ("-file".equals(args[i])) {
        filePath = homePath + args[i + 1];
        i++;
      } else if ("-out".equals(args[i])) {
        queryPath = homePath + args[i + 1];
        i++;
      } else if ("-e".equals(args[i])) {
        e = Integer.parseInt(args[i + 1]);
        i++;
      } else if ("-n".equals(args[i])) {
        n = Integer.parseInt(args[i + 1]);
        i++;
      } else if ("-kmer".equals(args[i])) {
        kmerLength = Integer.parseInt(args[i + 1]);
        i++;
      }
    }

    if (filePath == null) {
      System.out.println(usage);
      System.exit(0);
    }

    BufferedReader reader = new BufferedReader(new FileReader(filePath));
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

    String sequence = buffer.toString();
    int totalKmerCount = sequence.length() - kmerLength + 1;

    BufferedWriter writer = new BufferedWriter(new FileWriter(queryPath));
    for (int i = 0; i < n; i++) {
      // find a random k-mer
      int randKmerNo = random.nextInt(totalKmerCount);
      String kmer = sequence.substring(randKmerNo, randKmerNo + kmerLength);

      // change k-mer a little bit
      for (int j = 0; j < e; j++) {
        int randOp = random.nextInt(3);
        switch (randOp) {
          case 0:
            kmer = delete(kmer);
            break;
          case 1:
            kmer = insert(kmer);
            break;
          case 2:
            kmer = substitute(kmer);
            break;
        }
      }

      // write the changed k-mer to query file
      writer.write(kmer);
      writer.newLine();
    }
    writer.flush();
    writer.close();

    System.out.println("Query file created: " + queryPath);
  }

  /**
   * delete one random char of given kmer and return it
   * 
   * @param kmer
   * @return kmer
   */
  private static String delete(String kmer) {
    int rand = random.nextInt(kmer.length());
    return kmer.substring(0, rand) + kmer.substring(rand + 1);
  }

  /**
   * insert random char {A, C, G, T} at a random position to given kmer
   * 
   * @param kmer
   * @return kmer
   */
  private static String insert(String kmer) {
    char[] bases = { 'A', 'C', 'G', 'T' };
    int pos = random.nextInt(bases.length);
    int rand = random.nextInt(kmer.length());
    return kmer.substring(0, rand) + bases[pos] + kmer.substring(rand);
  }

  /**
   * substitute a random char from given k-mer with a random char {A, C, G, T}
   * 
   * @param kmer
   * @return kmer
   */
  private static String substitute(String kmer) {
    char[] bases = { 'A', 'C', 'G', 'T' };
    int pos = random.nextInt(bases.length);
    int rand = random.nextInt(kmer.length());
    return kmer.substring(0, rand) + bases[pos] + kmer.substring(rand + 1);
  }
}
