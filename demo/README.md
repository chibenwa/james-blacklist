Run:

```
mvn clean install
```

Then copy:

```
blacklist-mailet/target/blacklist-mailet-3.5.0-SNAPSHOT-jar-with-dependencies.jar
blacklist-webadmin/target/blacklist-webadmin-3.5.0-SNAPSHOT-jar-with-dependencies.jar
blacklist-memory/target/blacklist-memory-3.5.0-SNAPSHOT-jar-with-dependencies.jar
blacklist-mailbox/target/blacklist-mailbox-3.5.0-SNAPSHOT-jar-with-dependencies.jar
```

into extensions-jars folder.

Then launch james:

```
docker run -d -p 25:25 -p 143:143 -p 8000:8000 -v $PWD/conf:/root/conf -v $PWD/extensions-jars:/root/extensions-jars --name aceu apache/james:memory-3.7.0
```

To play with James you need to:

 - Create a domain:
 
```
docker exec aceu java -jar /root/james-cli.jar -h 127.0.0.1 -p 9999 AddDomain domain.tld
```

 - Create a recipient
 
```
docker exec aceu java -jar /root/james-cli.jar -h 127.0.0.1 -p 9999 AddUser recipient@domain.tld pass
```

 - Add someone in the blacklist
 
```
curl -XPUT http://127.0.0.1:8000/blacklist/domain.tld/sender@spammer.com
```

 - To check the content of the black list:
 
```
curl -XGET http://127.0.0.1:8000/blacklist/domain.tld
```

 - Send emails using telnet for instance
 
```
telnet 127.0.0.1 25
EHLO spammer.com
MAIL FROM: <sender@spammer.com>
RCPT TO: <recipient@domain.tld>
DATA
Subject: This mail should be blocked

Is it?
.
quit
```

```
telnet 127.0.0.1 25
EHLO spammer.com
MAIL FROM: <another@spammer.com>
RCPT TO: <recipient@domain.tld>
DATA
Subject: This mail should be received

Is it?
.
quit
```

To see rejected messages:

```
curl -XGET  http://127.0.0.1:8000/mailRepositories/var%2Fmail%2Fblacklisted
```

To see accepted messages:

```
telnet 127.0.0.1 143
a001 LOGIN recipient@domain.tld pass
a002 select INBOX
a003 FETCH 1:* BODY[HEADER.FIELDS (SUBJECT)] 
```
