swsd
====

A simple word sense disambiguation library

Dependencies:

Wordnet (using extJWNL)
StanforNLP
Tinkerpop graph database and scala-gremlin

Approach:
On input text, perform part-of-speech tagging
On tagged text, derive intersection points for the different possible synsets of the word
Disambiguate words that can be associated with one or more intersection points
For the remaining words, disambiguate to most popular context for that part of speech
