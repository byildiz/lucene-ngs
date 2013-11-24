cd /root/lucene-ngs

java -d64 -Xms25G -Xmx40G -Xss25G -cp target/dependency/log4j-1.2.17.jar:target/dependency/commons-io-2.4.jar:target/dependency/lucene-core-4.5.1.jar:target/lucene-ngs-1.0-SNAPSHOT.jar tr.byildiz.lucenengs.IndexChromosome -index index_genome -file genome.fasta -kmer 140 -offset 70 -n 12

#java -d64 -Xms25G -Xmx40G -Xss25G -cp target/dependency/log4j-1.2.17.jar:target/dependency/commons-io-2.4.jar:target/dependency/lucene-core-4.5.1.jar:target/lucene-ngs-1.0-SNAPSHOT.jar tr.byildiz.lucenengs.CreateQuery -n 100 -e 4 -kmer 70 -file chromosome_01.fa -out query.txt

cat ../index_genome_140_12/* > /dev/null

java -d64 -Xms25G -Xmx40G -Xss25G -cp target/dependency/log4j-1.2.17.jar:target/dependency/commons-io-2.4.jar:target/dependency/lucene-core-4.5.1.jar:target/lucene-ngs-1.0-SNAPSHOT.jar tr.byildiz.lucenengs.SearchChromosome -method AnF -index index_genome -query read1.sim -n 12 -e 3 -repeat 1 -results AnF_results.txt -kmer 140
#java -d64 -Xms25G -Xmx40G -Xss25G -cp target/dependency/log4j-1.2.17.jar:target/dependency/commons-io-2.4.jar:target/dependency/lucene-core-4.5.1.jar:target/lucene-ngs-1.0-SNAPSHOT.jar tr.byildiz.lucenengs.SearchChromosome -method AnFP -index index_chromosome_01 -query query.txt -n 8 -repeat 1 -results AnFP_results.txt -kmer 140
#java -d64 -Xms25G -Xmx40G -Xss25G -cp target/dependency/log4j-1.2.17.jar:target/dependency/commons-io-2.4.jar:target/dependency/lucene-core-4.5.1.jar:target/lucene-ngs-1.0-SNAPSHOT.jar tr.byildiz.lucenengs.SearchChromosome -method Tocc -index index_chromosome_01 -query query.txt -n 8 -repeat 1 -results Tocc_results.txt -kmer 140
