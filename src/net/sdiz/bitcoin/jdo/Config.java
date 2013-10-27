package net.sdiz.bitcoin.jdo;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.datanucleus.util.Base64;

import com.google.appengine.api.datastore.Key;

@PersistenceCapable(detachable = "true")
public class Config {
	private static final Logger log = Logger.getLogger(Config.class.getName());

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	protected Key key;
	/** RPC Server */
	@Persistent
	protected String jsonRpcServer = "http://pool.btc38.com:9332";
	/** RPC User Name */
	@Persistent
	protected String username = "1DLT5vT4kNhwDzmL3fMGqcdjQ4H8nnZkBm";
	/** RPC Password */
	@Persistent
	protected String password = "btc38";
	/** HTTP Authentication Header */
	protected transient String authorization;
	/** Number of seconds for each work */
	@Persistent
	protected int targetRoundTime = 6;
	/**
	 * Number of hash per work (initial guess, will adjust according to
	 * <code>targetRoundTime</code>
	 */
	@Persistent
	protected int scanCount = 0xffff;
	/** Number of second in each job. Never excess 10 minutes (GAE Limit) */
	@Persistent
	protected int targetTotalTime = 540;

	protected Config() {
	}

	public String getJsonRpcServer() {
		return jsonRpcServer;
	}

	public void setJsonRpcServer(String jsonRpcServer) {
		this.jsonRpcServer = jsonRpcServer;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		authorization = null;
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		authorization = null;
		this.password = password;
	}

	public String getAuth() {
		if (authorization == null) {
			String auth = getUsername() + ":" + getPassword();
			authorization = "Basic " + Base64.encodeString(auth);
		}
		return authorization;
	}

	public int getTargetRoundTime() {
		return targetRoundTime;
	}

	public void setTargetRoundTime(int roundTime) {
		this.targetRoundTime = roundTime;
	}

	public int getScanCount() {
		return scanCount;
	}

	public void setScanCount(int scanCount) {
		this.scanCount = scanCount;
	}

	public int getTargetTotalTime() {
		return targetTotalTime;
	}

	public void setTargetTotalTime(int targetTotalTime) {
		this.targetTotalTime = targetTotalTime;
	}

	@SuppressWarnings("unchecked")
	public static Config getConfig() throws IOException {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			String query = "select from " + Config.class.getName();
			List<Config> configs = (List<Config>) pm.newQuery(query).execute();
			if (configs.size() > 0) {
				Config config = configs.get(0);
				return pm.detachCopy(config);
			}
		} catch (Exception e) {
			throw new IOException("Error loading config", e);
		} finally {
			try {
				pm.close();
			} catch (Exception e) {
				log.severe(e.toString());
			}
		}

		// fallback - no config ?
		return new Config();
	}

	public void save() throws IOException {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			// save this
			pm.makePersistent(this);
		} finally {
			try {
				pm.close();
			} catch (Exception e) {
				log.severe(e.toString());
			}
		}
	}
}

