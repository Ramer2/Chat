package requests;

import java.io.Serial;

public class Receive_Msg extends Message{
    @Serial
    private static final long serialVersionUID = 1L;
    String contents;
    String sender;

    public Receive_Msg(String contents, String sender) {
        super("RECEIVE_MSG");
        this.contents = contents;
        this.sender = sender;
    }

    public String getContents() {
        return contents;
    }

    public String getSender() {
        return sender;
    }
}
