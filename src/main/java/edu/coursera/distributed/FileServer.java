package edu.coursera.distributed;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * A basic and very limited implementation of a file server that responds to GET
 * requests from HTTP clients.
 */
public final class FileServer {
    /**
     * Main entrypoint for the basic file server.
     *
     * @param serverSocket Provided socket to accept connections on.
     * @param fs A proxy filesystem to serve files from. See the PCDPFilesystem
     *           class for more detailed documentation of its usage.
     * @param ncores The number of cores that are available to your
     *               multi-threaded file server. Using this argument is entirely
     *               optional. You are free to use this information to change
     *               how you create your threads, or ignore it.
     * @throws IOException If an I/O error is detected on the server. This
     *                     should be a fatal error, your file server
     *                     implementation is not expected to ever throw
     *                     IOExceptions during normal operation.
     */
    public void run(final ServerSocket serverSocket, final PCDPFilesystem fs,
            final int ncores) throws IOException {
        /*
         * Enter a spin loop for handling client requests to the provided
         * ServerSocket object.
         */
        while (true) {
            // TODO 1) Use socket.accept to get a Socket object
            Socket socket = serverSocket.accept();

            /*
             * TODO 2) Using Socket.getInputStream(), parse the received HTTP
             * packet. In particular, we are interested in confirming this
             * message is a GET and parsing out the path to the file we are
             * GETing. Recall that for GET HTTP packets, the first line of the
             * received packet will look something like:
             *
             *     GET /path/to/file HTTP/1.1
             */

            Thread thread = new Thread(() -> {
                try {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                         PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())))) {
                        String line = reader.readLine();
                        PCDPPath path = new PCDPPath(line.split(" ")[1]);
                        String fileContent = fs.readFile(path);

                        if (fileContent != null && fileContent.length() > 0) {
                            writer.write("HTTP/1.0 200 OK\r\n");
                            writer.write("Server: FileServer\r\n");
                            writer.write("\r\n");
                            writer.write(fileContent + "\r\n");
                        } else {
                            writer.write("HTTP/1.0 404 Not Found\r\n");
                            writer.write("Server: FileServer\r\n");
                            writer.write("\r\n");
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            thread.start();
        }
    }
}
