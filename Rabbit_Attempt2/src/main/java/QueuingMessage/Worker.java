package QueuingMessage;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class Worker {

    private final static String QUEUE_NAME = "task_queue";

    public static void main(String[] args) throws Exception{
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        final Connection connection = factory.newConnection();
        final Channel channel = connection.createChannel();

        boolean durable = true;
        channel.queueDeclare(QUEUE_NAME, durable, false, false, null);

        System.out.println(" [*] Waiting for messages. To exit: ctrl+c");


        int prefetchCount = 1;
        channel.basicQos(prefetchCount);

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");

            System.out.println(" [x] Received '" + message + "'");

            try {
                doWork(message);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                System.out.println(" [x] Done");
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            }

        };


        channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumertag -> { });
    }

    private static void doWork(String task) throws InterruptedException{
        for (char ch: task.toCharArray()) {
            if (ch == '.') {
                try {
                    Thread.sleep(1000);
                }catch(InterruptedException _ignored){
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
