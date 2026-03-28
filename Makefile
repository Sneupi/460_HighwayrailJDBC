SHELL:=/bin/bash
-include .env
export CLASSPATH := .:ojdbc8.jar

.PHONY: clean scrub example

# clean local files
clean: 
	rm -f *.class

# ensure files ready to insert
scrub: highway*.csv clean
	javac Scrubber.java
	java Scrubber highway*.csv

example:
	javac JDBC.java
	java JDBC $(ORACLE_USERNAME) $(ORACLE_PASSWORD)

