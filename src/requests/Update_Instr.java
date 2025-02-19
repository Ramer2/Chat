package requests;

import java.io.Serial;

public class Update_Instr extends Message{

    @Serial
    private static final long serialVersionUID = 1L;
    String instructions;
    public Update_Instr(String instructions) {
        super("UPDATE_INSTR");
        this.instructions = instructions;
    }

    public String getInstructions() {
        return instructions;
    }
}
