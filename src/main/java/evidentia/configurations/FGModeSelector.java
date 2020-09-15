package evidentia.configurations;

/**
 * This class represents the Flight Guidance Mode Selector component
 * 
 */
public class FGModeSelector {

    private boolean isThisActiveSide;
    private boolean modeAnnunciationsOn;
    private boolean onSideFD;
    private boolean offSideFD;
    private boolean engagedAP;


    public FGModeSelector(boolean activeSide){
        this.isThisActiveSide = activeSide;
        this.modeAnnunciationsOn = false;
        this.onSideFD = false;
        this.offSideFD = false;
        this.engagedAP = false;
    }

    public void turnOnModeAnnunciations() {
        if(isThisActiveSide && !modeAnnunciationsOn && !onSideFD) {
            this.modeAnnunciationsOn = true;
        }
    }

    public void turnOffModeAnnunciations() {
        if(isThisActiveSide && modeAnnunciationsOn && !onSideFD && !offSideFD && !engagedAP){
            this.modeAnnunciationsOn = false;
        }
    }

    public boolean isModeAnnunciationsOn() {
        return modeAnnunciationsOn;
    }
}
