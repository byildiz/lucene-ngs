package tr.byildiz.lucenengs;

import java.util.Date;
import java.util.concurrent.Callable;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;

public class SearchWorker implements Callable<SearchWorker.Results[]> {

  public int no;

  public IndexSearcher searcher;

  public Query[] queries;

  public SearchWorker(int n, IndexSearcher s, Query[] q) {
    no = n;
    searcher = s;
    queries = q;

    System.out.println("Search worker #" + n + " was created");
  }

  @Override
  public Results[] call() throws Exception {
    System.out.println("Search worker #" + no + " is starting to search");

    int queryCount = queries.length;
    long totalWorkTime = 0;
    Results[] results = new Results[queryCount];
    for (int i = 0; i < queryCount; i++) {
      Date start = new Date();
      TopDocs topDocs = searcher.search(queries[i], Integer.MAX_VALUE);
      Date end = new Date();
      long workTime = end.getTime() - start.getTime();
      results[i] = new Results(topDocs, workTime);
      totalWorkTime += workTime;
    }
    System.out.println("Search worker #" + no + " searched " + queryCount
        + " at " + totalWorkTime + " ms");

    return results;
  }

  public static class Results {
    public TopDocs topDocs;
    public long workTime;

    public Results(TopDocs td, long w) {
      topDocs = td;
      workTime = w;
    }
  }

}
