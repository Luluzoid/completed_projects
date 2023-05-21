Run the program in following format:

python my_dvr.py [matrix]

Example:
python my_dvr.py network.txt

The .txt file should be in a matrix format.

Example:
0 2 0 0 1
2 0 5 0 0
0 5 0 4 0
0 0 4 0 1
1 0 0 1 0

Description of the program:
Calculates shortest paths to all nodes by using Bellman-Ford's algorithm (distance vector) and prints it to OUTPUT.txt.
Each node is a thread that uses client and server sockets to send and receive information.
Each node send in order, (A then B then C...)
Up to 5 nodes in the matrix, but can be less.
Each row represents the weights from the node with the row's index to the node with the column's index.
For example, in the matrix above, the cost from Node A (row 0) to Node E (col 4) would be 1.