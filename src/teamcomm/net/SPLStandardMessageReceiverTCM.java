package teamcomm.net;

import common.Log;
import common.net.SPLStandardMessagePackage;
import common.net.SPLStandardMessageReceiver;
import data.SPLStandardMessage;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import javax.swing.JOptionPane;
import teamcomm.PluginLoader;
import teamcomm.data.AdvancedMessage;
import teamcomm.data.GameState;
import teamcomm.net.logging.LogReplayer;

/**
 * Singleton class for the thread which handles messages from the robots. It
 * spawns one thread for listening on each team port up to team number 100 and
 * processes the messages received by these threads.
 *
 * @author Felix Thielke
 */
public class SPLStandardMessageReceiverTCM extends SPLStandardMessageReceiver {

    private static SPLStandardMessageReceiverTCM instance;

    public SPLStandardMessageReceiverTCM(final boolean multicast) throws IOException {
        super(multicast, null);
    }

    /**
     * Creates the only instance of the SPLStandardMessageReceiver.
     * @param multicast Should it also listen to multicast packets? This also means
     *                  that ip adresses are computed based on the player number.
     *
     * @return instance
     */
    public static SPLStandardMessageReceiverTCM createInstance(final boolean multicast) {
        try {
            instance = new SPLStandardMessageReceiverTCM(multicast);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null,
                    "Error while setting up packet listeners: " + ex.getMessage(),
                    "IOException",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }
        return instance;
    }

    /**
     * Returns the only instance of the SPLStandardMessageReceiver.
     *
     * @return instance
     */
    public static SPLStandardMessageReceiverTCM getInstance() {
        return instance;
    }

    @Override
    protected boolean processPackets() {
        return !LogReplayer.getInstance().isReplaying();
    }

    @Override
    protected void handleMessage(final SPLStandardMessagePackage p) {
        final SPLStandardMessage message;
        final Class<? extends SPLStandardMessage> c = PluginLoader.getInstance().getMessageClass(p.team);

        try {
            message = c.getDeclaredConstructor().newInstance();
            message.fromByteArray(ByteBuffer.wrap(p.message));
            if (message.teamNumValid && message.teamNum != p.team) {
                message.teamNumValid = false;
                message.valid = false;
            }

            SPLStandardMessage m = message;
            if (message instanceof AdvancedMessage) {
                if (message.valid) {
                    try {
                        ((AdvancedMessage) message).init();
                    } catch (final Throwable e) {
                        m = SPLStandardMessage.createFrom(message);
                        Log.error(e.getClass().getSimpleName() + " was thrown while initializing custom message class " + c.getSimpleName() + ": " + e.getMessage());
                    }
                } else {
                    m = SPLStandardMessage.createFrom(message);
                }
            }

            GameState.getInstance().receiveMessage(p.host, p.team, m);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
            Log.error("a problem occured while instantiating custom message class " + c.getSimpleName() + ": " + ex.getMessage());
        }
    }

}
