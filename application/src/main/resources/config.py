import xmltodict
import sys

f_truth = open(sys.argv[1])
truth_data = xmltodict.parse(f_truth.read())

f = open('truth_data.txt','w')
for domain in truth_data['trec_dd']['domain']:
    for topic in domain["topic"]:
        for subtopic in topic["subtopic"]:
            try:
                for passage in subtopic["passage"]:
                    f.write("%s,%s,%s,%s,%s\n" %  (passage["docno"] ,topic["@id"], subtopic["@id"], passage["rating"], passage["text"].replace("\n","")))
            except Exception as e:
                pass

f.close()

