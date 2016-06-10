# Redmine Mail Integration (IMAP only)
The idea is simple, this tool searches for messages in the mailbox, and for each message found it creates a issue in Redmine. If all occur correctly the message is moved to the "OK" folder, otherwise it is moved to the "FAILED" folder.

The tool imports mail the attachments to the issue and convert HTML mail part to Textile standard. All recipients are imported as watchers and the sender is set as the author.

### Features
* Fetch mail messages and create a issue.
* Setup message priority as issue's priority.
* Setup the sender as issue's author
* Setup the recipients as watchers
* HTML mail body is converted to textile and imported into issue's description field.
* Add messages' attachments to issue
* Prevent some signature images to be attached to issue

### Version
1.3.2 - Fixed: attachments with accents, spaces and upercase cause image visualization failure 

1.3.1 - Fixed attachments with explicit filename in multipart/alternative cases.

1.3.0 - Added support application/ms-tnef
      - Feature to clean images with link, usually it's a signature images.
      - Fixed printscreen attachments without name

1.2.0 - Added support MIME Multipart/Related

1.1.3 - Fixed image substitution in the description field

### Roadmap
* Detect replies and generate as comments.

### Dependencies
* Pandoc - http://pandoc.org
* Java JDK 1.8 + Maven

### Installation
1) ```git clone https://github.com/murara/redmine-mail-integration.git```

2) ```mvn install```

3) Create batch file. Example:

```
#!/bin/sh

# Set environment variables
export IMAP_HOSTNAME="imap.example.com"
export IMAP_USERNAME="me@example.com"
export IMAP_PASSWORD="myhardpassword"
export REDMINE_APIKEY="<redmine user api key>"
export REDMINE_HOSTNAME="http://myredmine.example.com:3000"
export REDMINE_PROJECT_ID="99" # only numbers - see "Extras" bellow
export REDMINE_TRACKER_ID="9" # only numbers - see "Extras" bellow

# export PANDOC_PATH="/bin/" # optional - if pandoc is not included in the PATH variable

java -jar /path/to/mail-integration.jar
```

4) Create a crontab (for windows: schedulle) every "n" minutes to execute your batch

### Extras
To get your default project ID and tracker ID use the Redmine API :)

**Projects:** ```http://myredmine.example.com:3000/projects.xml?key=<redmine user api key>```

**Trackers:** ```http://myredmine.example.com:3000/trackers.xml?key=<redmine user api key>```

### Docker

```
docker run --rm=true \
       -e "IMAP_HOSTNAME=imap.example.com" \
       -e "IMAP_USERNAME=me@example.com" \
       -e "IMAP_PASSWORD=myhardpassword" \
       -e "REDMINE_APIKEY=<redmine user api key>" \
       -e "REDMINE_HOSTNAME=http://myredmine.example.com:3000" \
       -e "REDMINE_PROJECT_ID=99" \
       -e "REDMINE_TRACKER_ID=9" \
       murara/redmine-mail-integration
``` 
