package mysim.javacard;

import javacard.security.AESKey;
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
    
    protected AESController()
    {
    	cipher	= Cipher.getInstance(Cipher.ALG_AES_BLOCK_128_CBC_NOPAD, false);
    	key		= (AESKey)KeyBuilder.buildKey(KeyBuilder.TYPE_AES, KeyBuilder.LENGTH_AES_128, false);
    }
    
    protected void initAesKey(byte mode)
    {
    	cipher.init(key, mode);
    }
    
    protected void encryptData(byte[] array)
    {
    	
    }
    
    protected void decryptData(byte[] array)
    {
    	
    }
}