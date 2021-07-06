package mysim.javacard;

import javacard.framework.APDU;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.security.AESKey;
import javacard.security.CryptoException;
import javacard.security.KeyBuilder;
import javacardx.crypto.Cipher;

public class AESController
{
    final static byte P1_INIT_KEY           = (byte)0x01;
    final static byte P1_UPDATE             = (byte)0x02;
    final static byte P1_FINALIZE           = (byte)0x03;

    final static byte P2_DECRYPT            = (byte)0x00;
    final static byte P2_ENCRYPT            = (byte)0x01;
    
    private Cipher cipher;
    private AESKey key;
    private byte[] data;
    private short dataOffset;
    protected AESController()
    {
    	cipher		= Cipher.getInstance(Cipher.ALG_AES_BLOCK_128_CBC_NOPAD, false);
    	data		= new byte[512];
    	dataOffset	= (short)0;
    }
    
    /**
     * Generates and returns a plain text of a new AES 128 bit key.
     * @param buffer	byte array to return key data.
     * @return 			byte length of the key data returned.
     */
    protected byte generateAesKey(byte[] buffer)
    {
    	if (key == null)
    		key = (AESKey)KeyBuilder.buildKey(KeyBuilder.TYPE_AES, KeyBuilder.LENGTH_AES_128, false);
    	
    	return key.getKey(buffer, (short)0);
    }
    
    protected void initAesKey(byte mode)
    {
    	if (key == null)
    		CryptoException.throwIt(CryptoException.INVALID_INIT);
    	
    	if ((mode != Cipher.MODE_ENCRYPT) || (mode != Cipher.MODE_DECRYPT))
    		CryptoException.throwIt(CryptoException.INVALID_INIT);
    	
    	cipher.init(key, mode);
    }

    protected void encryptData(APDU apdu, short apduBlockSize)
    {
    	byte[] buffer		= apdu.getBuffer();
		short read_count	= apdu.setIncomingAndReceive();							//Actual data received
		dataOffset			+= read_count;
		
		do
		{
			if (dataOffset > data.length)
	    		ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
			
			if (read_count > apduBlockSize)
				cipher.update(buffer, (short)0, read_count, data, dataOffset);
			else
				cipher.doFinal(buffer, (short)0, read_count, data, dataOffset);

			read_count	= apdu.receiveBytes((short)0);								//if there is no data anymore...
			dataOffset	+= read_count;
		}while(read_count > 0);														//... get out from the loop
    }

    protected void decryptData(APDU apdu, short apduBlockSize)
    {
    	byte[] buffer	= apdu.getBuffer();
    	short offset	= 0;
    	short dLen		= dataOffset;
    	apdu.setOutgoing();
    	
    	do
    	{
    		if (dLen < apduBlockSize)
    		{
    			cipher.doFinal(data, offset, dLen, buffer, (short)0);
    			apdu.setOutgoingLength(dataOffset);
    			apdu.sendBytes((short)0, dataOffset);
    			dataOffset = 0;
    		}
    		else
    		{
    			cipher.update(data, offset, apduBlockSize, buffer, (short)0);
    			apdu.setOutgoingLength(apduBlockSize);
    			apdu.sendBytes((short)0, apduBlockSize);
    			offset += apduBlockSize;
    			dLen -= apduBlockSize;
    		}
    	}while(dataOffset > (short)0);
    }
}