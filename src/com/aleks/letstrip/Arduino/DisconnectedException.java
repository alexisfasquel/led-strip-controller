package com.aleks.letstrip.Arduino;

/**
 * Created with IntelliJ IDEA.
 * User: Aleks
 * Date: 17/02/14
 * Time: 10:38
 * To change this template use File | Settings | File Templates.
 */
public class DisconnectedException extends Exception {
    public DisconnectedException() {
        super("No connected device");
    }

}
