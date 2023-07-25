package fr.istic.service.customdto;

import java.util.List;

public class ClusterDTO {
    private int templat;
    private List<Integer> copies;

    public int getTemplat() { return templat; }
    public void setTemplat(int value) { this.templat = value; }

    public List<Integer> getCopies() { return copies; }
    public void setCopies(List<Integer> value) { this.copies = value; }
}
