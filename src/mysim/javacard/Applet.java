package mysim.javacard;

import javacard.framework.APDU;
import javacard.framework.ISOException;


public class Applet extends javacard.framework.Applet
{
    private static AppletController controller  = null;     //Contains main functionality

    private Applet(byte[] bArray)
    {
        controller  = new AppletController(bArray[0], bArray[1]);
    }

    public static void install(byte[] bArray, short bOffset, byte bLength) throws ISOException
    {
        new Applet(bArray).register();
    }

    @Override
    public boolean select()
    {
       controller.resetPin();
        return true;
    }

    @Override
    public void process(APDU apdu) throws ISOException
    {
        if (selectingApplet())
        {
            controller.resetPin();
            return;
        }

        byte[] buffer = apdu.getBuffer();
        controller.checkPin(buffer);
        controller.updatePin(buffer);
        controller.process(apdu, buffer);
    }
}
