package mysim.javacard;

import javacard.framework.*;

public class AppletController
{
    private static PINController pin    = null;
    private final byte VERIFY_PIN       = (byte) 0x10;

    RSAController rsa                   = null;
    AESController aes                   = null;

    protected AppletController(byte tryLimit, byte maxPINSize)
    {
        pin    = new PINController(tryLimit, maxPINSize);
        rsa    = new RSAController();
        aes    = new AESController();
    }

    protected void process(APDU apdu, byte[] buffer)
    {
        switch (buffer[ISO7816.OFFSET_INS])
        {
            case VERIFY_PIN:
            {

            }
            default: ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
        }
    }


    protected static void updatePin(byte[] bArray)
    {
        short pinOffset = (short) 0x01;
        byte pinLen     = bArray[0];

        pin.update(bArray, pinOffset, pinLen);
    }

    protected boolean resetPin()
    {
        pin.reset();
        return true;
    }

    protected void checkPin(byte[] buffer) throws ISOException, NullPointerException, ArrayIndexOutOfBoundsException
    {
        if (buffer == null)
        {
            pin.setValidatedFlag(false);
            ISOException.throwIt(ISO7816.SW_WRONG_DATA);
        }
        short pinOffset = (short)0x01;
        byte pinLength  = buffer[0];
        boolean isSet   = pin.check(buffer, pinOffset, pinLength);

        if (!isSet)
        {
            ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
        }
    }
}
