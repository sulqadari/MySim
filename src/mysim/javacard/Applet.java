package mysim.javacard;

import javacard.framework.APDU;
import javacard.framework.ISOException;
import javacard.framework.JCSystem;

/**
 * Main class which implements functionality of the <code>javacard.framework.Applet</code>.<br>
 */
public class Applet extends javacard.framework.Applet
{
    private static AppletController controller  = null;     //Contains main functionality

    /**
     * Main constructor.<br>
     * This constructor initializes the instance of the <code>AppletConstructor</code> class
     * encapsulates main functionality and security features of the this Applet.
     * @param pinTryLimit   the upper bound of the PIN attempts limit. Up to 9 tries.
     * @param pinSize       the maximum size of the PIN. Can't exceed 8 bytes
     *
     */
    private Applet(byte pinTryLimit, byte pinSize)
    {
        controller  = new AppletController(pinTryLimit, pinSize);
    }

    /**
     * This method installs and registers a new instance of applet. After successful applet registration
     * the <code>AppletController.updatePin()</code> method is invoked to set initial PIN value.<br>
     * @param bArray    Must contain the following subsequent of data:<br>
     *                  <code>[tag_C9, ta_C9Len, pinLim, pinLen, pinByte1, ... pinByteN]</code>
     * @param bOffset   RFU
     * @param bLength   RFU
     * @throws ISOException
     */
    public static void install(byte[] bArray, short bOffset, byte bLength) throws ISOException
    {
        byte pinTryLimit    = bArray[2];
        byte pinSize        = bArray[3];
        short pinOffset     = (short)4;

        new Applet(pinTryLimit, pinSize).register();

        JCSystem.beginTransaction();
        controller.updatePin(bArray, pinOffset, pinSize);
        JCSystem.commitTransaction();
    }

    @Override
    public void process(APDU apdu) throws ISOException
    {
        if (selectingApplet())
        {
            controller.resetPin();
            return;
        }
        controller.process(apdu);
    }

    @Override
    public boolean select()
    {
       controller.resetPin();
        return true;
    }
}
