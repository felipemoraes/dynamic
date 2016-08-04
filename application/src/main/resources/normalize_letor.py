import sys

inputfile = open(sys.argv[1])
qrelsfile = open(sys.argv[2])
outputfile = open(sys.argv[3],"w")

qrels = {}

for line in qrelsfile:
    line = line.strip().split()
    topicid = line[0].split('-')[-1]
    docid = line[2]
    relevance = line[-1]
    if not qrels.has_key(topicid):
        qrels[topicid] = {}
    if not qrels[topicid].has_key(docid):
        qrels[topicid][docid] = 0
    if relevance > qrels[topicid][docid]:
        qrels[topicid][docid] = int(relevance)
    
for line in inputfile:
    
    topicid = line.split()[1].split(':',2)[-1]
    docid = line.strip().split()[-1]
    features = line.split(" ",1)[-1]
    if qrels[topicid].has_key(docid):
        relevance = qrels[topicid][docid]
        outputfile.write(str(relevance) + " " + features)
    else:
        outputfile.write("0 " + features)
outputfile.close()
