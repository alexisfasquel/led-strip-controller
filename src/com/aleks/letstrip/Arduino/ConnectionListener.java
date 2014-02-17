package com.aleks.letstrip.Arduino;

/**
 * Created with IntelliJ IDEA.
 * User: Aleks
 * Date: 17/02/14
 * Time: 05:21
 * To change this template use File | Settings | File Templates.
 */
public interface ConnectionListener {

    public void onConnected();
    public void onDisconnected();
}
