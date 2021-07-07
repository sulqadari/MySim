package mysim.javacard;

import javacard.framework.APDU;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacardx.crypto.Cipher;

public class AppletController
{
	private static final byte CLA_BYTE		= (byte)0x80;
    static final byte INS_VERIFY_PIN		= (byte)0x20;
    static final byte INS_UPDATE_PIN		= (byte)0x22;

    static final byte INS_GENERATE_AES_KEY	= (byte)0x50;
    static final byte INS_UNWRAP_AES_KEY	= (byte)0x52;
    static final byte INS_AES_CALC_KCV		= (byte)0x54;
    static final byte INS_PROCESS_AES		= (byte)0x56;

    private PINController pin				= null;
    private AESController aes				= null;

    protected AppletController(byte[] pinArr, short pinOff, byte pinLen, byte tryLimit) throws ISOException
    {
    	pin	= new PINController(tryLimit, pinLen);
    	aes	= new AESController();
    	pin.update(pinArr, pinOff, pinLen);
    }

    protected void process(APDU apdu)
    {
        byte[] buffer   = apdu.getBuffer();
        
        byte cla		= buffer[ISO7816.OFFSET_CLA];
        byte ins		= buffer[ISO7816.OFFSET_INS];
        byte p1			= buffer[ISO7816.OFFSET_P1];
        byte p2			= buffer[ISO7816.OFFSET_P2];
        
        if (cla != CLA_BYTE)
        {
            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
        }

        switch (ins)
        {
            case INS_VERIFY_PIN:
            {
            	byte pinLength  = (byte)apdu.setIncomingAndReceive();
                checkPin(buffer, ISO7816.OFFSET_CDATA, pinLength);
            }break;
            case INS_UPDATE_PIN:
            {
            	byte pinLength  = (byte)apdu.setIncomingAndReceive();
                updatePin(buffer, ISO7816.OFFSET_CDATA, pinLength);
            }break;
            case INS_GENERATE_AES_KEY:
            {
            	if (!pin.isValidated())
            		ISOException.throwIt(PINController.SW_PIN_NOT_VERIFIED);
            	
            	byte len = aes.generateAesKey(buffer);
            	apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, (short)len);
            }break;
            case INS_PROCESS_AES:
            {
            	if (!pin.isValidated())
            		ISOException.throwIt(PINController.SW_PIN_NOT_VERIFIED);
            	
            	if (p1 == AESController.P1_INIT_KEY)
            	{
            		short apduBlockSize	= APDU.getInBlockSize();		//get actual IFSC to control doFinal() method invocation
            		if (p2 == AESController.P2_ENCRYPT)
            		{
            			aes.initAesKey(Cipher.MODE_ENCRYPT);
        				aes.encryptData(apdu, apduBlockSize);
            		}
            		else if (p2 == AESController.P2_DECRYPT)
            		{
            			aes.initAesKey(Cipher.MODE_DECRYPT);
    					aes.decryptData(apdu, apduBlockSize);
            		}
            		else
            			ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
            	}
            	else
            		ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
            }break;
            case INS_UNWRAP_AES_KEY:
            {
            	if (!pin.isValidated())
            		ISOException.throwIt(PINController.SW_PIN_NOT_VERIFIED);
            	
            	byte keyLen  = (byte)apdu.setIncomingAndReceive();
            	
            	if (keyLen != (short)0x10)
            		ISOException.throwIt(ISO7816.SW_WRONG_DATA);
            	
            	aes.initAesKey(Cipher.MODE_DECRYPT);
            	aes.unwrapNewKey(buffer, keyLen);
            }break;
            case INS_AES_CALC_KCV:
            {
            	if (!pin.isValidated())
            		ISOException.throwIt(PINController.SW_PIN_NOT_VERIFIED);
            	
            	aes.initAesKey(Cipher.MODE_ENCRYPT);
            	aes.calculateKcv(buffer);
            }break;
            
            default: ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
        }
    }
    
    protected void updatePin(byte[] pinArray, short pinOffset, byte pinLength)
    {
    	short attemptsLeft = pin.getTriesRemaining();

        if ((short)((short)attemptsLeft & (short)0x000F) <= (short)0)
        	ISOException.throwIt(PINController.SW_PIN_IS_BLOCKED);
        
        if (!pin.isValidated())
        	ISOException.throwIt(PINController.SW_PIN_NOT_VERIFIED);
        
        if ((pinOffset + pinLength) > pinArray.length)
            ISOException.throwIt(ISO7816.SW_WRONG_DATA);

    	pin.update(pinArray, pinOffset, pinLength);
        pin.reset();												//...reset SW_63Cx to initial value
        
    }

    private void checkPin(byte[] pinArray, byte pinOffset, byte pinLength) throws ISOException, NullPointerException, ArrayIndexOutOfBoundsException
    {
    	short attemptsLeft = pin.getTriesRemaining();
    	attemptsLeft &= (short)0x000F;
    	
    	if ((short)((short)attemptsLeft & (short)0x000F) <= (short)0)		//If no attempts left
        	ISOException.throwIt(PINController.SW_PIN_IS_BLOCKED);

        boolean isValid = pin.check(pinArray, pinOffset, pinLength);//verify PIN

        if (!isValid)
        	throwAttemptsRemainig();
        	
    }

    private void throwAttemptsRemainig()
    {
        short triesCounter = pin.getTriesRemaining();             	//...get current SW_63Cx value...
        ISOException.throwIt(triesCounter);                     	//...and pass it as the exception argument
    }

    protected void resetPin()
    {
        pin.reset();
    }
}