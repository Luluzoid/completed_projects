import sys
import random
import struct
import socket

def main():
    if len(sys.argv) == 2:
        host_name = sys.argv[1]
    else:
        print("Execute the client in following format: python my-dns-client.py [host name]")
        sys.exit()
    print("--------------------------------------------------------------------\n")
    print("Preparing DNS query..")
    ID = random.randint(0, 65535) # generates random 16-bit number
    QR = 0
    OPCODE = 0
    AA = 0
    TC = 0
    RD  = 1
    RA = 0 
    Z = 0 # "Reserved for future use. Must be zero in all queries and responses."
    RCODE = 0 # set in responses, this is a query
    QDCOUNT = 1 
    ANCOUNT = 0
    NSCOUNT = 0
    ARCOUNT = 0

    # Packing into 2-byte shorts since each layer is 2 bytes (H stands for unsigned int)
    query_header = struct.pack('!HHHHHH', ID, (QR<<15)|(OPCODE<<11)|(AA<<10)|(TC<<9)|(RD<<8)|(RA<<7)|(Z<<4)|RCODE, QDCOUNT, ANCOUNT, NSCOUNT, ARCOUNT)
    print("DNS query header =", query_header)
    query_header_hex = ''
    for byte in query_header:
        query_header_hex +='{:02X}'.format(byte) #formatting without spaces and in byte increments with leading zeros
    print("Query header in HEX = 0x", end='')
    print(query_header_hex)

    # Question section:
    domain_sections=host_name.split(".") # split domain into separate labels
    QNAME=b''
    QTYPE = 1
    QCLASS = 1
    for section in domain_sections:  
        section_length = len(section)
        section_ascii = section.encode('utf-8') # converts a section of the domain into bytes that represent each letter as a 1 byte value
        QNAME = QNAME + struct.pack('!B', section_length) + section_ascii
    QNAME += b'\x00'
    query_question = QNAME+struct.pack('!HH', QTYPE, QCLASS)
    query = query_header + QNAME + struct.pack('!HH', QTYPE, QCLASS)
    print("DNS query question section =", query_question)
    query_question_hex = ''
    for byte in query_question:
        query_question_hex +='{:02X}'.format(byte) #formatting without spaces and in byte increments with leading zeros
    print("Query question section in HEX = 0x", end='')
    print(query_question_hex)
    print("Complete DNS query =", query)
    query_in_hex=''
    for byte in query:
        query_in_hex +='{:02X}'.format(byte) #formatting without spaces and in byte increments with leading zeros
    print("Complete query in HEX = 0x", end='')
    print(query_in_hex)
    print("--------------------------------------------------------------------\n")
    
    # Send query:
    server_address_port = ('8.8.8.8', 53)
    buffer_size = 1024
    attempts = 3
    curr_attempt=1

    while True:
        try:
            print("Contacting DNS server..")
            my_socket = socket.socket(family=socket.AF_INET, type=socket.SOCK_DGRAM) # create the socket: 'AF_INET' for Internet and 'SOCK_DGRAM' for UDP socket
            print("Sending DNS query..")
            my_socket.sendto(query, server_address_port)
            my_socket.settimeout(5) # still need to add the "3 attempts" 
            msg_server = my_socket.recvfrom(buffer_size)
            break
        except socket.timeout:
            if attempts == curr_attempt:
                print("3 of 3 attempts failed.")
                sys.exit()
            print("Attempt", curr_attempt, "failed. Retrying...")
            curr_attempt+=1
    my_socket.close() # close socket
    print("DNS response received (attempt", curr_attempt, "of 3)")

    # Processing response:
    # Header processing:
    print("Processing DNS response...\n--------------------------------------------------------------------\n")
    returned_query = msg_server[0]
    ID = (returned_query[0]<<8) + returned_query[1]
    print("header.ID =", ID) 
    QR = (returned_query[2] & 0b10000000)>>7 # bitmask to get QR's true value
    print("header.QR =", QR)
    OPCODE = (returned_query[2] & 0b01111000)>>3 #bitmask to get OPCODE's true value
    print("header.OPCODE =", OPCODE)
    AA = (returned_query[2] & 0b00000100)>>2 #bitmask to get AA's true value
    print("header.AA =", AA)
    TC = (returned_query[2] & 0b00000010)>>1 #bitmask to get TC's true value
    print("header.TC =", TC)
    RD = returned_query[2] & 0b00000001 #bitmask to get RD's true value
    print("header.RD =", RD)
    RA = (returned_query[3] & 0b10000000) >> 7 #bitmask to get RD's true value
    print("header.RA =", RA)
    Z = (returned_query[3] & 0b01110000) >> 4  # bitmask to get Z's true value
    print("header.Z =", Z)
    RCODE = returned_query[3] & 0b00001111 
    print("header.RCODE =", RCODE)
    QDCOUNT = (returned_query[4]<<8) + returned_query[5]
    print("header.QDCOUNT =", QDCOUNT)
    ANCOUNT = (returned_query[6]<<8) + returned_query[7]
    print("header.ANCOUNT =", ANCOUNT)
    NSCOUNT = (returned_query[8]<<8) + returned_query[9]
    print("header.NSCOUNT =", NSCOUNT)
    ARCOUNT = (returned_query[10]<<8) + returned_query[11]
    print("header.ARCOUNT =", ARCOUNT)

    # Question section processing:
    print("--------------------------------------------------------------------\n")
    returned_query = returned_query[12:] # shift it to where question section begins
    counter = 0
    host_name = ''
    while returned_query[counter] != 0: # decoding the domain of the host by looping until reaching the null label (x00)
        label_length=returned_query[counter]
        counter+=1
        for byte in range(label_length):
            host_name += chr(returned_query[counter])
            counter+=1
        host_name +='.'
    host_name=host_name[:-1] # drop the last character since it is an uncessesary period character
    print("question.QNAME =", host_name)
    returned_query = returned_query[counter+1:] # +1 since we want to turnicate
    QTYPE = (returned_query[0]<<8) + returned_query[1]
    print("question.QTYPE =", QTYPE)
    QCLASS = (returned_query[2]<<8) + returned_query[3]
    print("question.QCLASS =", QCLASS)
    returned_query=returned_query[4:]
    print("--------------------------------------------------------------------\n")

    # RR section
    while len(returned_query) > 0:
        NAME = (returned_query[0]<<8) + returned_query[1]
        print("answer.NAME =", NAME)
        TYPE = (returned_query[2]<<8) + returned_query[3]
        print("answer.TYPE =", TYPE)
        CLASS = (returned_query[4]<<8) + returned_query[5]
        print("answer.CLASS =", CLASS)
        TTL = (returned_query[6]<<24) + (returned_query[7]<<16) + (returned_query[8]<<8) + returned_query[9]
        print("answer.TTL =", TTL)
        RDLENGTH = (returned_query[10]>>8) + returned_query[11]
        print("answer.RDLENGTH =", RDLENGTH)
        current_rr = returned_query[12:12+RDLENGTH]
        returned_query = returned_query[12+RDLENGTH:]
        if TYPE == 1:
            returned_ip = socket.inet_ntoa(current_rr)
            print("answer.RDATA =", returned_ip)
        if TYPE == 5:
            counter = 0
            host_name = ''
            while current_rr[counter] != 0xc0 and current_rr[counter] != 0:
                label_length=current_rr[counter]
                counter+=1
                for byte in range(label_length):
                    host_name += chr(current_rr[counter])
                    counter+=1
                host_name +='.'
            host_name=host_name[:-1] # drop the last character since it is an uncessesary period character
            print("answer.RDATA =", host_name)
        print("--------------------------------------------------------------------\n")
    
if __name__ == '__main__':
    main()