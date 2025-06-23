const net = require('net');

// Send command to server and return the response
function sendCommandToServer(command) {
// Return a new Promise to handle asynchronous operations
return new Promise((resolve, reject) => {
  // Parse the server port from environment variables
  const SERVER_PORT = parseInt(process.env.SERVER_PORT);
  // Get the server IP address from environment variables
  const SERVER_IP = process.env.SERVER_IP;
    // Create a new TCP socket
    const client = new net.Socket();
    // Connect to the C++ server
    client.connect(SERVER_PORT, SERVER_IP, () => {
      // Send the command followed by \n
      client.write(command + '\n');
    });

    // When the response is received from the server, resolve the promise with the response and close the connection
    client.on('data', (response) => {
      resolve(response.toString().trim());
      client.end();
    });

    // If there is a connection or sending error, reject the promise with the error and close the connection
    client.on('error', (err) => {
      client.destroy();
      reject(err);
    });
  });
}

module.exports = { sendCommandToServer };