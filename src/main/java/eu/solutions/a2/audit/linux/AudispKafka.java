package eu.solutions.a2.audit.linux;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.solutions.a2.audit.DaemonThreadFactory;
import eu.solutions.a2.audit.utils.ExceptionUtils;

public class AudispKafka {

	private static final Logger LOGGER = LoggerFactory.getLogger(AudispKafka.class);
	private static final Properties props = new Properties();
	/**  Kafka producer */
	private static Producer<String, String> producer;
	/** Variable for constructing Kafka key */
	private static long kafka_counter = 0;
	/** Properties for building Kafka producer */
	private static Properties kafkaProps;
	/** Kafka topic */
	private static String kafkaTopic = null;
	/** Default number of worker threads */
	private static final int WORKER_THREAD_COUNT = 16;
	/** Maximum number of worker threads */
	private static final int WORKER_THREAD_MAX = 150;
	/** Number of async workers for data transfer */
	private static int workerThreadCount = WORKER_THREAD_COUNT;

	private static ScheduledExecutorService writeExecutor;

	public static void main(String[] argv) throws IOException {

		// Check for valid log4j configuration
		String log4jConfig = System.getProperty("a2.log4j.configuration");
		if (log4jConfig == null || "".equals(log4jConfig)) {
			System.err.println("JVM argument -Da2.log4j.configuration must set!");
			System.err.println("Exiting.");
			System.exit(1);
		}

		// Check that log4j configuration file exist
		Path path = Paths.get(log4jConfig);
		if (!Files.exists(path) || Files.isDirectory(path)) {
			System.err.println("JVM argument -Da2.log4j.configuration points to unknown file " + log4jConfig + "!");
			System.err.println("Exiting.");
			System.exit(1);
		}
		// Initialize log4j
		PropertyConfigurator.configure(log4jConfig);

		// read properties from /etc/audisp/audisp-kafka.conf
		String configPath = "/etc/audisp/audisp-kafka.conf";
		if (argv.length == 1) {
			configPath = argv[0];
		} else if (argv.length > 1) {
			LOGGER.error("Usage:\njava " + AudispKafka.class.getCanonicalName() + " [<full path to configuration file>]");
			LOGGER.error("Exiting.");
			System.exit(2);
		}
		try {
			props.load(new FileInputStream(configPath));
		} catch (IOException eoe) {
			LOGGER.error("Unable to open configuration file " + configPath);
			LOGGER.error(ExceptionUtils.getExceptionStackTrace(eoe));
			LOGGER.error("Exiting.");
			System.exit(3);
		}

		// Thread count
		String threadCountString = props.getProperty("a2.worker.count").trim();
		if (threadCountString != null && !"".equals(threadCountString)) {
			try {
				workerThreadCount = Integer.parseInt(threadCountString);
			} catch (Exception e) {
				LOGGER.error("a2.worker.count set to wrong value in " + configPath);
				LOGGER.error("Exiting.");
				System.exit(4);
			}
			if (workerThreadCount > WORKER_THREAD_MAX) {
				LOGGER.warn("a2.worker.count is maximum that allowed. Setting it to " + WORKER_THREAD_MAX);
				workerThreadCount = WORKER_THREAD_MAX;
			} else if (workerThreadCount < 0) {
				LOGGER.warn("a2.worker.count is negative. Setting it to " + WORKER_THREAD_COUNT);
				workerThreadCount = WORKER_THREAD_COUNT;
			}
		}

		// 
		kafkaTopic = props.getProperty("a2.kafka.topic");
		if (kafkaTopic == null || "".equals(kafkaTopic)) {
			LOGGER.error("kafka.topic parameter must set in configuration file " + configPath);
			LOGGER.error("Exiting.");
			System.exit(5);
		}

		String kafkaServers = props.getProperty("a2.kafka.servers");
		if (kafkaServers == null || "".equals(kafkaServers)) {
			LOGGER.error("kafka.servers parameter must set in configuration file " + configPath);
			LOGGER.error("Exiting.");
			System.exit(6);
		}

		String kafkaClientId = props.getProperty("a2.kafka.client.id");
		if (kafkaServers == null || "".equals(kafkaServers)) {
			LOGGER.error("a2.kafka.client.id parameter must set in configuration file " + configPath);
			LOGGER.error("Exiting.");
			System.exit(7);
		}

		//
		//TODO - hardcoding!!!
		//
		int kafkaMaxRequestSize = 11010048;
		int kafkaBatchSize = 256;

		kafkaProps = new Properties();
		kafkaProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServers);
		kafkaProps.put(ProducerConfig.CLIENT_ID_CONFIG, kafkaClientId);
		kafkaProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		kafkaProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		kafkaProps.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, kafkaMaxRequestSize);
		kafkaProps.put(ProducerConfig.BATCH_SIZE_CONFIG, kafkaBatchSize);
		/** Always try to use compression */
//		kafkaProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, 1);

		// Initialize connection to Kafka
		producer = new KafkaProducer<>(kafkaProps);

		ThreadFactory daemonFactory = new DaemonThreadFactory();
		writeExecutor = Executors.newScheduledThreadPool(workerThreadCount, daemonFactory);

		Scanner input = new Scanner(System.in);
		// Main loop
		while (input.hasNextLine()){
			writeExecutor.submit(new KafkaJob(
					System.currentTimeMillis() + "_" + kafka_counter,
					input.nextLine()));
			synchronized (AudispKafka.class) {
				kafka_counter++;
			}
		}
		input.close();
	}

	private static class KafkaJob implements Runnable {

		final String key;
		final String data;

		KafkaJob(final String key, final String data) {
			this.key = key;
			this.data = data;
		}

		@Override
		public void run() {
			final ProducerRecord<String, String> record =
					new ProducerRecord<>(kafkaTopic, key, data);
			producer.send(
					record,
					(metadata, exception) -> {
						if (metadata == null) {
							// Error occured
							LOGGER.error("Exception while sending\n\t" + data + "\n\tto Kafka!!!" );
							LOGGER.error(ExceptionUtils.getExceptionStackTrace(exception));
							//TODO
							//TODO Temporary storage????????
							//TODO
						} else {
							//TODO
							//TODO Count bytes transferred....
							//TODO
						}
				});
		}
	}

}
