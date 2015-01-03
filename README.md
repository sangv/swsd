# Simple Word Sense Disambiguation Library (swsd)

A simple word sense disambiguation library written in scala with a java api

## Dependencies:

Wordnet 3.0 (using extJWNL)

StanforNLP

Neo4j graph database and scala-gremlin

## Approach:

On input text, perform part-of-speech tagging

On tagged text, derive intersection points for the different possible synsets of the word

Disambiguate words that can be associated with one or more intersection points

For the remaining words, disambiguate to most popular context for that part of speech

## Future Work:

Add support for word compoundification

Provide a simple play webapp to deploy this as a REST service

Research into other alternatives to using StanfordNLP

Research into using other graph databases or in-memory graphs like TinkerGraph etc.



