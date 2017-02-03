# -*- encoding: utf-8 -*-
from collections import OrderedDict
import sys
import math

MAX_JUDGMENT = 4
MAX_HEIGHT = 5
beta = 1
gamma = 0.5

# $topic $docno $subtopic $judgement
qrels = {}
#$topic $subtopic $area
subtopic_weight = {}
# $topic $subtopic $gainHeights
current_gain_height = {}
# $topic $subtopic $occurrences
subtopic_cover = {}

#########################################

#### Read qrels file(groundtruth), check format, and sort
def prepare_qrels(qrelsfile):
    global qrels, subtopic_weight, current_gain_height, subtopic_cover
    # $topic $docno $subtopic $judgement
    qrels = {}
    #$topic $subtopic $area
    subtopic_weight = {}
    # $topic $subtopic $gainHeights
    current_gain_height = {}
    # $topic $subtopic $occurrences
    subtopic_cover = {}
    tmp_qrels = {}
    count = 0
    for line in open(qrelsfile):
        topic, subtopic, docno, passage, judgment = line.strip().split('\t')
        if int(judgment) > 0 :
            judgment = int(judgment)
        else :
            judgment = 1
        if not tmp_qrels.has_key(topic):
            tmp_qrels[topic] = {}
            tmp_qrels[topic][docno] = {}
            tmp_qrels[topic][docno][subtopic] = {}
        if not tmp_qrels[topic].has_key(docno):
            tmp_qrels[topic][docno] = {}
            tmp_qrels[topic][docno][subtopic] = {}
        if not tmp_qrels[topic][docno].has_key(subtopic):
            tmp_qrels[topic][docno][subtopic] = {}

        tmp_qrels[topic][docno][subtopic][passage] = judgment

    for topic, docs in tmp_qrels.iteritems():
        for docno, subtopics in docs.iteritems():
            for subtopic, rels in subtopics.iteritems():
                rels = sorted(rels.values(),reverse=True)  
                log2 = math.log(2)
                rel = sum([ rel/(math.log(rank+2)/log2)  for rank, rel in enumerate(rels)])
                
                if not qrels.has_key(topic):
                    qrels[topic] = {}
                    subtopic_weight[topic] = {}
                    current_gain_height[topic] = {}
                    subtopic_cover[topic] = {}
                    qrels[topic][docno] = {}
                    qrels[topic][docno][subtopic] = {}
                if not qrels[topic].has_key(docno):
                    qrels[topic][docno] = {}
                    qrels[topic][docno][subtopic] = {}
                if not qrels[topic][docno].has_key(subtopic):
                    qrels[topic][docno][subtopic] = {}
                
                qrels[topic][docno][subtopic] = rel
                subtopic_weight[topic][subtopic] = 1
                current_gain_height[topic][subtopic] = 0
                subtopic_cover[topic][subtopic] = 0

    #### Normalize subtopic weight

    for topic, subtopics in subtopic_weight.iteritems():
        max_weight = get_max_weight(topic)
        for subtopic in subtopics:
            subtopic_weight[topic][subtopic] /= float(max_weight)

def get_doc_gain(topic,docno):
    gain = 0
    for subtopic, area in subtopic_weight[topic].iteritems():
        nrel = subtopic_cover[topic][subtopic]
        if qrels[topic].has_key(docno):
            if not qrels[topic][docno].has_key(subtopic):
                continue
        else:
            continue
        hight_keepfilling = get_hight_keepfilling(topic, docno, subtopic, nrel+1)
        area = get_area(topic,subtopic)
        
        gain += area*hight_keepfilling
        
    return gain

def update_doc_gain(topic,docno):
    gain = 0
    for subtopic, area in subtopic_weight[topic].iteritems():
        nrel = subtopic_cover[topic][subtopic]
        if qrels[topic].has_key(docno):
            if not qrels[topic][docno].has_key(subtopic):
                continue
        else:
            continue
        hight_keepfilling = update_hight_keepfilling(topic, docno, subtopic, nrel+1)
        area = get_area(topic,subtopic)
        
        gain += area*hight_keepfilling
        subtopic_cover[topic][subtopic] +=1
    return gain

def get_hight_keepfilling(topic, docno, subtopic, nrel):
    
    rel = 0
    if qrels[topic].has_key(docno):
        if qrels[topic][docno].has_key(subtopic):
            rel = qrels[topic][docno][subtopic]
    if rel == 0:
        return 0
    
    current_gain = current_gain_height[topic][subtopic]

    gain = get_hight_discount(nrel)*rel
    return gain

def update_hight_keepfilling(topic, docno, subtopic, nrel):
    
    rel = 0
    if qrels[topic].has_key(docno):

        if qrels[topic][docno].has_key(subtopic):
            rel = qrels[topic][docno][subtopic]
    if rel == 0:
        return 0
    
    current_gain = current_gain_height[topic][subtopic]

    gain = get_hight_discount(nrel)*rel
    if current_gain + gain > MAX_HEIGHT: 
        gain = MAX_HEIGHT - current_gain
    current_gain_height[topic][subtopic] += gain
    return gain

def get_area(topic, subtopic):
    if subtopic_weight[topic].has_key(subtopic):
        return subtopic_weight[topic][subtopic]
    return 0

def get_hight_discount(nrels):
    return gamma ** nrels

def get_max_weight(topic):
    max_weight = sum([v for v in subtopic_weight[topic].values()])
    return max_weight


def perfect_run(qrelsfile):
    prepare_qrels(qrelsfile)
    for topic, docs in qrels.iteritems():
        candidate_docs = docs.keys()
        best_docs = set()
        i = 0
        while len(best_docs) < len(candidate_docs):
            best_doc = "-"
            best_gain = -1
            
            for docno in candidate_docs:
                if docno in best_docs:
                    continue
                gain = get_doc_gain(topic,docno)
                if gain > best_gain:
                    best_doc = docno
                    best_gain = gain
            update_doc_gain(topic,docno)
            best_docs.add(best_doc)
            print "%s\t%s\t%s\t%f\t1\t%s" % (topic, (i/5), best_doc, ((len(candidate_docs)-i)/float(len(candidate_docs))), "|".join(["%s:%f" % (subtopic, qrels[topic][best_doc][subtopic])  for subtopic in qrels[topic][best_doc]]))
            i+=1
        

perfect_run(sys.argv[1])




    
   




