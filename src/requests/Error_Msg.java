package requests;

import java.io.Serial;

public class Error_Msg extends Message{
    @Serial
    private static final long serialVersionUID = 1L;
    private final String errorText;

    public Error_Msg(String errorText) {
        super("ERROR");
        this.errorText = errorText;
    }

    public String getErrorText() {
        return errorText;
    }
}
