package evidentia;

public class EntityType {

	String connectionProfileFilename;
	String coordinatorProfileFilename;
	String caPemFilename;
	String caClientUrl;
	String entityName; //organization name
	String networkChannelName;
	String chaincodeName;
	String connectionProfilePath = "src/main/resources/";

	public EntityType(String connectionProfileFilename, String coordinatorProfileFilename, String caPemFilename, String caClientUrl, String entityName, String networkChannelName, String chaincodeName) {
		this.connectionProfileFilename = connectionProfileFilename;
		this.caPemFilename = caPemFilename;
		this.caClientUrl = caClientUrl;
		this.entityName = entityName;
		this.networkChannelName = networkChannelName;
		this.chaincodeName = chaincodeName;
		this.coordinatorProfileFilename = coordinatorProfileFilename;
	}

	public String getConnectionProfilePath() {
		return this.connectionProfilePath + this.connectionProfileFilename;
	}
	
    public String getCoordinatorProfilePath() {
		return this.connectionProfilePath + this.coordinatorProfileFilename;
	}
	
    public String getEntityName() {
		return this.entityName;
	}

	public String getNetworkChannelName() {
		return this.networkChannelName;
	}

	public String getChaincodeName() {
		return this.chaincodeName;
	}

	public void setConnectionProfileFilename(String connectionProfileFilename) {
		this.connectionProfileFilename = connectionProfileFilename;
	}

	public String getCaPemFilename() {
		return caPemFilename;
	}

	public void setCaPemFilename(String caPemFilename) {
		this.caPemFilename = caPemFilename;
	}

	public String getCaClientUrl() {
		return caClientUrl;
	}

	public void setCaClientUrl(String caClientUrl) {
		this.caClientUrl = caClientUrl;
	}
}
