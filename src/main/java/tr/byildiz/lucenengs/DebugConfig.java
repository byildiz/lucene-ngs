package tr.byildiz.lucenengs;

public final class DebugConfig {

  public static final boolean DEBUG = false;

  public static final int N = 12;

  public static final int E = 3;

  public static final int Q = 100;

  public static final int REPEAT = 1;

  public static final int KMERLENGTH = 100;

  public static final int SLIDE = 50;

  public static final boolean VERBOSE = false;

  public static final String FILEPATH = DefaultConfig.HOMEPATH
      + "kmer/genome.fasta";

  public static final String INDEXPATH = DefaultConfig.HOMEPATH
      + "kmer/index_genome";

  public static final String QUERYPATH = DefaultConfig.HOMEPATH
      + "kmer/reads1.sim";

  public static final String SEARCHMETHOD = DefaultConfig.METHODS.AnF;

  public static final String RESULTSPATH = DefaultConfig.HOMEPATH
      + SEARCHMETHOD + "_results.txt";

  public static final boolean WITHED = false;

  public static final boolean WITHHASH = false;

  public static final int POLLSIZE = 10;

}
