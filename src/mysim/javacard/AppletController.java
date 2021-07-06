package mysim.javacard;

import javacard.framework.*;

public class AppletController
{
	private final byte[] pinArray				= {(byte)0x11, (byte)0x22, (byte)0x33, (byte)0x44, (byte)0x55, (byte)0x66, (byte)0x77, (byte)0x88};
	
    private static final byte CLA_BYTE		= (byte)0x80;
    static final byte INS_VERIFY_PIN		= (byte)0x20;
    static final byte INS_UPDATE_PIN		= (byte)0x22;

    static final byte INS_GENERATE_AES_KEY	= (byte)0x50;
    static final byte INS_UNWRAP_AES_KEY	= (byte)0x52;
    static final byte INS_AES_CALC_KCV		= (byte)0x54;
    static final byte INS_PROCESS_AES		= (byte)0x56;

    private PINController pin				= null;
    private RSAController rsa				= null;
    private AESController aes				= null;

    protected AppletController() throws PINException
    {
    	pin	= new PINController();
    	pin.update(pinArray, (short)0, PINController.pinLength);
    	
    	rsa	= new RSAController();
    	aes	= new AESController();
    }

    protected void process(APDU apdu)
    {
        byte[] buffer   = apdu.getBuffer();
        byte pinLength;

        if (buffer[ISO7816.OFFSET_CLA] != CLA_BYTE)
        {
            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
        }

        switch (buffer[ISO7816.OFFSET_INS])
        {
            case INS_VERIFY_PIN:
            {
            	pinLength  = (byte)apdu.setIncomingAndReceive();
                checkPin(buffer, ISO7816.OFFSET_CDATA, pinLength);
            }break;
            case INS_UPDATE_PIN:
            {
            	pinLength  = (byte)apdu.setIncomingAndReceive();
                updatePin(buffer, ISO7816.OFFSET_CDATA, pinLength);
            }break;
            case INS_GENERATE_AES_KEY:
            {

            }break;
            default: ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
        }
    }

    protected void updatePin(byte[] pinArray, short pinOffset, byte pinLength)
    {
    	short attemptsLeft = pin.getLimitCounter();

        if ((short)(attemptsLeft & (short)0x000F) <= (short)0)
            PINException.throwIt(PINController.PIN_IS_BLOCKED);
        
        //Any attempt to update PIN whith out prior validation lays to PIN-counter decrementation
        if (!pin.isValidated())
        	decrementLimitCounterAndThrowException();

        if (pinArray == null)
            ISOException.throwIt(ISO7816.SW_WRONG_DATA);

        if ((pinOffset + pinLength) > pinArray.length)
            ISOException.throwIt(ISO7816.SW_WRONG_DATA);

        if (pinLength != PINController.pinLength)
            PINException.throwIt(PINException.ILLEGAL_VALUE);

        JCSystem.beginTransaction();								//For the sake of PIN integrity it is must be updated using atomicity facility.  
    	pin.update(pinArray, pinOffset, pinLength);
        pin.resetPinCounter();										//...reset SW_63Cx to initial value
        JCSystem.commitTransaction();								//...PIN 
    }

    private void checkPin(byte[] pinArray, byte pinOffset, byte pinLength) throws ISOException, NullPointerException, ArrayIndexOutOfBoundsException
    {
    	short attemptsLeft = pin.getLimitCounter();

        if ((short)(attemptsLeft & (short)0x000F) <= (short)0)		//If no attempts left
            PINException.throwIt(PINController.PIN_IS_BLOCKED);

        if (pinArray == null)
            decrementLimitCounterAndThrowException();

        if ((pinOffset + pinLength) > pinArray.length)
            decrementLimitCounterAndThrowException();

        boolean isValid = pin.check(pinArray, pinOffset, pinLength);//verify PIN

        if (isValid)												//if PIN successfully verified...
	        pin.resetPinCounter();                       			//...reset SW_63Cx to maximum value
        else
	        decrementLimitCounterAndThrowException();
    }

    private void decrementLimitCounterAndThrowException()
    {
        pin.decrementLimitCounter();                            	//...decrement SW_63Cx...
        short triesCounter = pin.getLimitCounter();             	//...get current SW_63Cx value...
        ISOException.throwIt(triesCounter);                     	//...and pass it as the exception argument
    }

    protected void resetPin()
    {
        pin.reset();
    }
}