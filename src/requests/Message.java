package requests;

import java.io.Serializable;

public abstract class Message implements Serializable {
    // types: REQUEST_CONN, REQUEST_BP (request banned phrases), REQUEST_ML (request member list), SEND_MSG (send message from client),
    // REQUEST_ML (request member list), REQUEST_INSTR (request instructions)
    // ERROR, RECEIVE_MSG (receive the forwarded message from the server), SUCCESS (success of the previous operation),
    // UPDATE_INSTR (receive the instructions from the server), UPDATE_ML (receive the member list from the server)
    private final String type;

    public Message(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
