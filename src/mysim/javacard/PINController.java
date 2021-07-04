package mysim.javacard;

import javacard.framework.*;

public class PINController implements PIN
{
    final static short PIN_IS_BLOCKED    = (short)0x6983;
    final static short PIN_NOT_VERIFIED  = (short)0x6982;

    private byte[] pin;
    private byte maxPINSize;
    private byte tryLimit;
    private byte tryLimitUpperBound;
    private boolean isValid;

    public PINController(byte tryLimit, byte maxPINSize) throws PINException
    {
        pin                 = new byte[8];
        this.tryLimit       = tryLimit;
        tryLimitUpperBound  = tryLimit;
        this.maxPINSize     = maxPINSize;
        isValid             = false;
    }

    @Override
    public boolean check(byte[] bytes, short offset, byte length) throws ISOException
    {
        if (tryLimit == 0)
        {
            ISOException.throwIt(PIN_IS_BLOCKED);
        }

        if ((bytes == null) || offset >= bytes.length)
        {
            isValid = false;
            tryLimit--;
            ISOException.throwIt(ISO7816.SW_UNKNOWN);
        }

        if ((offset + length) >= bytes.length)
        {
            isValid = false;
            tryLimit--;
            ISOException.throwIt(ISO7816.SW_UNKNOWN);
        }

        if ((offset < 0) || (length < 0))
        {
            isValid = false;
            tryLimit--;
            ISOException.throwIt(ISO7816.SW_UNKNOWN);
        }

        byte isEqual = (byte) -1;
        JCSystem.beginTransaction();
        isEqual = Util.arrayCompare(bytes, offset, pin, (short)0x00, length);
        JCSystem.commitTransaction();

        if (isEqual == 0)
        {
            isValid = true;
            tryLimit = tryLimitUpperBound;
            return true;
        }
        else
        {
            isValid = false;
            tryLimit--;
            return false;
        }
    }

    public void update(byte[] pin, short offset, byte length)
    {
        if (!isValid)
        {
            ISOException.throwIt(PIN_NOT_VERIFIED);
        }

        maxPINSize = length;
        Util.arrayCopy(pin, offset, this.pin, (short)0x00, maxPINSize);
    }

    @Override
    public byte getTriesRemaining() {
        return 0;
    }

    @Override
    public boolean isValidated() {
        return false;
    }

    @Override
    public void reset() {

    }

    protected void setValidatedFlag(boolean flag)
    {
        isValid = flag;
    }
}
