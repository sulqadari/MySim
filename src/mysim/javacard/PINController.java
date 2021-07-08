package mysim.javacard;

import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.Util;

/**
 * 
 */
public class PINController
{
	public static final short SW_PIN_ILLEGAL_VALUE	= (short)0x6A85;
	public static final short SW_PIN_IS_BLOCKED		= (short)0x6983;
	public static final short SW_PIN_NOT_VERIFIED	= (short)0x6982;
    
	private byte[] pinValue				= new byte[8];
	private byte pinLimit				= (byte)0;
	private byte pinLength				= (byte)8;
	private short pinCounter			= (short)0x63C0;
	private boolean isValidated			= false;

    /**
     * 
     * @param tryLimit
     * @param maxPINSize
     */
    public PINController(byte tryLimit, byte maxPINSize)
    {
    	if ((tryLimit > (byte)0x09) || (maxPINSize != (byte)0x08))
    		ISOException.throwIt(ISO7816.SW_UNKNOWN);
    	
    	pinLimit	= tryLimit;
    	pinLength	= maxPINSize;
    	pinCounter	= (short)((pinCounter & (short)0xFFF0) | tryLimit);
    }

    /**
     * 
     * @param pinArr
     * @param pinOff
     * @param pinLen
     * @return
     */
    protected boolean check(byte[] pinArr, short pinOff, byte pinLen)
    {
    	if (pinCounter <= (short)0)
    		return isValidated = false;
    	
    	if ((pinArr == null) || (pinLen != pinLength) || (pinOff < (short)0))
    	{
    		pinCounter--;
	    	return isValidated = false;
    	}

    	byte result = Util.arrayCompare(pinArr, pinOff, pinValue, (short)0, pinLen);
    	
    	if (result == 0)
    	{
    		resetPinCounter();
    		 isValidated = true;
    		return isValidated;
    	}
    	else
    	{
    		pinCounter--;
    		return isValidated = false;
    	}
    }
    
    protected void update(byte[] pinArr, short pinOff, byte pinLen)
    {
    	if ((pinArr == null) || (pinLen != (byte)8))
    		ISOException.throwIt(SW_PIN_ILLEGAL_VALUE);
    	
    	Util.arrayCopy(pinArr, pinOff, pinValue, (short)0, pinLen);
    }
    
    /**
     * 
     * @return
     */
    protected short getTriesRemaining()
    {
        return pinCounter;
    }
    
    protected void dectementPinCounter()
    {
    	pinCounter--;
    }
    
    /**
     * 
     * @return
     */
    protected boolean isValidated()
    {
    	return isValidated;
    }
    
    /**
     * 
     */
    protected void reset()
    {
    	if (isValidated)
    	{
    		isValidated = false;
    		resetPinCounter();
    		return;
    	}
    	return;
    }
    
    /**
     * Assigns <code>pinLimitCounter</code> with the initial value.<br>
     * To avoid redundant EEPROM rewriting operations this method checks for the correct PIN input from the first try.
     */
    private void resetPinCounter()
    {
        if ((pinCounter & (short)0x000F) == (short)pinLimit)
        	return;

        pinCounter = (short)((pinCounter & (short)0xFFF0) | ((short)pinLimit & (short)0x000F));
    }
}