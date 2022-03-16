package fr.istic.service.customdto;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class ResizeZoneDTO {
    private int left;

    private int top;
    private double x;
    private double y;

    public int getLeft() { return left; }
    public void setLeft(int value) { this.left = value; }

    public int getTop() { return top; }
    public void setTop(int value) { this.top = value; }

    public double getX() { return x; }
    public void setX(double value) { this.x = value; }

    public double getY() { return y; }
    public void setY(double value) { this.y = value; }
    @Override
    public String toString() {
        return "ResizeZoneDTO [left=" + left + ", top=" + top + ", x=" + x + ", y=" + y + "]";
    }
}
