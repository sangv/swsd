package textrank;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * The TextRankTest
 *
 * @author Sang Venkatraman
 */
public class TextRankTest {

	private final static Logger LOG =
			LoggerFactory.getLogger(TextRankTest.class.getName());

	private final String apacheCassandra = "The Apache Cassandra database is the right choice when you need scalability and high availability. Linear scalability and proven fault-tolerance on commodity hardware make it the perfect platform for mission-critical data.";

	@Test
	public void testTextRank() throws Exception {
		String resPath = "/Users/sang/Temp/swsd";
		final TextRank tr = new TextRank(resPath, "en");
		final String data_file = "/Users/sang/Temp/textrank/test/good.txt";
		String text = FileUtils.readFileToString(new File(data_file));
		tr.prepCall(apacheCassandra, true);
		//tr.prepCall(TestText.fakePlasticTrees(),true);
		final FutureTask<Collection<MetricVector>> task = new FutureTask<Collection<MetricVector>>(tr);
		Collection<MetricVector> answer = null;

		final Thread thread = new Thread(task);
		thread.run();

		try {
			//answer = task.get();  // run until complete
			answer = task.get(15000L, TimeUnit.MILLISECONDS); // timeout in N ms
		}
		catch (ExecutionException e) {
			LOG.error("exec exception", e);
		}
		catch (InterruptedException e) {
			LOG.error("interrupt", e);
		}
		catch (TimeoutException e) {
			LOG.error("timeout", e);

			// Unfortunately, with graph size > 700, even read-only
			// access to WordNet on disk will block and cause the
			// thread to be uninterruptable. None of the following
			// remedies work...

			//thread.interrupt();
			//task.cancel(true);
			return;
		}
		LOG.info("\n" + tr);

		System.out.println("DONE");
	}

	@Test
	public void testTextRank1() throws Exception {
		String resPath = "/Users/sang/Temp/swsd";
		final textrank1.TextRank tr = new textrank1.TextRank(resPath, "en");
		final String data_file = "/Users/sang/Temp/textrank/test/good.txt";
		String text = FileUtils.readFileToString(new File(data_file));
		tr.prepCall(apacheCassandra, true);
		final FutureTask<Collection<textrank1.MetricVector>> task = new FutureTask<Collection<textrank1.MetricVector>>(tr);
		Collection<textrank1.MetricVector> answer = null;

		final Thread thread = new Thread(task);
		thread.run();

		try {
			//answer = task.get();  // run until complete
			answer = task.get(15000L, TimeUnit.MILLISECONDS); // timeout in N ms
		}
		catch (ExecutionException e) {
			LOG.error("exec exception", e);
		}
		catch (InterruptedException e) {
			LOG.error("interrupt", e);
		}
		catch (TimeoutException e) {
			LOG.error("timeout", e);

			// Unfortunately, with graph size > 700, even read-only
			// access to WordNet on disk will block and cause the
			// thread to be uninterruptable. None of the following
			// remedies work...

			//thread.interrupt();
			//task.cancel(true);
			return;
		}
		LOG.info("\n" + tr);

		System.out.println("DONE");
	}

}
