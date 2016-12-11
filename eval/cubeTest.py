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
# $docID $docLength
docLengthMap = {}

#########################################

#### Read qrels file(groundtruth), check format, and sort
def prepare_qrels(qrelsfile):
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


def evaluate_run(runfile,iterations):
    #### Process runs: compute measures for each topic and average
    results = OrderedDict()
    for line in open(runfile):
        topic, iteration, docno, score, ontopic, subtopics = line.split("\t")
        iteration = int(iteration)
        if not results.has_key(topic):
            results[topic] = OrderedDict()
            results[topic][iteration] = []
        elif not results[topic].has_key(iteration):
            results[topic][iteration] = []
        results[topic][iteration].append(docno)

    all_ct = {}
    all_act = {}
    for i in range(iterations):
        all_ct[i] = 0
        all_act[i] = 0

    ntopics = 0
    for topic, result in results.iteritems():
        if not qrels.has_key(topic):
            continue
        ntopics += 1
        time = 0
        score = 0
        ct_accu = 0
        for iteration in range(iterations):
            if result.has_key(iteration):
                for docno in result[iteration]:
                    gain =  get_doc_gain(topic,docno)
                    score += gain
                    act = score/MAX_HEIGHT
                    ct_accu += act/(iteration+1)
                    time += 1
                    
                ct = score/MAX_HEIGHT
                ct_speed = ct/(iteration+1)
                all_ct[iteration] += ct_speed
                all_act[iteration] += ct_accu/time
    cts = []
    acts = []
    for i in range(iterations):
        cts.append(all_ct[i]/ntopics)
        acts.append(all_act[i]/ntopics)
    return cts, acts

def evaluate_run_q(runfile,iterations):
    #### Process runs: compute measures for each topic and average
    results = OrderedDict()
    for line in open(runfile):
        topic, iteration, docno, score, ontopic, subtopics = line.split("\t")
        iteration = int(iteration)
        if not results.has_key(topic):
            results[topic] = OrderedDict()
            results[topic][iteration] = []
        elif not results[topic].has_key(iteration):
            results[topic][iteration] = []
        results[topic][iteration].append(docno)

    all_ct = {}
    all_act = {}
    for i in range(iterations):
        all_ct[i] = 0
        all_act[i] = 0

    ntopics = 0
    cts = {}
    acts = {}
    for topic, result in results.iteritems():
        if not qrels.has_key(topic):
            continue
        ntopics += 1
        time = 0
        score = 0
        ct_accu = 0
        for iteration in range(iterations):
            if result.has_key(iteration):
                for docno in result[iteration]:
                    gain =  get_doc_gain(topic,docno)
                    score += gain
                    act = score/MAX_HEIGHT
                    ct_accu += act/(iteration+1)
                    time += 1
                    
                ct = score/MAX_HEIGHT
                ct_speed = ct/(iteration+1)
                all_ct[iteration] += ct_speed
                all_act[iteration] += ct_accu/time

        cts[topic] = ct_speed
        acts[topic] = ct_accu/time

    return cts, acts

def main():
    if len(sys.argv) < 3:
        print "Usage: python cubeTest.py qrels runfile iterations"

    qrelsfile = sys.argv[1]
    runfile = sys.argv[2]
    iterations = int(sys.argv[3])

    prepare_qrels(qrelsfile)

    #### Process runs: compute measures for each topic and average
    results = OrderedDict()
    for line in open(runfile):
        topic, iteration, docno, score, ontopic, subtopics = line.split("\t")
        iteration = int(iteration)
        if not results.has_key(topic):
            results[topic] = OrderedDict()
            results[topic][iteration] = []
        elif not results[topic].has_key(iteration):
            results[topic][iteration] = []
        results[topic][iteration].append(docno)

    all_ct = {}
    all_act = {}
    for i in range(iterations):
        all_ct[i] = 0
        all_act[i] = 0


    print "run_id,topic,ct@10,avg_ct@10"
    ntopics = 0
    for topic, result in results.iteritems():
        if not qrels.has_key(topic):
            continue
        ntopics += 1
        time = 0
        score = 0
        ct_accu = 0
        for iteration in range(iterations):
            if result.has_key(iteration):
                for docno in result[iteration]:
                    gain =  get_doc_gain(topic,docno)
                    score += gain
                    act = score/MAX_HEIGHT
                    ct_accu += act/(iteration+1)
                    time += 1
                    
                ct = score/MAX_HEIGHT
                ct_speed = ct/(iteration+1)
                all_ct[iteration] += ct_speed
                all_act[iteration] += ct_accu/time
                print "ct@%d\t%s\t%.10f" % (iteration+1, topic, ct_speed)
                print "avg_ct@%d\t%s\t%.10f" % (iteration+1, topic, ct_accu/time)

        
    for i in range(iterations):
        print "ct@%d\tall\t%.10f" % ( i+1, all_ct[i]/ntopics)
        print "avg_ct@%d\tall\t%.10f" % (i+1, all_act[i]/ntopics)



if __name__ == "__main__":
    main()



    
   




