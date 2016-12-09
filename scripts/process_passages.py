import sys
import xml.etree.ElementTree as ET
import unicodedata
from collections import OrderedDict

def remove_accents(input_str):
    nfkd_form = unicodedata.normalize('NFKD', input_str)
    only_ascii = nfkd_form.encode('ASCII', 'ignore')
    return only_ascii


f = open('truth_data.txt','w')
fp = open('passages.txt','w')

passages_set = OrderedDict()

tree = ET.parse(sys.argv[1])
root = tree.getroot()

domain_nodes = root.findall('.//domain')
for domain_node in domain_nodes:
    topic_nodes = domain_node.findall('./topic')
    for topic_node in topic_nodes:
        tid = topic_node.get('id')
        subtopics = ""
        subtopic_nodes = topic_node.findall('./subtopic')
        for subtopic_node in subtopic_nodes:
            sid = subtopic_node.get('id')
            passages = subtopic_node.findall('./passage')
            subtopics += sid + '\t'
            for passage in passages:
                pid = passage.get('id')
                text = passage.find('./text').text
                docno = passage.find('./docno').text
                text = remove_accents(unicode(text))
                if not passages_set.has_key(text):
                	pid = len(passages_set)
                	passages_set[text] = pid
                pid = passages_set[text]

                rating = int(passage.find('./rating').text)
                f.write("%s,%s,%s,%d,%d\n"  % (docno, tid, sid, rating, pid))

tree = ET.parse(sys.argv[2])
root = tree.getroot()

domain_nodes = root.findall('.//domain')
for domain_node in domain_nodes:
    topic_nodes = domain_node.findall('./topic')
    for topic_node in topic_nodes:
        tid = topic_node.get('id')
        subtopics = ""
        subtopic_nodes = topic_node.findall('./subtopic')
        for subtopic_node in subtopic_nodes:
            sid = subtopic_node.get('id')
            passages = subtopic_node.findall('./passage')
            subtopics += sid + '\t'
            for passage in passages:
                pid = passage.get('id')
                text = passage.find('./text').text
                docno = passage.find('./docno').text
                text = remove_accents(unicode(text))
                if not passages_set.has_key(text):
                	pid = len(passages_set)
                	passages_set[text] = pid
                pid = passages_set[text]

                rating = int(passage.find('./rating').text)
                f.write("%s,%s,%s,%d,%d\n"  % (docno, tid, sid, rating, pid))


for passage in passages_set:
	fp.write("%d,%s\n" % (passages_set[passage], passage))

f.close()
