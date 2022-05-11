package fr.istic.service.customdto;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class StudentResultDTO extends StudentDTO{
    String uuid;
    long note;
    boolean abi;

    public String getUuid() {
        return uuid;
    }
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
    public long getNote() {
        return note;
    }
    public void setNote(long note) {
        this.note = note;
    }
    public boolean isAbi() {
        return abi;
    }
    public void setAbi(boolean abi) {
        this.abi = abi;
    }


    @Override
    public String toString() {
        return "StudentResultDTO [note=" + note + ", uuid=" + uuid +", abi=" + abi +
        super.toString() +
        "]";
    }



}
