package requests;

import java.io.Serial;

public class Request_Instr extends Message{

    @Serial
    private static final long serialVersionUID = 1L;
    public Request_Instr() {
        super("REQUEST_INSTR");
    }
}
