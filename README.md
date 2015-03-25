# graphene-instagram
Prep:
Python 2.7
Install a yaml parser and any other libs it complains about
Create the data directory and it's subdirectories, as well as the logs directory


Doing the scrape:
Put a starting file in the queue directory and end the file with a .q, i.e.  start.q
Inside that file put any userids to use as seeds.

For the config.yaml, comment out the hdfs and es url for now, and leave as empty strings.
Perform the scrap with bin/run.sh &


After doing a scrape of data:
0) Setup your elastic search server and have it running.
1) uncomment and modify the esurl to point to your elasticsearch server i.e.  http://localhost:9200
1.1) pip install elasticsearch if you don't have it
2) python src/ingest.py conf/config.yaml 
3) ???
4) Profit!



