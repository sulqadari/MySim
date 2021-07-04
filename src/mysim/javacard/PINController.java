package mysim.javacard;

import javacard.framework.OwnerPIN;
import javacard.framework.PINException;

public class PINController extends OwnerPIN
{
    final static short PIN_IS_BLOCKED   = (short)0x6983;
    final static short PIN_NOT_VERIFIED = (short)0x6982;
    private short pinLimitCounter       = (short)0x63C0;

    public PINController(byte tryLimit, byte maxPINSize) throws PINException
    {
        super(tryLimit, maxPINSize);
    }

    /**
     * Assigns <code>pinLimitCounter</code> with <code>OwnerPIN.tryLimit</code> initial value.<br>
     * To prevent redundant EEPROM writing operations, this method checks for correct PIN input<br>
     * from the first attempt.
     * @param value
     */
    public void setPinCounter(byte value)
    {
        if ((short)(pinLimitCounter & (short)0x000F) == (short)value)
        {
            return;
        }

        pinLimitCounter &= (short)0xFFF0;
        pinLimitCounter += value;
    }

    public void decrementLimitCounter()
    {
        pinLimitCounter--;
    }

    public short getLimitCounter()
    {
        return pinLimitCounter;
    }
}
