package requests;

import java.io.Serial;

public class Send_All extends Message{
    @Serial
    private static final long serialVersionUID = 1L;
    private final String contents;

    public Send_All(String contents) {
        super("SEND_ALL");
        this.contents = contents;
    }

    public String getContents() {
        return contents;
    }
}
