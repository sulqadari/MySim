package mysim.javacard;

import javacard.framework.*;

public class AppletController
{
    private static byte APPLET_LIFE_PHASE               = (byte)0x00;
    private static final byte APPLET_LIFE_PHASE_INIT    = (byte)0x03;
    private static final byte APPLET_LIFE_PHASE_USE     = (byte)0x07;

    private static final byte CLA_BYTE                  = (byte)0x80;
    private static final byte INS_VERIFY_PIN            = (byte)0x20;
    private static final byte INS_UPDATE_PIN            = (byte)0x22;

    private static PINController pin                    = null;
    private RSAController rsa                           = null;
    private AESController aes                           = null;

    protected AppletController(byte tryLimit, byte maxPINSize) throws PINException
    {
        pin    = new PINController(tryLimit, maxPINSize);
        rsa    = new RSAController();
        aes    = new AESController();
    }

    protected void process(APDU apdu)
    {
        if (pin.getTriesRemaining() == 0)
        {
            PINException.throwIt(PINController.PIN_IS_BLOCKED);
        }

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
            ISOException.throwIt(ISO7816.SW_WRONG_DATA);

        if ((pinOffset + pinLength) > buffer.length)
            ISOException.throwIt(ISO7816.SW_WRONG_DATA);

        if (length != (byte)8)
            PINException.throwIt(PINException.ILLEGAL_VALUE);

        if (APPLET_LIFE_PHASE == APPLET_LIFE_PHASE_INIT)
            updatePinOnInit(pinArray, offset, length);

        else if (APPLET_LIFE_PHASE == APPLET_LIFE_PHASE_USE)
            updatePinOnUse(pinArray, offset, length);
    }

    private static void updatePinOnInit(byte[] pinArray, short offset, byte length)
    {
        byte attemptsLeft = 0;

        JCSystem.beginTransaction();                                //For the sake of PIN integrity...

        pin.update(pinArray, offset, length);
        attemptsLeft = pin.getTriesRemaining();
        pin.setPinCounter(attemptsLeft);                        //...reset SW_63Cx to initial value

        JCSystem.commitTransaction();                               //...PIN must be updated using atomicity facility.
    }

    private static void updatePinOnUse(byte[] pinArray, short offset, byte length)
    {
        boolean isValid     = pin.isValidated();
        byte attemptsLeft   = 0;

        if (!isValid)
            PINException.throwIt(PINController.PIN_NOT_VERIFIED);

        JCSystem.beginTransaction();                                //For the sake of PIN integrity...

        pin.update(pinArray, offset, length);
        attemptsLeft = pin.getTriesRemaining();
        pin.setPinCounter(attemptsLeft);                        //...reset SW_63Cx to initial value

        JCSystem.commitTransaction();                               //...PIN must be updated using atomicity facility.
    }

    private void checkPin(byte[] buffer, byte pinOffset, byte pinLength) throws ISOException, NullPointerException, ArrayIndexOutOfBoundsException
    {
        byte attemptsLeft = pin.getTriesRemaining();

        if (attemptsLeft <= 0)
            PINException.throwIt(PINController.PIN_IS_BLOCKED);

        if (buffer == null)
            decrementLimitCounterAndThrowException();

        if ((pinOffset + pinLength) > buffer.length)
            decrementLimitCounterAndThrowException();

        boolean isSet = pin.check(buffer, pinOffset, pinLength);    //verify PIN

        if (isSet)                                                  //if PIN successfully verified...
        {
            attemptsLeft = pin.getTriesRemaining();
            pin.setPinCounter(attemptsLeft);                        //...reset SW_63Cx to initial value
        }
        else
        {
            decrementLimitCounterAndThrowException();
        }
    }

    private void decrementLimitCounterAndThrowException()
    {
        pin.decrementLimitCounter();                            //...decrement SW_63Cx...
        short triesCounter = pin.getLimitCounter();             //...get current SW_63Cx value...
        ISOException.throwIt(triesCounter);                     //...and pass it as the exception argument
    }

    protected void resetPin()
    {
        pin.reset();
    }

    protected void changeAppletLifePhase()
    {
        if (APPLET_LIFE_PHASE == 0x00)
            APPLET_LIFE_PHASE = APPLET_LIFE_PHASE_INIT;
        else if (APPLET_LIFE_PHASE == APPLET_LIFE_PHASE_INIT)
            APPLET_LIFE_PHASE = APPLET_LIFE_PHASE_USE;
    }
}
