cd /root/lucene-ngs

java -d64 -Xms25G -Xmx40G -Xss25G -cp target/dependency/lucene-core-3.6.2.jar:target/lucene-ngs-1.0-SNAPSHOT.jar tr.byildiz.lucenengs.IndexChromosome -index index_chromosome_01 -file chromosome_01.fa -kmer 140 -n 8

java -d64 -Xms25G -Xmx40G -Xss25G -cp target/dependency/lucene-core-3.6.2.jar:target/lucene-ngs-1.0-SNAPSHOT.jar tr.byildiz.lucenengs.CreateQuery -n 100 -e 4 -kmer 70 -file chromosome_01.fa -out query.txt

cat ../index_chromosome_01_140_8/* > /dev/null

java -d64 -Xms25G -Xmx40G -Xss25G -cp target/dependency/lucene-core-3.6.2.jar:target/lucene-ngs-1.0-SNAPSHOT.jar tr.byildiz.lucenengs.SearchChromosome -method AnF -index index_chromosome_01 -query query.txt -n 8 -repeat 1 -results AnF_results.txt -kmer 140
java -d64 -Xms25G -Xmx40G -Xss25G -cp target/dependency/lucene-core-3.6.2.jar:target/lucene-ngs-1.0-SNAPSHOT.jar tr.byildiz.lucenengs.SearchChromosome -method AnFP -index index_chromosome_01 -query query.txt -n 8 -repeat 1 -results AnFP_results.txt -kmer 140
java -d64 -Xms25G -Xmx40G -Xss25G -cp target/dependency/lucene-core-3.6.2.jar:target/lucene-ngs-1.0-SNAPSHOT.jar tr.byildiz.lucenengs.SearchChromosome -method Tocc -index index_chromosome_01 -query query.txt -n 8 -repeat 1 -results Tocc_results.txt -kmer 140
