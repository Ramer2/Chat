package requests;

import java.io.Serial;

public class Success_Msg extends Message {
    @Serial
    private static final long serialVersionUID = 1L;
    private String contents = null;

    public Success_Msg() {
        super("SUCCESS");
    }

    public Success_Msg(String contents) {
        super("SUCCESS");
        this.contents = contents;
    }

    public String getContents() {
        return contents;
    }
}
