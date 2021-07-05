package mysim.javacard;

import javacard.framework.OwnerPIN;
import javacard.framework.PINException;

public class PINController extends OwnerPIN
{
    final static byte INS_VERIFY_PIN            = (byte)0x20;
    final static byte INS_UPDATE_PIN            = (byte)0x22;


    final static short PIN_IS_BLOCKED           = (short)0x6983;
    final static short PIN_NOT_VERIFIED         = (short)0x6982;
    private short pinLimitCounter               = (short)0x63C0;

    public PINController(byte tryLimit, byte maxPINSize) throws PINException
    {
        super(tryLimit, maxPINSize);
    }

    /**
     * Assigns <code>pinLimitCounter</code> with <code>OwnerPIN.tryLimit</code> initial value.<br>
     * To prevent redundant EEPROM writing operations this method checks for correct PIN input
     * at the first try.
     * @param initTriesCounter     initial tries counter value which retreived from the
     *                             <code>OwnerPIN.getTriesRemaining()</code> method invocation.<br>
     *                             When the correct PIN is entered the <code>OwnerPIN.check()</code> method<br>
     *                             sets the tries counter to its maximum value.
     */
    protected void setPinCounter(byte initTriesCounter)
    {
        if ((short)(pinLimitCounter & (short)0x000F) == (short)initTriesCounter)
        {
            return;
        }

        pinLimitCounter &= (short)0xFFF0;
        pinLimitCounter += initTriesCounter;
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
