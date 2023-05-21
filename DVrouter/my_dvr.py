import sys
import random
import struct
import socket
import threading
import time
from node import Node

iterator = 0
counter = 0
num_nodes = 0
curr_node = 0
exec_cond = threading.Condition() # condition variable to order the execution of the nodes (A, B, C, D, E, A,...)
counter_lock = threading.Lock()
nodes_changed = [] # list of booleans to indicate if a node has shortest paths to all other nodes

def get_letter(index): # helper function to get the letter of the node
    return chr(index + 65) 

def get_neighbors(matrix, node):
    neighbors = []
    for i in range(len(matrix[node])):
        if matrix[node][i] != 0:
            neighbors.append((i, matrix[node][i]))
    return neighbors

def get_num_nodes(matrix):
    return len(matrix)

def server_startup(node, barrier):
    node.start_server()
    barrier.wait()

def network_init(matrix_file, log):
    global num_nodes, nodes_changed
    matrix = []
    
    with open(matrix_file, 'r') as file: # read in the matrix file
        read_in_matrix = file.readlines()
        for line in read_in_matrix:
            row = list(map(int, line.strip().split())) # convert the line to a list of integers
            matrix.append(row)

    num_nodes = get_num_nodes(matrix) # get the number of nodes in the network
    nodes_changed = [True] * num_nodes # list of booleans to indicate if a node has shortest paths to all other nodes
    nodes = [] # list of Node objects
    node_threads = [] # list of threads for each node

    server_startup_barrier = threading.Barrier(num_nodes + 1) # barrier to wait for all servers to start

    for i in range(len(matrix)):
        neighbors = get_neighbors(matrix, i)
        server_port = (i*15)+50000 # server port is 50000, 50015, 50030, etc.
        client_port = (i*15)+50001 # client port is 50001, 50016, 50031, etc.

        node = Node(i, server_port, client_port, [], log) # create a Node object for each node, dv will be initialized later
        nodes.append(node) 

        t_server = threading.Thread(target=server_startup, args=(node, server_startup_barrier)) # start the server for each node
        t_server.daemon = True
        t_server.start()


    server_startup_barrier.wait() # wait for all servers to start

    for node in nodes:
        neighbors = get_neighbors(matrix, node.node_id)
        node_thread = threading.Thread(target=dv_processing, args=(node, neighbors, log))
        node_threads.append(node_thread)
        node_thread.start() # start the thread for each node

    return matrix, nodes, node_threads # return the matrix and the list of threads



def dv_processing(node, neighbors, log):
    global counter, curr_node, nodes_changed, iterator

    max_num = 2**31-1
    dv = [max_num] * num_nodes # initialize the distance vector
    dv[node.node_id] = 0 # set the distance to itself to 0

    for neighbor, weight in neighbors:
        dv[neighbor] = weight # set the distance to the neighbor to the weight
    
    node.dv = dv # set the node's distance vector to the initialized distance vector

    while True:
        with exec_cond:
            while(curr_node != node.node_id and counter < 100): # wait until it is the node's turn to execute
                exec_cond.wait()

            if curr_node == 0:
                counter+=1
                print("----------------------------------------", end="", file=log)
            print(f"\nRound {counter}: {get_letter(node.node_id)}", file=log)
            

            updated_dv = list(node.dv) # copy the distance vector
            with counter_lock:
                to_byte = struct.pack('i' * (len(updated_dv)+1), node.node_id, *updated_dv) # convert the distance vector to bytes
                for neighbor, _ in neighbors:
                    print(f"Sending DV to node {get_letter(neighbor)}", file=log)
                    node.send_message(neighbor, to_byte) # send the distance vector to each neighbor

                nodes_changed[node.node_id] = False
                with node.received_dvs_lock:
                    for i in range(num_nodes): # update the distance vector
                        for sender_id, received_dv in node.received_dvs:
                            updated_val = min(updated_dv[i], node.dv[sender_id] + received_dv[i]) 
                            if updated_dv[i] != updated_val:
                                nodes_changed[node.node_id] = True # set the node's boolean to True if the distance vector has changed
                            updated_dv[i] = updated_val
                    node.received_dvs = []  # Clear the received distance vectors for the next iteration


                print(f"Current DV = {updated_dv}", file=log)
                print(f"Last DV = {node.dv}", file=log)
                print("Updated from last DV?", end=" ", file=log)
                if nodes_changed[node.node_id]:
                    print("Yes", file=log)
                else:
                    print("No", file=log)
                node.dv = updated_dv
                iterator+=1
                print(iterator)
                
                print("Nodes changed: ", nodes_changed)

                curr_node = (curr_node + 1) % num_nodes # increment curr_node
            exec_cond.notify_all() # signal all threads to execute
                        
            with counter_lock: # if distance vector has not changed (all paths are the shortest), exit the algorithm
                if not any(nodes_changed): 
                    exec_cond.notify_all()
                    break
def main():
    
    if len(sys.argv) == 2:
        matrix_file = sys.argv[1]
    else:
        print("Execute the client in following format: my_dvr.py [matrix file]")
        sys.exit()
    
    log_file = "OUTPUT.txt"
    log = open(log_file, "w")

    matrix, nodes, node_threads = network_init(matrix_file, log) # matrix is a 2D array of integers

    for node_thread in node_threads:
        node_thread.join()
    
    print("----------------------------------------", file=log)
    print("Final output:", file=log)
    for node in nodes:
        print(f"Node {get_letter(node.node_id)} DV = {node.dv}", file=log)

    print("Number of rounds until convergence:", counter, file=log)

    log.close()
if __name__ == '__main__':
    main()