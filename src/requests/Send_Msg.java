package requests;

import java.io.Serial;

public class Send_Msg extends Message{

    @Serial
    private static final long serialVersionUID = 1L;
    private final String[] recipients;
    private final String contents;

    public Send_Msg(String[] recipients, String contents) {
        super("SEND_MSG");
        this.recipients = recipients;
        this.contents = contents;
    }

    public String[] getRecipients() {
        return recipients;
    }

    public String getContents() {
        return contents;
    }
}
