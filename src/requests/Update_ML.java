package requests;

import java.io.Serial;

public class Update_ML extends Message{
    @Serial
    private static final long serialVersionUID = 1L;
    private final String[] members;

    public Update_ML(String[] members) {
        super("UPDATE_ML");
        this.members = members;
    }

    public String[] getMemberList() {
        return members;
    }
}
