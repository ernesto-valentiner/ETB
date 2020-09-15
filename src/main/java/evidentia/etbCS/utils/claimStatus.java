package evidentia.etbCS.utils;

import java.util.*;

public class claimStatus {
    
    List<Integer> updatedInputPosS = new ArrayList<Integer>();
    List<String> updatedServices = new ArrayList<String>();
    String workFlowStatus = "notUpdated";
    
    public claimStatus(List<Integer> updatedInputPosS, List<String> updatedServices, String workFlowStatus) {
        this.updatedInputPosS = updatedInputPosS;
        this.updatedServices = updatedServices;
        this.workFlowStatus = workFlowStatus;
    }
    
    public boolean inputsUpdated() {
        return (updatedInputPosS.size() != 0);
    }
    
    public boolean servicesUpdated() {
        return (updatedServices.size() != 0);
    }
    
    public List<Integer> getUpdatedInputs() {
        return updatedInputPosS;
    }
    
    public List<String> getUpdatedServices() {
        return updatedServices;
    }
    
    public String getWorkFlowStatus() {
        return workFlowStatus;
    }
    
    public boolean isUptodate() {
        return ((updatedInputPosS.size() == 0) && (updatedServices.size() == 0) && workFlowStatus.equals("notUpdated"));
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("updatedInputPosS : " + updatedInputPosS);
        sb.append("\nupdatedServices : " + updatedServices);
        sb.append("\nworkFlowStatus : " + workFlowStatus);
        return sb.toString();
    }
}

