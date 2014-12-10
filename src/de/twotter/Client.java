package de.twotter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.astyanax.AstyanaxContext;
import com.netflix.astyanax.Keyspace;
import com.netflix.astyanax.connectionpool.NodeDiscoveryType;
import com.netflix.astyanax.connectionpool.OperationResult;
import com.netflix.astyanax.connectionpool.exceptions.ConnectionException;
import com.netflix.astyanax.connectionpool.impl.ConnectionPoolConfigurationImpl;
import com.netflix.astyanax.connectionpool.impl.CountingConnectionPoolMonitor;
import com.netflix.astyanax.impl.AstyanaxConfigurationImpl;
import com.netflix.astyanax.model.ColumnFamily;
import com.netflix.astyanax.model.ColumnList;
import com.netflix.astyanax.model.CqlResult;
import com.netflix.astyanax.model.Row;
import com.netflix.astyanax.serializers.IntegerSerializer;
import com.netflix.astyanax.serializers.StringSerializer;
import com.netflix.astyanax.thrift.ThriftFamilyFactory;

/**
 * Exercise for programming a Cassandra-based application (simple Twitter
 * clone). Partly based on examples by Netflix Astyanax contributors.
 * 
 * Preparation: Log into CQL shell via $ cqlsh Create a keyspace: cqlsh> CREATE
 * KEYSPACE twotter WITH replication = {'class': 'SimpleStrategy',
 * 'replication_factor': 1};
 * 
 * @author markus klems
 * 
 */
public class Client {

	public static final String CLUSTER_NAME = "Test Cluster";
	public static final String KEYSPACE_NAME = "twotter";
	// userline stores tweets of a user
	private static final String USERLINE_TABLE_NAME = "userline";
	private ColumnFamily<Integer, String> userlineTable;

	// tweet columns
	public static final String COL_NAME_USERID = "user_id";
	public static final String COL_NAME_TWEET_TIMESTAMP = "tweet_timestamp";
	public static final String COL_NAME_USER_NAME = "user_name";
	public static final String COL_NAME_TWEET_TXT = "tweet_txt";

	private static final Logger logger = LoggerFactory.getLogger(Client.class);
	private AstyanaxContext<Keyspace> context;
	private Keyspace keyspace;

	private static final String INSERT_TWEET_INTO_USERLINE_STATEMENT = String
			.format("INSERT INTO %s (%s, %s, %s, %s) VALUES (?, ?, ?, ?);",
					USERLINE_TABLE_NAME, COL_NAME_USERID,
					COL_NAME_TWEET_TIMESTAMP, COL_NAME_USER_NAME,
					COL_NAME_TWEET_TXT);
	private static final String CREATE_USERLINE_STATEMENT = String
			.format("CREATE TABLE %s (%s uuid, %s timestamp, %s varchar, %s varchar, PRIMARY KEY (%s, %s))",
					USERLINE_TABLE_NAME, COL_NAME_USERID,
					COL_NAME_TWEET_TIMESTAMP, COL_NAME_USER_NAME,
					COL_NAME_TWEET_TXT, COL_NAME_USERID,
					COL_NAME_TWEET_TIMESTAMP);

	public void init() {
		logger.debug("init()");
		context = new AstyanaxContext.Builder()
				.forCluster(CLUSTER_NAME)
				.forKeyspace(KEYSPACE_NAME)
				.withAstyanaxConfiguration(
						new AstyanaxConfigurationImpl()
								.setDiscoveryType(NodeDiscoveryType.RING_DESCRIBE))
				.withConnectionPoolConfiguration(
						new ConnectionPoolConfigurationImpl("MyConnectionPool")
								.setPort(9160).setMaxConnsPerHost(1)
								.setSeeds("127.0.0.1:9160"))
				.withAstyanaxConfiguration(
						new AstyanaxConfigurationImpl().setCqlVersion("3.0.0")
								.setTargetCassandraVersion("2.1"))
				.withConnectionPoolMonitor(new CountingConnectionPoolMonitor())
				.buildKeyspace(ThriftFamilyFactory.getInstance());
		context.start();
		keyspace = context.getEntity();
		userlineTable = ColumnFamily.newColumnFamily(USERLINE_TABLE_NAME,
				IntegerSerializer.get(), StringSerializer.get());
	}

	public void tweet(String username, String tweetText) {
		// TODO insert a tweet
		logger.debug("insert tweet ok");
	}

	public OperationResult<CqlResult<Integer, String>> read(String username,
			ColumnFamily<Integer, String> table) {
		logger.debug("read()");
		// TODO read the tweets
		return null;
	}

	private void readUserline(String username) {
		OperationResult<CqlResult<Integer, String>> result = read(username,
				userlineTable);
		for (Row<Integer, String> row : result.getResult().getRows()) {
			logger.debug("row: " + row.getKey() + "," + row);
			ColumnList<String> cols = row.getColumns();
			logger.debug("userline");
			logger.debug("- user id: "
					+ cols.getUUIDValue(COL_NAME_USERID, null));
			logger.debug("- tweet timestamp: "
					+ cols.getLongValue(COL_NAME_TWEET_TIMESTAMP, null));
			String username2 = cols.getStringValue(COL_NAME_USER_NAME, null);
			String tweet = cols.getStringValue(COL_NAME_TWEET_TXT, null);
			System.out.println(username2 + " : " + tweet);
		}
	}

	public static void main(String[] args) {
		logger.debug("main");
		Client c = new Client();
		c.init();
		try {
			BufferedReader commandline = new java.io.BufferedReader(
					new InputStreamReader(System.in));
			System.out.print("> ");
			while (true) {
				String s = commandline.readLine();
				if (s.equalsIgnoreCase("exit")) {
					System.exit(0);
				} else if (s.equalsIgnoreCase("create-tables")) {
					c.createTables();
					System.out.println("Created tables.");
					System.out.print("> ");
				} else if (s.equalsIgnoreCase("tweet")) {
					System.out
					.println("Send a tweet like this: tweet <username> : <tweet_message> (for example: Eric : Respect mah authorotah");
					String tweetInput = commandline.readLine();
					System.out.print("> ");
					while (true) {
						StringTokenizer st = new StringTokenizer(tweetInput,
								":");
						String user = st.nextToken();
						String msg = st.nextToken();
						c.tweet(user, msg);
						break;
					}
				} else if (s.equalsIgnoreCase("read")) {
					System.out.println("Enter user name:");
					String username = commandline.readLine();
					System.out.print("> ");
					while (!username.equalsIgnoreCase("exit")) {
						c.readUserline(username);
						break;
					}					
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Helpers
	private void createTables() {
		createTable(userlineTable, CREATE_USERLINE_STATEMENT);
	}

	private void createTable(ColumnFamily<Integer, String> table,
			String statement) {
		logger.debug("CQL: " + statement);
		try {
			@SuppressWarnings("unused")
			OperationResult<CqlResult<Integer, String>> result = keyspace
					.prepareQuery(table).withCql(statement).execute();
		} catch (ConnectionException e) {
			logger.error("failed to create Table", e);
			throw new RuntimeException("failed to create Table", e);
		}
	}

}
