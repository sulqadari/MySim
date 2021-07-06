package mysim.javacard;

import javacard.framework.OwnerPIN;
import javacard.framework.PINException;

public class PINController extends OwnerPIN
{
	protected final static byte pinLength		= (byte)0x08;
	protected final static byte pinLimit		= (byte)0x09;
	private short pinLimitCounter               = (short)0x63C9;

	final static short PIN_IS_BLOCKED           = (short)0x6983;
    final static short PIN_NOT_VERIFIED         = (short)0x6982;

    public PINController() throws PINException
    {
    	super((byte)0x09, (byte)0x08);
    }

    /**
     * Assigns <code>pinLimitCounter</code> with the initial value.<br>
     * To prevent redundant EEPROM rewriting operations this method checks for the correct PIN input from the first try.
     */
    protected void resetPinCounter()
    {
        if (pinLimitCounter == (short)0x63C9)
        {
            return;
        }

        pinLimitCounter = (short)0x63C9;
    }

    protected void decrementLimitCounter()
    {
        pinLimitCounter--;
    }

    protected short getLimitCounter()
    {
        return pinLimitCounter;
    }
}