package fr.istic.service.customdto;

import io.quarkus.runtime.annotations.RegisterForReflection;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
@RegisterForReflection
@Schema(description = "Resize coordinates applied to a zone.")
public class ResizeZoneDTO {
    @Schema(description = "Left offset of the resize.")
    private int left;

    @Schema(description = "Top offset of the resize.")
    private int top;
    @Schema(description = "Horizontal scale factor.")
    private double x;
    @Schema(description = "Vertical scale factor.")
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
