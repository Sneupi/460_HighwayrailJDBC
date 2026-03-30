SHELL:=/bin/bash
-include .env
export CLASSPATH := .:${DB_DRIVER}

.PHONY: clean prog3 example backup fetch login login_db create_tables drop_tables

# clean local files
clean: 
	rm -f *.db
	rm -f *.class

# run prog3 assignment
prog3: clean
	javac Prog3.java
	java Prog3 ${DB_CLASSNAME} $(DB_URL)

# JDBC example
example:
	javac JDBC.java
	java JDBC $(DB_USERNAME) $(DB_PASSWORD)

# backup proj to SSH
backup: clean
	scp -r * ${SSH}:${SSH_PATH}

# fetch files from SSH
fetch: clean
	scp -r ${SSH}:${SSH_PATH}/* .

# login to SSH
login:
	ssh ${SSH}

# login to DB shell
login_db:
# for H2 db
	java -cp h2*.jar org.h2.tools.Shell -url ${DB_URL}
# for Oracle db
# 	sqlpl ${DB_USERNAME}/${DB_PASSWORD}@oracle.aloe

# create tables
create_tables: drop_tables
	javac CreateTable.java
	-java CreateTable ${DB_USERNAME} highwayrail1980.csv ${DB_CLASSNAME} ${DB_URL}
	-java CreateTable ${DB_USERNAME} highwayrail1995.csv ${DB_CLASSNAME} ${DB_URL}
	-java CreateTable ${DB_USERNAME} highwayrail2010.csv ${DB_CLASSNAME} ${DB_URL}
	-java CreateTable ${DB_USERNAME} highwayrail2025.csv ${DB_CLASSNAME} ${DB_URL}
	

# drop tables
drop_tables:
	javac DropTable.java
	-java DropTable ${DB_USERNAME} highwayrail1980.csv ${DB_CLASSNAME} ${DB_URL}
	-java DropTable ${DB_USERNAME} highwayrail1995.csv ${DB_CLASSNAME} ${DB_URL}
	-java DropTable ${DB_USERNAME} highwayrail2010.csv ${DB_CLASSNAME} ${DB_URL}
	-java DropTable ${DB_USERNAME} highwayrail2025.csv ${DB_CLASSNAME} ${DB_URL}