package server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.io.PrintWriter;
import java.io.IOException;
import java.awt.AWTException;

import device.KeyboardController;
import device.MouseController;
import net.ClientInfo;

/**
 * @author Sudipto Bhattacharjee
 * @author Sayantan Majumdar
 */

class Server {

    private boolean mStopFlag;
    private ServerSocket mServerSocket;
    private MouseController mMouseController;
    private KeyboardController mKeyboardController;
    private NetworkState mState;
    private String mPairingKey;

    Server(NetworkState state) throws IOException, AWTException {
        this.mState = state;
        mMouseController = new MouseController();
        mKeyboardController = new KeyboardController();
        mServerSocket = new ServerSocket(NetworkManager.TCP_PORT);
        mServerSocket.setSoTimeout(200);
        mStopFlag = false;
    }
    
    private boolean isValidClient(Socket socket) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String pairingKey = in.readLine();
        System.out.println("Received pairing key: " + pairingKey);
        return mPairingKey.equals(pairingKey);
    }

    void setStopFlag() { mStopFlag = true; }

    int getTimeout() throws IOException { return mServerSocket.getSoTimeout(); }

    void listen() throws IOException,InterruptedException {
        while(!mStopFlag) {
            try {
                Socket clientSocket = mServerSocket.accept();
                System.out.println(clientSocket.getInetAddress().getHostAddress() +
                        " wants to connect.");

                String[] items = new ClientInfo().getPKIV();
                String clientPubKey = items[0];
                mPairingKey = items[1];
                System.out.println("Type the following pairing key to connect your phone: " + mPairingKey);
                if (isValidClient(clientSocket)) {
                    new PrintWriter(clientSocket.getOutputStream(), true).println(1);
                    System.out.println("Connected to " + clientSocket.getInetAddress().getHostAddress());
                    System.out.println("Client public key: " + clientPubKey);
                    ServerThread st = new ServerThread(mState, clientSocket, mMouseController, mKeyboardController);
                    Thread t = new Thread(st);
                    mState.add(clientSocket, st);
                    t.start();
                } else {
                    System.out.println("Incorrect Pairing Key!");
                    new PrintWriter(clientSocket.getOutputStream(), true).println(0);
                    clientSocket.close();
                }
            } catch (SocketTimeoutException | SocketException e) {}
        }
    }

    void close() throws IOException {
        mServerSocket.close();
    }
}