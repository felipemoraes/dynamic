import sys
import xml.etree.ElementTree as ET



tree = ET.parse(sys.argv[1])
root = tree.getroot()


f = open('truth_data_2016.txt','w')
ft =  open('topics_domain_2016.txt','w')
domain_nodes = root.findall('.//domain')

for domain_node in domain_nodes:
    did = int(domain_node.get('id'))
    topic_nodes = domain_node.findall('./topic')
    for topic_node in topic_nodes:
        tid = topic_node.get('id')
        ft.write("%s %s %s\n" % (domain_node.get('name'), topic_node.get('id'), topic_node.get('name')))
        subtopics = ""
        subtopic_nodes = topic_node.findall('./subtopic')
        for subtopic_node in subtopic_nodes:
            sid = subtopic_node.get('id')
            passages = subtopic_node.findall('./passage')
            subtopics += sid + '\t'
            for passage in passages:
                pid = passage.get('id')
                docno = passage.find('./docno').text
                text = passage.find('./text').text
                rating = int(passage.find('./rating').text)
                type = passage.find('./type').text
                if type == 'MATCHED':
                    score == float(passage.find('./score').text)
                else:
                    score = None
                f.write( "%s,%s,%s,%d,%s\n"  % (docno, tid, sid, rating, text.encode("utf8")))

f.close()
