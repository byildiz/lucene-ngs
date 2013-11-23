package tr.byildiz.lucenengs;

public final class DefaultConfig {

  public static final int N = 8;

  public static final int E = 4;

  public static final int Q = 100;

  public static final int REPEAT = 1;

  public static final int KMERLENGTH = 70;

  public static final boolean MORE = false;

  public static final String HOMEPATH = System.getProperty("user.home")
      + System.getProperty("file.separator");

  public static final String FILEPATH = DefaultConfig.HOMEPATH
      + "kmer/dna.fasta";

  public static final String INDEXPATH = DefaultConfig.HOMEPATH
      + "kmer/index_dna";

  public static final String QUERYPATH = DefaultConfig.HOMEPATH
      + "kmer/query.txt";

  public static final String SEARCHMETHOD = "AnF";
  
  public static final boolean INDEXWITHED = false;

  public static final String RESULTSPATH = DefaultConfig.HOMEPATH
      + SEARCHMETHOD + "_results.txt";

  public static final String[] METHODS = { "Tocc", "AnFP", "AnF", "AnFPE" };

  public static final String FIELD = "contents";

}
