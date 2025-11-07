package ui.doctor;

public class TimeSlot {
    private String startTime;
    private String endTime;
    private boolean isSelected;
    private boolean isDisabled;

    public TimeSlot(String startTime, String endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.isSelected = false;
        this.isDisabled = false;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public boolean isDisabled() {
        return isDisabled;
    }

    public void setDisabled(boolean disabled) {
        isDisabled = disabled;
    }

    public String getDisplayTime() {
        return startTime + " - " + endTime;
    }
}
