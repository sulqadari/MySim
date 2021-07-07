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
    private byte[] keyCheckValue;
    
    protected AESController()
    {
    	cipher			= Cipher.getInstance(Cipher.ALG_AES_BLOCK_128_CBC_NOPAD, false);
    	data			= new byte[512];
    	keyCheckValue	= new byte[3];
    	dataOffset		= (short)0;
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
		if (key == null)
    		CryptoException.throwIt(CryptoException.INVALID_INIT);
		
    	byte[] buffer		= apdu.getBuffer();
		short read_count	= apdu.setIncomingAndReceive();							//Actual data received
		dataOffset			+= read_count;
		
		do
		{
			if (dataOffset > data.length)
	    		ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
			
			if (read_count > apduBlockSize)
				cipher.update(buffer, ISO7816.OFFSET_CDATA, read_count, data, dataOffset);
			else
				cipher.doFinal(buffer, ISO7816.OFFSET_CDATA, read_count, data, dataOffset);

			read_count	= apdu.receiveBytes(ISO7816.OFFSET_CDATA);								//if there is no data anymore...
			dataOffset	+= read_count;
		}while(read_count > 0);														//... get out from the loop
    }

    protected void decryptData(APDU apdu, short apduBlockSize)
    {
		if (key == null)
    		CryptoException.throwIt(CryptoException.INVALID_INIT);
		
    	byte[] buffer	= apdu.getBuffer();
    	short offset	= 0;
    	short dLen		= dataOffset;
    	apdu.setOutgoing();
    	
    	do
    	{
    		if (dLen < apduBlockSize)
    		{
    			cipher.doFinal(data, offset, dLen, buffer, ISO7816.OFFSET_CDATA);
    			apdu.setOutgoingLength(dataOffset);
    			apdu.sendBytes(ISO7816.OFFSET_CDATA, dataOffset);
    			dataOffset = 0;
    		}
    		else
    		{
    			cipher.update(data, offset, apduBlockSize, buffer, ISO7816.OFFSET_CDATA);
    			apdu.setOutgoingLength(apduBlockSize);
    			apdu.sendBytes(ISO7816.OFFSET_CDATA, apduBlockSize);
    			offset += apduBlockSize;
    			dLen -= apduBlockSize;
    		}
    	}while(dataOffset > (short)0);
    }
    
    protected void unwrapNewKey(byte[] buffer, short keyLength)
    {
    	short outputOff = (short)(ISO7816.OFFSET_CDATA + keyLength);
    	cipher.doFinal(buffer, ISO7816.OFFSET_CDATA, keyLength, buffer, outputOff);
    	
    	key.setKey(buffer, outputOff);
    }
    
    protected void calculateKcv(byte[] buffer)
    {
    	short kcvOff = (short)(ISO7816.OFFSET_CDATA + (short)0x10);
    	
    	cipher.doFinal(buffer, ISO7816.OFFSET_CDATA, (short)0x10, buffer, kcvOff);
    	
    	for (short i = 0; i < keyCheckValue.length; ++i)
    		keyCheckValue[i] = buffer[(short)(kcvOff + i)];
    }
}