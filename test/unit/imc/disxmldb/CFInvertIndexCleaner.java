/**
*@author:xiafan xiafan68@gmail.com
*@version: 2011-10-28 0.1
*/
package imc.disxmldb;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.antlr.grammar.v3.ANTLRv3Parser.finallyClause_return;
import org.apache.cassandra.SchemaLoader;
import org.apache.cassandra.config.ConfigurationException;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.config.KSMetaData;
import org.apache.cassandra.config.Schema;
import org.apache.cassandra.db.commitlog.CommitLog;
import org.apache.cassandra.io.util.FileUtils;
import org.apache.cassandra.locator.AbstractReplicationStrategy;
import org.apache.cassandra.locator.SimpleStrategy;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CFInvertIndexCleaner {
	 private static Logger logger = LoggerFactory.getLogger(SchemaLoader.class);
	 public static final String keyspace = "testDb";
	 
	 @BeforeClass
	    public static void cleanupAndLeaveDirs() throws IOException
	    {
	        mkdirs();
	        cleanup();
	        mkdirs();
	        CommitLog.instance.resetUnsafe(); // cleanup screws w/ CommitLog, this brings it back to safe state
	    }

	    public static void cleanup() throws IOException
	    {
	        // clean up commitlog
	        String[] directoryNames = { DatabaseDescriptor.getCommitLogLocation(), };
	        for (String dirName : directoryNames)
	        {
	            File dir = new File(dirName);
	            if (!dir.exists())
	                throw new RuntimeException("No such directory: " + dir.getAbsolutePath());
	            FileUtils.deleteRecursive(dir);
	        }

	        // clean up data directory which are stored as data directory/table/data files
	        for (String dirName : DatabaseDescriptor.getAllDataFileLocations())
	        {
	            File dir = new File(dirName);
	            if (!dir.exists())
	                throw new RuntimeException("No such directory: " + dir.getAbsolutePath());
	            FileUtils.deleteRecursive(dir);
	        }
	    }

	    public static void mkdirs()
	    {
	        try
	        {
	            DatabaseDescriptor.createAllDirectories();
	        }
	        catch (IOException e)
	        {
	            throw new RuntimeException(e);
	        }
	    }
	    
    @BeforeClass
    public static void loadSchema()
    {
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler()
        {
            public void uncaughtException(Thread t, Throwable e)
            {
                logger.error("Fatal exception in thread " + t, e);
            }
        });

        Schema.instance.load(schemaDefinition(), Schema.instance.getVersion());
    }
    
	private static Collection<KSMetaData> schemaDefinition() {
		List<KSMetaData> schema = new ArrayList<KSMetaData>();
		 Class<? extends AbstractReplicationStrategy> simple = SimpleStrategy.class;
		 Map<String, String> opts_rf1 = KSMetaData.optsWithRF(1);
		 // Keyspace 1
        schema.add(new KSMetaData(keyspace, simple, opts_rf1));
        
		return schema;
	}
}
