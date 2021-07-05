package mysim.javacard;

public class AESController
{
    final static byte INS_GENERATE_AES_KEY  = (byte)0x50;
    final static byte INS_UNWRAP_AES_KEY    = (byte)0x52;
    final static byte AES_CALC_KCV          = (byte)0x54;
    final static byte INS_PROCESS_AES       = (byte)0x56;

    final static byte P1_INIT_KEY           = (byte)0x01;
    final static byte P1_UPDATE             = (byte)0x02;
    final static byte P1_FINALIZE           = (byte)0x03;

    final static byte P2_DECRYPT            = (byte)0x00;
    final static byte P2_ENCRYPT            = (byte)0x01;


}
