package tr.byildiz.lucenengs;

public final class DefaultConfig {

  /**
   * n-grams n
   */
  public static final int N = 8;

  /**
   * edit distance
   */
  public static final int E = 4;

  /**
   * number of query
   */
  public static final int Q = 100;

  /**
   * number of run of each queries
   */
  public static final int REPEAT = 1;

  /**
   * k-mer length
   */
  public static final int KMERLENGTH = 70;
  
  /**
   * indexing offset
   */
  public static final int SLIDE = 1;

  /**
   * if more equal to true, searcher writes found k-mers to results file
   */
  public static final boolean MORE = false;

  /**
   * user home path
   */
  public static final String HOMEPATH = System.getProperty("user.home")
      + System.getProperty("file.separator");

  /**
   * fasta file path
   */
  public static final String FILEPATH = null;

  /**
   * index directory path
   */
  public static final String INDEXPATH = null;

  /**
   * query file path
   */
  public static final String QUERYPATH = null;

  /**
   * default search method
   */
  public static final String SEARCHMETHOD = METHODS.AnF;

  /**
   * if with ed is true, indexer will index terms with their positions and
   * searcher will search with positions
   */
  public static final boolean WITHED = false;

  /**
   * if with hash is true, indexer indexes the hashes of terms and searcher
   * searches with the hashes of terms
   */
  public static final boolean WITHHASH = false;
  
  /**
   * if true, compress terms
   */
  public static final boolean WITHCOMPRESSED = true;

  /**
   * file path to write the search results
   */
  public static final String RESULTSPATH = DefaultConfig.HOMEPATH
      + SEARCHMETHOD + "_results.txt";

  /**
   * available search methods
   */
  public static final class METHODS {
    public static final String Tocc = "Tocc";
    public static final String AnFP = "AnFP";
    public static final String AnF = "AnF";
    public static final String AnFwithED = "AnFPwithED";
    public static final String AnFPwithHash = "AnFPwithHash";
  };

  /**
   * field name
   */
  public static final String FIELD = "contents";

  /**
   * how many k-mer will be stored in each part. each element will be multiplied
   * by a million
   * <p>
   * for example: 1-10,000,000 -> part 1, 1-50,000,000 -> part 2, 1-100,000,000
   * -> part 3, ...
   * <p>
   * Note: index parts must be sorted
   */
  public static final int[] INDEXPARTS = { 10, 50, 100, 150, 200 };
  
  public static final int POLLSIZE = 20;

  public static int INDEXSIZE = 0;

}
