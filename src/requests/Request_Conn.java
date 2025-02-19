package requests;

import java.io.Serial;

public class Request_Conn extends Message{
    @Serial
    private static final long serialVersionUID = 1L;
    String nickname;

    public Request_Conn(String nickname) {
        super("REQUEST_CONN");
        this.nickname = nickname;
    }

    public String getNickname() {
        return nickname;
    }
}
