package mysim.javacard;

import javacard.framework.APDU;
import javacard.framework.ISOException;

public class Applet extends javacard.framework.Applet
{
    private Applet()
    {

    }

    public static void install(byte[] bArray, short bOffset, byte bLength)
    {
        new Applet().register();
    }

    @Override
    public void process(APDU apdu) throws ISOException
    {
        if (selectingApplet())
        {
            return;
        }

        byte[] buffer = apdu.getBuffer();
    }
}
