package mysim.javacard;

import javacard.framework.OwnerPIN;
import javacard.framework.PINException;

public class PINController extends OwnerPIN
{
    final static short PIN_IS_BLOCKED   = (short)0x6983;
    final static short PIN_NOT_VERIFIED = (short)0x6982;
    private short PIN_LIMIT_COUNTER     = (short)0x63C0;

    public PINController(byte tryLimit, byte maxPINSize) throws PINException
    {
        super(tryLimit, maxPINSize);
    }

    public void setPinCounter(byte value)
    {
        PIN_LIMIT_COUNTER &= (byte)0xF0;
        PIN_LIMIT_COUNTER += value;
    }

    public void decrementLimitCounter()
    {
        PIN_LIMIT_COUNTER--;
    }

    public short getLimitCounter()
    {
        return PIN_LIMIT_COUNTER;
    }
}
