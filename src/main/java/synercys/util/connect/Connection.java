package synercys.util.connect;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.io.File;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Connection {
    Socket socket;
    //BufferedReader input;
    //DataOutputStream output;
    PrintWriter output;                 // This is for sending strings.
    BufferedOutputStream outputStream;  // This is for transmitting binary file raw data.
    //BufferedInputStream inputStream;    // This is for receiving binary file raw data.
    InputStream inputStream;

    Lock outputLock = new ReentrantLock();
    Lock readLock = new ReentrantLock();

    public Connection() {
        // Empty constructor
    }

    public Connection(Socket inSocket) throws IOException {
        socket = inSocket;
        socket.setSoTimeout(500);  // 500 mini sec timeout
        //input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        //output = new DataOutputStream(socket.getOutputStream());
        output = new PrintWriter(new BufferedWriter(
                new OutputStreamWriter(socket.getOutputStream())),
                true);
        outputStream = new BufferedOutputStream(socket.getOutputStream());
        //inputStream = new BufferedInputStream(socket.getInputStream());
        inputStream = socket.getInputStream();
    }


    public void setTimeout(int inTimeout) {
        try {
            socket.setSoTimeout(inTimeout);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public String getClientIp() {
        return socket.getInetAddress().toString();
    }

    public String read() {
        byte[] readBuffer = new byte[1024];
        //char[] readBuffer = new char[1024];
        int receivedByteCount = 0;
        String output;

        readLock.lock();
        {
            try {
                receivedByteCount = inputStream.read(readBuffer, 0, 1024);
                //receivedByteCount = input.read(readBuffer);
                if (receivedByteCount < 0) {
                    output = null;
                } else {
                    output = new String(readBuffer, 0, receivedByteCount);
                }
            } catch (SocketTimeoutException e) {
                // Read timeout.
                //Log.errPutline("Read time out.");
                output = null;
            } catch (IOException e) {
                if (!socket.isClosed())
                    e.printStackTrace();
                output = null;
            }
        }
        readLock.unlock();

        return output;
    }

    public String putStringAndRead(String inString) {
        String returnData;

        putString(inString);
        returnData = read();//input.readLine();

        return returnData;

    }

    public void putString(String inString) {
        outputLock.lock();
        {
            output.print(inString);
            output.flush();
        }
        outputLock.unlock();
    }


    public Boolean sendFile(File inFile) {
        byte[] fileBytes;// = new byte[0];
        try {
            fileBytes = Files.readAllBytes(inFile.toPath());
        } catch (IOException e) {
            //e.printStackTrace();
            //Log.errPutline("Failed to read assigned file.");
            return false;
        }

        //Log.sysPutLine("Transmitting a file: " + String.valueOf(fileBytes.length));

        Boolean sendResult = true;
        outputLock.lock();
        {
            try {
                outputStream.write(fileBytes, 0, fileBytes.length);
                outputStream.flush();   // Necessary when the file is small and is queued in the output buffer.
                //Log.sysPutLine("A file transmission is done.");
            } catch (IOException e) {
                sendResult = false;
                //e.printStackTrace();
            }
        }
        outputLock.unlock();

        return sendResult;
    }

    public Boolean receiveFile(File outFile, int inLength) {
        byte[] readBuffer = new byte[inLength];
        int totalByteCount = 0;
        Boolean resultOk = true;

        readLock.lock();
        {
            try {
                FileOutputStream outStream = new FileOutputStream(outFile);
                while (totalByteCount<inLength) {
                    int receivedByteCount = inputStream.read(readBuffer, 0, inLength-totalByteCount);
                    if (receivedByteCount < 0) {
                        resultOk = false;
                        break;
                    }
                    totalByteCount += receivedByteCount;
                    outStream.write(readBuffer, 0, receivedByteCount);
                }
                outStream.close();
                //Log.sysPutLine(String.valueOf(totalByteCount) + " bytes received.");
                //Log.sysPutLine(String.valueOf(inputStream.available()) + " in the buffer.");
            } catch (SocketTimeoutException e) {
                //Log.errPutline("Time out while receiving incoming file packets.");
                //Log.errPutline("%d bytes in the buffer.", inputStream.available());
                resultOk = false;
            }
            catch (IOException e) {
                if (!socket.isClosed())
                    e.printStackTrace();
                resultOk = false;
            }
        }
        readLock.unlock();

        return resultOk;
    }

    public Boolean isClosed() {
        return socket.isClosed();
    }

    public void close() {
        if (socket.isClosed()) {
            return;
        }

        outputLock.lock();  // It's useful when there is another thread sending out the quit packet.
        {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        outputLock.unlock();
    }
}