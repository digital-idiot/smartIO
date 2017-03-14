package net;

import java.awt.AWTException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Manages the different operations on the network.
 *
 * <p>
 *     This class creates a {@link NetworkState} which stores the information
 *     regarding the network. It provides methods for start and stop of the
 *     server. It also provides method for disconnecting a client.These
 *     methods modify the {@code NetworkState} accordingly.
 * </p>
 * <p>
 *     This class provides back-end of the network operations achieved by the
 *     GUI of SmartIO.
 * </p>
 *
 * @author Sudipto Bhattacharjee, Sayantan Majumdar, Abhisek Maiti
 * @see NetworkState
 */
public class NetworkManager {

    private Server mServer;
    private BroadcastThread mBroadcastThread;
    private Thread mThread;
    private NetworkState mState;

    /**
     * Constructor.
     * <p>
     *     This instantiates {@code NetworkState} and {@code BroadcastThread}.
     *     The {@code BroadcastThread} is not started in the constructor.
     *     It can be started using {@link #startServer()}.
     * </p>
     *
     * @param serverInfo a {@link ServerInfo} object.
     * @see ServerInfo
     * @see BroadcastThread
     * @see #startServer()
     */
    public NetworkManager(ServerInfo serverInfo) {
        mState = new NetworkState();
        mBroadcastThread = new BroadcastThread(serverInfo);
    }

    /**
     * @return The {@link NetworkState} of this network
     */
    public NetworkState getNetworkState() { return mState; }

    /**
     * Method to start server.
     *
     * <p>
     *     This is achieved by executing two primary operations :
     *     <ul>
     *         <li>Starting the {@code BroadcastThread}.</li>
     *         <li>Calling {@link Server#listen()}.</li>
     *     </ul>
     * </p>
     *
     * @throws IOException
     * @throws InterruptedException
     * @throws AWTException
     * @see BroadcastThread
     * @see Server#listen()
     */
    void startServer() throws IOException, InterruptedException, AWTException {
        mThread = new Thread(mBroadcastThread);
        mServer = new Server(mState, mBroadcastThread);
        mThread.start();
        System.out.println("Broadcast started ...");
        System.out.println("Server started ...");
        mServer.listen();
    }

    /**
     * Method to stop server.
     *
     * <p>
     *     This is achieved by executing three primary operations :
     *     <ul>
     *         <li>
     *             Stopping {@code BroadcastThread} by calling
     *             {@link BroadcastThread#stopBroadcast()}.
     *         </li>
     *         <li>Calling {@link Server#setStopFlag()}.</li>
     *         <li>
     *             Disconnecting all clients. This is done by calling
     *             {@link #disconnect(InetAddress)} for every connection
     *             stored in the network state.
     *         </li>
     *     </ul>
     * </p>
     *
     * @throws IOException
     * @throws InterruptedException
     * @see BroadcastThread
     * @see BroadcastThread#stopBroadcast()
     * @see Server#setStopFlag()
     * @see #disconnect(InetAddress)
     */

    @SuppressWarnings("unchecked")
    public void stopServer() throws IOException, InterruptedException {
        mBroadcastThread.stopBroadcast();
        Thread.sleep(mBroadcastThread.getTimeout());
        mThread = null;
        System.out.println("Broadcast stopped ...");

        if(mServer != null) {
            mServer.setStopFlag();
            Thread.sleep(mServer.getTimeout());
            mServer.close();
            mServer = null;

            HashMap<InetAddress, Socket> aMap = mState.getsAddressMap();
            Iterator it = aMap.entrySet().iterator();
            while(it.hasNext()) {
                Map.Entry<InetAddress, Socket> entry = (Map.Entry<InetAddress, Socket>) it.next();
                disconnect(entry.getKey());
            }

            System.out.println("Server stopped ...");
        }
    }

    /**
     * Method to disconnect a client. It gets the {@link ServerThread} of
     * the connection with the client to be disconnected from the network
     * state by calling {@link NetworkState#getServerThread(InetAddress)}.
     *
     * @param address {@link InetAddress} of the client to be disconnected
     * @throws IOException
     * @throws InterruptedException
     * @see ServerThread
     * @see NetworkState#getServerThread(InetAddress)
     */
    public void disconnect(InetAddress address) throws IOException, InterruptedException {
        ServerThread st = mState.getServerThread(address);
        try {
            st.setStopFlag();
            Thread.sleep(st.getTimeout());
            System.out.println(address.getHostAddress() + " disconnected");
        } catch (NullPointerException e) {System.out.println("No connected devices. Skipping ...");}
    }
}