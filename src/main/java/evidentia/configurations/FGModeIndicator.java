package evidentia.configurations;

/**
 * This class represents the Flight Guidance Mode Indicator component
 *
 */
public class FGModeIndicator {

    private String lateralMode;
    private String verticalMode;

    public FGModeIndicator(){
        this.lateralMode = "Roll Hold";
        this.verticalMode = "Pitch Hold";
    }

    public void changeLateralMode(String newMode){
        this.lateralMode = newMode;
    }

    public void changeVerticalMode(String newMode){
        this.verticalMode = newMode;
    }

    public String getLateralMode() {
        return lateralMode;
    }

    public String getVerticalMode() {
        return verticalMode;
    }
}
