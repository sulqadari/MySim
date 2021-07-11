package mysim.javacard;

import javacard.framework.APDU;
import javacard.framework.ISOException;
import javacard.framework.Util;

/**
 * Main class which implements functionality of the <code>javacard.framework.Applet</code>.<br>
 */
public class MySimApplet extends javacard.framework.Applet
{
    private AppletController controller  = null;     //Contains main functionality
 
    /**
     * Main constructor.<br>
     * This constructor initializes the instance of the <code>AppletConstructor</code> class which
     * encapsulates main functionality and security features of the MySimApplet.
     *
     */
    private MySimApplet(byte[] pinArr, short pinOff, byte pinLen, byte tryLimit)
    {
        controller  = new AppletController(pinArr, pinOff, pinLen, tryLimit);
        
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
    	byte aidOff		= bArray[bOffset + 1];
		short pinOff	= (short)(bArray[bOffset] + (byte)4);
		byte pinLen		= (byte)8;
		byte tryLimCtr	= (byte)(bArray[bOffset] + (byte)3);
    	
    	new MySimApplet(bArray, pinOff, pinLen, tryLimCtr).register();
    }

    public void process(APDU apdu) throws ISOException
    {
        if (selectingApplet())
        {
            controller.resetPin();
            return;
        }
        controller.process(apdu);
    }

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
