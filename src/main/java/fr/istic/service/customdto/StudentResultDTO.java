package fr.istic.service.customdto;

import java.util.HashMap;
import java.util.Map;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class StudentResultDTO extends StudentDTO{
    String uuid;
    String note;
    boolean abi;
    Map<Integer, String> notequestions =  new HashMap<>();

    public String getUuid() {
        return uuid;
    }
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
    public String getNote() {
        return note;
    }
    public void setNote(String note) {
        this.note = note;
    }
    public boolean isAbi() {
        return abi;
    }
    public void setAbi(boolean abi) {
        this.abi = abi;
    }

    public Map<Integer, String> getNotequestions() {
        return notequestions;
    }
    public void setNotequestions(Map<Integer, String> notequestions) {
        this.notequestions = notequestions;
    }


    @Override
    public String toString() {
        return "StudentResultDTO [note=" + note + ", uuid=" + uuid +", abi=" + abi +
        super.toString() +
        "]";
    }



}
