import socket
import threading
import time
import struct

# Node class to represent a node in the network:
#
# node_id: the id of the node
# server_port: the port number the node will listen on
# client_port: the port number the node will send messages from
# socket_table: a dictionary of node_id to Node objects
# server_socket: the socket the node will listen on
# client_socket: the socket the node will send messages from 
# dv: the distance vector for the node
# log: the log file

class Node:
    socket_table = {} # dictionary of node_id to Node objects, shared by all nodes
    log = None
    def __init__(self, node_id, server_port, client_port, dv, log=None):
        self.node_id = node_id
        self.server_port = server_port
        self.client_port = client_port
        self.server_socket = None
        self.client_socket = None
        Node.socket_table[node_id] = self # add the created node to the socket table
        self.dv = dv # distance vector for the node
        self.log = log
        self.received_dvs = []  # Store the received distance vectors
        self.received_dvs_lock = threading.Lock()  # Lock to protect access to received_dvs, used in client_connection and dv_processing

    def start_server(self):
        def get_letter(index): # helper function to get the letter of the node
            return chr(index + 65) 
        
        def client_connection(conn):
            message = conn.recv(1024)
            if message:
                num_of_ints = len(message) // 4  # each integer is 4 bytes, so the number of integers is the length of the message divided by 4
                data = struct.unpack('i' * num_of_ints, message) # unpack the message into a list of integers
                sender_id, received_dv = data[0], data[1:] # the first integer is the sender_id, the rest is the distance vector
                print(f"Node {get_letter(self.node_id)} received DV from: {get_letter(sender_id)}", file=self.log)
                with self.received_dvs_lock: 
                    self.received_dvs.append((sender_id, list(received_dv)))  # Store the received distance vector
            conn.close()

        def server_thread(): # thread to listen for incoming connections
            self.server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            self.server_socket.bind(('localhost', self.server_port))
            self.server_socket.listen(5) # listen for up to 5 connections since we have up to 5 nodes

            while True: 
                conn, _ = self.server_socket.accept() # accept a connection
                threading.Thread(target=client_connection, args=(conn,)).start() # start a thread to handle the connection

        threading.Thread(target=server_thread).start()

    def send_message(self, target_node_id, message): # send a message to a target node, used in dv_processing
        if target_node_id not in Node.socket_table: # check if the target node is in the socket table
            print(f"Node {target_node_id} not found.")
            return
        
        target_node = Node.socket_table[target_node_id]
        self.client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM) # create a socket
        self.client_socket.connect(('localhost', target_node.server_port)) # connect to the target node
        self.client_socket.sendall(message) # send the message
        self.client_socket.close() # close the socket since we are done sending
