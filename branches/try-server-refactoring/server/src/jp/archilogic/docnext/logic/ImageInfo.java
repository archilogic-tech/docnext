package jp.archilogic.docnext.logic;

public class ImageInfo {
    private double unitWidth;
    private double unitHeight;
    private int pages;

    ImageInfo( double unitWidth , double unitHeight, int pages ) {
        this.unitWidth = unitWidth;
        this.unitHeight = unitHeight;
        this.pages = pages;
    }
    
    public double getUnitWidth() {
        return unitWidth;
    }

    public double getUnitHeight() {
        return unitHeight;
    }

    public double getRatio() {
        return this.unitWidth / unitHeight;
    }

    public int getPages() {
        return pages;
    }
}
