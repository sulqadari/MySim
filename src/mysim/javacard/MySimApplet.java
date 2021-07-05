package mysim.javacard;

import javacard.framework.APDU;
import javacard.framework.ISOException;
import javacard.framework.JCSystem;

/**
 * Main class which implements functionality of the <code>javacard.framework.MySimApplet</code>.<br>
 */
public class MySimApplet extends javacard.framework.Applet
{
    private static AppletController controller  = null;     //Contains main functionality

    /**
     * Main constructor.<br>
     * This constructor initializes the instance of the <code>AppletConstructor</code> class
     * encapsulates main functionality and security features of the this MySimApplet.
     * @param pinTryLimit   the upper bound of the PIN attempts limit. Up to 9 tries.
     * @param pinSize       the maximum size of the PIN. Can't exceed 8 bytes
     *
     */
    private MySimApplet(byte pinTryLimit, byte pinSize)
    {
        if ((pinTryLimit > (byte)9) || (pinSize != (byte)8))
        {
            PINException.throwIt(PINException.ILLEGAL_VALUE);
        }
        controller  = new AppletController(pinTryLimit, pinSize);
    }

    /**
     * This method installs and registers a new instance of applet. After successful applet registration
     * the <code>AppletController.updatePin()</code> method is invoked to set initial PIN value.<br>
     * @param bArray
     * @param bOffset
     * @param bLength
     * @throws ISOException
     */
    public static void install(byte[] bArray, short bOffset, byte bLength) throws ISOException
    {
        byte pinTryLimit    = bArray[0];
        byte pinLength      = bArray[1];
        short pinOffset     = bArray[2];

        new MySimApplet(pinTryLimit, pinLength).register();

        changeAppletLifePhase();                            //Change applet life phase to INIT
        controller.updatePin(bArray, pinOffset, pinLength);
        changeAppletLifePhase();                            //Change applet life phase to USE
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

    public void deselect()
    {
        controller.resetPin();
    }
}
