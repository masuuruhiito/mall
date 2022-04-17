import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.Test;
import scala.tools.jline_embedded.internal.TestAccessible;

import java.util.Arrays;
import java.util.Properties;

/**
 * 创建人：SHI
 * 创建时间：2021/12/2
 * 描述你的类：
 */
public class KafkaUtils {

    private static   String brokerList = "master:9092";
    @Test
    public void deleteTopic(){
//有个FileNotFouind日志异常 没关系 可以正常删除topic
        String []topic = {"malluv"};

        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG,brokerList);
        props.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG,30000);

        AdminClient client = AdminClient.create(props);

        client.deleteTopics(Arrays.asList(topic));
        client.close();
    }

    @Test
    public  void addTopic(){
//有个FileNotFouind日志异常 没关系 可以正常建topic
        AdminClient adminClient;
        Properties properties = new Properties();
        properties.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG,brokerList);
        properties.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG,30000);
        adminClient = AdminClient.create(properties);
        NewTopic newTopic = new NewTopic("malluv",5,(short)1);
        adminClient.createTopics(Arrays.asList(newTopic));
        adminClient.close();
        System.out.println("创建主题成功：");//有个FileNotFouind日志异常 没关系 可以正常建topic


    }
}
