# Wikipedia networks of first links

Scala program to construct networks of first links from Wikipedia dumps.


## Instructions

In terminal, open relevant folder and compile with Simple Build Tool:

sbt

Extract first (and second) links for every article from a Wikipedia dump:

runMain wikicycles.parser.WikiXmlConverter dewiki-latest-pages-articles.xml.bz2

Create paths of first links:

runMain wikicycles.analysis.FullPathCreator ../wikidata/dewiki-latest-pages-articles-pagelinks.csv
