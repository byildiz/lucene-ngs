cd /root/lucene-ngs

java -d64 -Xms25G -Xmx40G -Xss25G -cp target/lucene-ngs-1.0-SNAPSHOT-jar-with-dependencies.jar tr.byildiz.lucenengs.IndexChromosome -index index_genome -file genome.fasta -kmer 100 -slide 50 -n 12

#java -d64 -Xms25G -Xmx40G -Xss25G -cp target/lucene-ngs-1.0-SNAPSHOT-jar-with-dependencies.jar tr.byildiz.lucenengs.CreateQuery -n 100 -e 4 -kmer 70 -file chromosome_01.fa -out query.txt

#cat ../index_genome_100_12/* > /dev/null

java -d64 -Xms25G -Xmx40G -Xss25G -cp target/lucene-ngs-1.0-SNAPSHOT-jar-with-dependencies.jar tr.byildiz.lucenengs.SearchChromosome -method AnF -index index_genome -query reads1.sim -n 12 -e 3 -repeat 1 -results AnF_results.txt -kmer 100 -pool 1000
