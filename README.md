# Wikipedia networks of first links

Scala and Mathematica programs to construct networks of first links from Wikipedia dumps.

## Reference

"Structures of Knowledge from Wikipedia Networks," Maxime Gabella, https://arxiv.org/abs/1708.05368


## Instructions

1.  Choose a Wikipedia backup dump from https://dumps.wikimedia.org and download the file with a name of the form "enwiki-20180220-pages-articles.xml.bz2" (see for example https://dumps.wikimedia.org/enwiki/).

2.  In a terminal, compile Scala program with Simple Build Tool:

    $ sbt

    Extract first and second links for every article from the Wikipedia dump:

    $ runMain wikicycles.parser.WikiXmlConverter WIKI_DUMP/enwiki-20180220-pages-articles.xml.bz2
    
    where "WIKI_DUMP" is the directory in which you saved the Wikipedia dump. The result is written to a file "enwiki-20180220-pages-articles-pagelinks.csv".

    Create a path of first links starting from every article and ending at the first repetition (if a first link is invalid, the second link is used):

    $ runMain wikicycles.analysis.FullPathCreator WIKI_DUMP/enwiki-20180220-pages-articles-pagelinks.csv

3.  The resulting file "enwiki-20180220-pages-articles-pagelinks-fullpaths.csv" can then be imported with the Mathematica notebook WikiNets.nb to construct and analyse Wikipedia networks of first links.
