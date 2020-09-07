package com.zhurlik.max8.ui12.component;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * This class implements the logic for handling and for forwarding the messages.
 *
 * @author zhurlik@gmail.com
 */
class MessageHandler implements Consumer<String> {

    /**
     * For forwarding to the outlets.
     * See {@link com.cycling74.max.MaxObject#outlet(int, String[])}
     */
    private final Outlets outlets;

    /**
     * A constructor.
     *
     * @param outlets reference to the outlets of the MaxObject.
     *                see {@link com.cycling74.max.MaxObject#outlet(int, String[])}
     */
    MessageHandler(final Outlets outlets) {
        this.outlets = outlets;
    }

    /**
     * A multi-line message will be split into multiple messages.
     *
     * NOTE: there are a few custom replacements of the original message.
     *
     * @param message incoming message via WebSocket
     */
    @Override
    public void accept(final String message) {
        if (message == null) {
            return;
        }
        // split by '\n'
        Arrays.stream(message.split("\n"))
                // replace '^' -> ' '
                .map(s -> s.replaceAll("\\^", " "))
                // trick with last space
                .map(s -> s.replaceAll(" $", " \" \""))
                // sending to the corresponded outlet
                .forEach(s -> outlets.toMainOutlet(new String[]{s}));
    }

    /**
     * Output channel. See {@link com.cycling74.max.MaxObject#outlet(int, String[])}
     *
     * @return a function for forwarding the messages to the outlet
     */
    Outlets getOutlets() {
        return outlets;
    }
}
