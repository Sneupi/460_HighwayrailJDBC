SHELL:=/bin/bash
-include .env
export CLASSPATH := .:ojdbc8.jar

.PHONY: clean scrub prog3 example backup fetch login

# clean local files
clean: 
	rm -f *.class

# ensure files ready to insert
scrub: highway*.csv clean
	javac Scrubber.java
	java Scrubber highway*.csv

# run prog3 assignment
prog3: clean
	javac Prog3.java
	java Prog3 $(ORACLE_URL) $(ORACLE_USERNAME) $(ORACLE_PASSWORD)

# JDBC example
example:
	javac JDBC.java
	java JDBC $(ORACLE_USERNAME) $(ORACLE_PASSWORD)

# backup proj to SSH
backup: clean
	scp -r * ${SSH}:${SSH_PATH}

# fetch files from SSH
fetch: clean
	scp -r ${SSH}:${SSH_PATH}/* .

# login to SSH
login:
	ssh ${SSH}
