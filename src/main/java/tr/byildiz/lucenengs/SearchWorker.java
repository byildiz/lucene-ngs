package tr.byildiz.lucenengs;

import java.util.concurrent.Callable;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;

public class SearchWorker implements Callable<SearchWorker.Results> {

  public int id;

  public int poolSize;

  public int kmerLength;

  public boolean verbose;

  public IndexSearcher searcher;

  public Query[] queries;

  public SearchWorker(int i, IndexSearcher s, Query[] q, int p, int l, boolean v) {
    id = i;
    searcher = s;
    queries = q;
    poolSize = p;
    verbose = v;

    System.out.println("Search worker #" + i + " was created");
  }

  @Override
  public Results call() throws Exception {
    if (verbose)
      System.out.println("Search worker #" + id + " is starting to search");

    int queryCount = queries.length;
    long workTime = 0;
    long lastWorkTime = 0;
    int totalHits = 0;
    long start = System.currentTimeMillis();

    int amount = (int) Math.ceil((double) queryCount / poolSize);
    for (int i = amount * id; i < amount * (id + 1) && i < queryCount; i++) {
      Query query = queries[i];
      TopDocs topDocs = searcher.search(query, Integer.MAX_VALUE);
      long end = System.currentTimeMillis();
      workTime = end - start;
      totalHits += topDocs.totalHits;
      if (verbose) {
        System.out.println("Search worker #" + id + " searched for query: "
            + query + " in " + (workTime - lastWorkTime) + " ms");
        for (int j = 0; j < topDocs.totalHits; j++) {
          int docId = topDocs.scoreDocs[j].doc;
          System.out.println("Search worker #" + id + " found a kmer: "
              + Utils.getBases(docId, SearchChromosome.kmerLength));
        }
      }
      lastWorkTime = workTime;
    }

    if (verbose)
      System.out.println("Search worker #" + id + " finished searching in "
          + workTime + " ms");

    return new Results(id, workTime, totalHits);
  }

  public class Results {
    public int threadId;
    public long workTime;
    public int totalHits;

    public Results(int i, long t, int h) {
      threadId = i;
      workTime = t;
      totalHits = h;
    }
  }

}
