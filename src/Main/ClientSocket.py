import socket
import sys

def main():
    # Create an IPv4 TCP socket
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

    # Get server IP and port from command line arguments
    ip = sys.argv[1]
    port = int(sys.argv[2])

    # Connect to the server
    s.connect((ip, port))

    # Loop forever
    while True:
        # Read input
        msg = input()      
        # Send message as bytes         
        s.send((msg + "\n").encode())     
        # Receive server response
        response = s.recv(4096)     
        # Decode and print the response
        print(response.decode())    

if __name__ == "__main__":
    main()
