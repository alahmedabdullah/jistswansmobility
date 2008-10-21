
#java jist.runtime.Main driver.aodvsim  -n 100 -f 500x300 -l uniform:0.02 -a random -m waypoint:0:1:0:20   -t 10,300,10 -x true

#java jist.runtime.Main driver.aodvsim  -n 100 -f 500x300 -l uniform:0.02 -a random -m uniform_rect:0:20:10:5   -t 10,300,10 -x true


#java jist.runtime.Main driver.aodvsim  -n 100 -f 500x300 -l uniform:0.02 -a random -m nomande_rect_uniform:0:20:5000:10:50:0:5   -t 10,300,10 -x true

java jist.runtime.Main driver.aodvsim  -n 100 -f 500x300 -l uniform:0.02 -a random -m gauss_markov:0:20:3:3.14:0.75:0.2   -t 10,300,10 -x true



