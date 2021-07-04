package mysim.javacard;

import javacard.framework.*;

public class AppletController
{
    static final byte CLA_BYTE          = (byte)0x80;
    static final byte INS_VERIFY_PIN    = (byte)0x20;
    static final byte INS_UPDATE_PIN    = (byte)0x22;

    private static PINController pin    = null;
    RSAController rsa                   = null;
    AESController aes                   = null;

    protected AppletController(byte tryLimit, byte maxPINSize) throws PINException
    {
        if ((tryLimit > 9) || (maxPINSize > 8))
        {
            PINException.throwIt(PINException.ILLEGAL_VALUE);
        }

        pin    = new PINController(tryLimit, maxPINSize);
        rsa    = new RSAController();
        aes    = new AESController();
    }

    protected void process(APDU apdu)
    {
        byte[] buffer   = apdu.getBuffer();
        byte pinLength  = (byte)apdu.setIncomingAndReceive();

        if (buffer[ISO7816.OFFSET_CLA] != CLA_BYTE)
        {
            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
        }

        switch (buffer[ISO7816.OFFSET_INS])
        {
            case INS_VERIFY_PIN:
            {
                checkPin(buffer, ISO7816.OFFSET_CDATA, pinLength);
            }break;
            case INS_UPDATE_PIN:
            {
                updatePin(buffer, ISO7816.OFFSET_CDATA, pinLength);
            }break;
            default: ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
        }
    }

    protected static void updatePin(byte[] pinArray, short offset, byte length)
    {
        if (pinArray == null)
        {
            ISOException.throwIt(ISO7816.SW_WRONG_DATA);
        }

        if (length > 8)
        {
            PINException.throwIt(PINException.ILLEGAL_VALUE);
        }

        JCSystem.beginTransaction();
        pin.update(pinArray, offset, length);
        JCSystem.commitTransaction();
    }

    protected boolean resetPin()
    {
        pin.reset();
        return true;
    }

    protected void checkPin(byte[] buffer, byte pinOffset, byte pinLength) throws ISOException, NullPointerException, ArrayIndexOutOfBoundsException
    {
        byte attemptsLeft = pin.getTriesRemaining();

        //If attempts exceed limit
        if (attemptsLeft <= 0)
        {
            PINException.throwIt(PINController.PIN_IS_BLOCKED);
        }

        if (buffer == null)
        {
            ISOException.throwIt(ISO7816.SW_WRONG_DATA);
        }

        boolean isSet = pin.check(buffer, pinOffset, pinLength);    //verify PIN

        if (isSet)                                                  //if PIN successfully verified...
        {
            attemptsLeft = pin.getTriesRemaining();
            pin.setPinCounter(attemptsLeft);                        //...reset SW_63Cx to initial value
        }
        else                                                        //Else...
        {
            pin.decrementLimitCounter();                            //...decrement SW_63Cx...
            short triesCounter = pin.getLimitCounter();             //...get current SW_63Cx value...
            ISOException.throwIt(triesCounter);                     //...and pass it as exception argument
        }
    }
}
