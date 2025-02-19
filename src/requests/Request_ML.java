package requests;

import java.io.Serial;

public class Request_ML extends Message{

    @Serial
    private static final long serialVersionUID = 1L;
    public Request_ML() {
        super("REQUEST_ML");
    }
}
