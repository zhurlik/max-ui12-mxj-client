package com.zhurlik.max8.ui12.component;

/**
 * The commands that are used for handling the Max signals into inlet.
 *
 * @author zhurlik@gmail.com
 */
enum Command {
    /**
     * The command to specify url of the WebSocket server.
     */
    SET_URL,
    /**
     * The command for sending a message to Ui12 device.
     */
    SEND_MESSAGE,

    /**
     * An empty command.
     */
    UNDEFINED;

    /**
     * To define the command by incoming string in the inlet.
     *
     * @param name expected either url or msg
     * @return SET_URL or SEND_MESSAGE, by default UNDEFINED
     */
    static Command findBy(final String name) {
        if ("url".equalsIgnoreCase(name)) {
            return SET_URL;
        }

        if ("msg".equalsIgnoreCase(name)) {
            return SEND_MESSAGE;
        }

        return UNDEFINED;
    }
}
