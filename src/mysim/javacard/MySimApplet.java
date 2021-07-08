package mysim.javacard;

import javacard.framework.APDU;
import javacard.framework.ISOException;
import javacard.framework.Util;

/**
 * Main class which implements functionality of the <code>javacard.framework.MySimApplet</code>.<br>
 */
public class MySimApplet extends javacard.framework.Applet
{
    private AppletController controller  = null;     //Contains main functionality
 
    /**
     * Main constructor.<br>
     * This constructor initializes the instance of the <code>AppletConstructor</code> class which
     * encapsulates main functionality and security features of the this MySimApplet.
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
    	byte aidLen		= bArray[(short)0];					//AID length
    	byte tryLim		= bArray[(short)(aidLen + 3)];		//Try limit value
    	
    	short pinOff	= (short)(bArray[0] + 4);		//PIN array starts right after try-limit byte.
    	byte pinLen		= (byte)bArray[bArray[0] + 2];	//The len_tag of the incoming data encapsulates not only PIN array, but try counter too...
    	pinLen			-=(byte)1;						//...so if the length of the PIN is 8, the len-tag contains 9 and 1 byte must be subtracted
    	
    	byte[] pinArr	= new byte[8];
    	Util.arrayCopy(bArray, pinOff, pinArr, (short)0, pinLen);
    	
    	new MySimApplet(pinArr, (short)0, pinLen, tryLim).register();
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
