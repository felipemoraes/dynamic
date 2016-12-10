import sys
f = open(sys.argv[2] +"_" + sys.argv[1], "w")
for line in open(sys.argv[1]):
    if line.split()[0] == sys.argv[2]:
        f.write(line)
f.close()
