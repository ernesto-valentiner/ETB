package evidentia;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.Set;
import java.security.PrivateKey;

import org.hyperledger.fabric.gateway.Wallet;
import org.hyperledger.fabric.gateway.Wallet.Identity;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric.sdk.security.CryptoSuiteFactory;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.RegistrationRequest;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric_ca.sdk.EnrollmentRequest;


public class Administrator implements User{
	String pemFilename;
	String adminName;
	String adminPassword;
	String caClientUrl;
	Identity identity;
	String msp;
	String affiliation;
	final String walletPath = "wallet";
	String pemFilePath = "src/main/resources/";
	
	public Administrator(String adminName, String adminPassword, String pemFilename, String caClientUrl, String msp, String affiliation) {
		System.setProperty("org.hyperledger.fabric.sdk.service_discovery.as_localhost", "true");
		this.adminName = adminName;
		this.pemFilename = pemFilename;
		this.caClientUrl = caClientUrl;
		this.adminPassword = adminPassword;
		this.msp = msp;
		this.affiliation = affiliation;
	}

	public void enroll() throws Exception {
		
		Properties props = new Properties();
		props.put("pemFile",this.pemFilePath + this.pemFilename);
		props.put("allowAllHostNames", "true");
		HFCAClient caClient = HFCAClient.createNewInstance(this.caClientUrl, props);
		CryptoSuite cryptoSuite = CryptoSuiteFactory.getDefault().getCryptoSuite();
		caClient.setCryptoSuite(cryptoSuite);
		
		// Create a wallet for managing identities
		Wallet wallet = Wallet.createFileSystemWallet(Paths.get(walletPath));

		// Check to see if we've already enrolled the admin user.
		boolean adminExists = wallet.exists(this.adminName);
        if (adminExists) {
            System.out.println("An identity for the admin user" + this.adminName + " already exists in the wallet");
            return;
        }
        
        // Enroll the admin user, and import the new identity into the wallet.
        final EnrollmentRequest enrollmentRequestTLS = new EnrollmentRequest();
        enrollmentRequestTLS.addHost("localhost");
        enrollmentRequestTLS.setProfile("tls");
        Enrollment enrollment = caClient.enroll("admin", this.adminPassword, enrollmentRequestTLS);
        Identity user = Identity.createIdentity(this.msp, enrollment.getCert(), enrollment.getKey());
        wallet.put(this.adminName, user);
		System.out.println("Successfully enrolled user " + this.adminName + " and imported it into the wallet");
	}
	
	public void registerUser(String entityName) throws Exception {
		
		// Create a CA client for interacting with the CA.
		Properties props = new Properties();
		props.put("pemFile",this.pemFilePath + this.pemFilename);
		props.put("allowAllHostNames", "true");
		HFCAClient caClient = HFCAClient.createNewInstance(this.caClientUrl, props);
		CryptoSuite cryptoSuite = CryptoSuiteFactory.getDefault().getCryptoSuite();
		caClient.setCryptoSuite(cryptoSuite);

		// Create a wallet for managing identities
		Wallet wallet = Wallet.createFileSystemWallet(Paths.get(walletPath));

		// Check to see if we've already enrolled the user.
		boolean userExists = wallet.exists(entityName);
		if (userExists) {
			System.out.println("An identity for the user \"" + entityName + "\" already exists in the wallet");
			return;
		}

		userExists = wallet.exists(adminName);
		if (!userExists) {
			System.out.println(adminName + " needs to be enrolled and added to the wallet first");
			return;
		}
		
		this.identity = wallet.get(adminName);
		
		// Register the user, enroll the user, and import the new identity into the wallet.
		RegistrationRequest registrationRequest = new RegistrationRequest(entityName);
		registrationRequest.setAffiliation(this.affiliation);
		registrationRequest.setEnrollmentID(entityName);
		String enrollmentSecret = caClient.register(registrationRequest, this);
		EnrollmentRequest enrollmentRequest = new EnrollmentRequest();
		Enrollment enrollment = caClient.enroll(entityName, enrollmentSecret, enrollmentRequest);
		Identity user = Identity.createIdentity(this.msp, enrollment.getCert(), enrollment.getKey());
		wallet.put(entityName, user);
		System.out.println("Successfully enrolled user \"" + entityName + "\" and imported it into the wallet");
	}

	@Override
	public String getName() {
		return this.adminName;
	}

	@Override
	public Set<String> getRoles() {
		return null;
	}

	@Override
	public String getAccount() {
		return null;
	}

	@Override
	public String getAffiliation() {
		return this.affiliation;
	}

	@Override
	public Enrollment getEnrollment() {
		return new Enrollment() {

			@Override
			public PrivateKey getKey() {
				return identity.getPrivateKey();
			}

			@Override
			public String getCert() {
				return identity.getCertificate();
			}
		};
	}

	@Override
	public String getMspId() {
		return this.msp;
	}

	public static void main(String[] args) {
		try (InputStream input = new FileInputStream("src/main/resources/networkConfig.properties")) {

			Properties prop = new Properties();
			prop.load(input);

			String entityName = prop.getProperty("entityName");
			String pemFilename = prop.getProperty("pemFilename");
			String pemFilenameCoord = prop.getProperty("pemFilenameCoord");
			String caClientUrl = prop.getProperty("caClientUrl");
			String caClientCoordUrl = prop.getProperty("caClientCoordUrl");
			String mspName = prop.getProperty("MSPName");
			String mspCoordName = prop.getProperty("MSPCoordName");

			Administrator admin = new Administrator("admin_" + entityName, "adminpw", pemFilename, caClientUrl, mspName, entityName + ".department1");
			admin.enroll();
			admin.registerUser(entityName);

			Administrator admin_coord = new Administrator("admin_coord", "adminpw", pemFilenameCoord, caClientCoordUrl, mspCoordName, "org1.department1");
			admin_coord.enroll();
			admin_coord.registerUser("coord");

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
