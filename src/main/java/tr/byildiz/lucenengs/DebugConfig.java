package tr.byildiz.lucenengs;

public final class DebugConfig {

  public static final boolean DEBUG = false;

  public static final int N = 8;

  public static final int E = 4;

  public static final int Q = 100;

  public static final int REPEAT = 1;

  public static final int KMERLENGTH = 70;

  public static final boolean MORE = false;

  public static final String FILEPATH = DefaultConfig.HOMEPATH
      + "kmer/genome.fasta";

  public static final String INDEXPATH = DefaultConfig.HOMEPATH
      + "kmer/index_genome";

  public static final String QUERYPATH = DefaultConfig.HOMEPATH
      + "kmer/read1.sim";

  public static final String SEARCHMETHOD = DefaultConfig.METHODS.AnFPwithHash;

  public static final String RESULTSPATH = DefaultConfig.HOMEPATH
      + SEARCHMETHOD + "_results.txt";

  public static final boolean WITHED = false;

  public static final boolean WITHHASH = false;

}
