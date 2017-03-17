package device;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;

/**
 * Utility for performing keyboard input operations.
 *
 * <p>
 *    It uses a {@link Robot} object to generate native system
 *    input events. This differs from posting events to the AWT
 *    event queue or AWT components in that the events are
 *    generated in the platform's native input queue.
 * </p>
 *
 * @see Robot
 */
public class KeyboardController {
    private Robot mRobot;

    /**
     * Constructor. <br/>
     * Initializes this {@code KeyboardController} by
     * instantiating a {@link Robot} object.
     *
     * @throws AWTException
     */
    public KeyboardController() throws AWTException {
        mRobot = new Robot();
    }

    /**
     * Method to perform the keyboard input operations.
     *
     * @param s {@code String} representing the sequence of
     *          characters to be typed.
     */
    public void doKeyOperation(String s) {

        if (s.equals("backspace")) {
            mRobot.keyPress(KeyEvent.VK_BACK_SPACE);
            mRobot.keyRelease(KeyEvent.VK_BACK_SPACE);
            return;
        }
        if (s.equals("enter")) {
            mRobot.keyPress(KeyEvent.VK_ENTER);
            mRobot.keyRelease(KeyEvent.VK_ENTER);
            return;
        }
        int keyLength = s.length();
        if(keyLength == 0) {
            performAction('\n');
            return;
        }
        for (int index = 0; index < keyLength; ++index) performAction(s.charAt(index));
    }
    private void performAction(char key) {

        if(KeyCode.isShiftKey(key)) {
            int keyCode = KeyCode.getShiftCode(key);
            if(keyCode == -1)   System.out.println("Invalid char! " + key);
            else doShift(keyCode);
            return;
        }
        int keyCode = KeyCode.getCodeByChar(key);
        if (keyCode == -1) System.out.println("Invalid char! " + key);
        else {
            if (Character.isUpperCase(key))   doShift(keyCode);
            else {
                mRobot.keyPress(keyCode);
                mRobot.keyRelease(keyCode);
            }
        }
    }
    private void doShift(int keyCode) {
        mRobot.keyPress(KeyEvent.VK_SHIFT);
        mRobot.keyPress(keyCode);
        mRobot.keyRelease(keyCode);
        mRobot.keyRelease(KeyEvent.VK_SHIFT);
    }
}