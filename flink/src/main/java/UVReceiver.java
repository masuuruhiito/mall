import com.weikun.api.common.GsonUtil;
import com.weikun.api.model.UMSLog;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumerBase;
import org.apache.flink.streaming.connectors.redis.RedisSink;
import org.apache.flink.streaming.connectors.redis.common.config.FlinkJedisPoolConfig;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisCommand;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisCommandDescription;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * @author weikun
 * @date 2020-02-06 17:46:48
 * 新冠状病毒肆虐 黑龙江已经200人了 草
 * @desc 由9号用户，在2020-02-06时间操作了地址【/subject/listAll】1次！
 * 【/subject/listAll】，共操作了6次！
 */
public class UVReceiver {
    public static void main(String[] args) throws Exception {
        String topic = "malluv";
        String host = "master";
        int port = 9092;
        int database_id = 0;//可以和主要的模块的redis缓存数据库分开存放

        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.enableCheckpointing(TimeUnit.MINUTES.toMillis(1));
        env.setParallelism(5);

        CheckpointConfig checkpointConf = env.getCheckpointConfig();
        //证最后的数据处理的结果和数据摄入时没有数据的丢失与重复
        checkpointConf.setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);
        //表示一旦Flink处理程序被cancel后，会保留Checkpoint数据，以便根据实际需要恢复到指定的Checkpoint处理。上面代码配置了执行Checkpointing的时间间隔为1分钟。
        checkpointConf.enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, host + ":" + port);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "app-uv-stat");

        FlinkKafkaConsumerBase<String> kafkaConsumer = new FlinkKafkaConsumer<>(
                topic, new SimpleStringSchema(), props)
                .setStartFromGroupOffsets();//从topic中指定的group上次消费的位置开始消费，所以必须配置group.id参数

        FlinkJedisPoolConfig conf = new FlinkJedisPoolConfig
                .Builder()
                .setDatabase(database_id)
                .setHost(host).build();
        env.addSource(kafkaConsumer)
                //.map(string -> GsonUtil.fromJson(string, UserVisitWebEvent.class))  // 反序列化 JSON

                .map(string -> {
                    StringBuilder sb = new StringBuilder(1000);
                    try {

                        List<String> list = Arrays.asList(string.split(","));
//{id:"54",createBy:"system",createTime:"2020-02-05 18:05:15.0",delFlag:"0",
// updateBy:"system",updateTime:"2020-02-05",costTime:"23",ip:"127.0.0.1",ipInfo:"",
// name:"品牌列表",
// requestParam:"{""pageNum"":[""1""]&&""pageSize"":[""100""]}",requestType:"GET",requestUrl:"/brand/list",
// userid:"3",logType:"5"}
                        sb.append("{");
                        Field[] fs = UMSLog.class.getDeclaredFields();
                        for (int i = 0; i < list.size(); i++) {
                            sb.append(fs[i].getName() + ":" + list.get(i) + ",");
                        }
                        sb.deleteCharAt(sb.lastIndexOf(","));//去掉最后一个
                        sb.append("}");

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //System.out.println(sb.toString());
                    UMSLog u = null;
                    try {
                        u = GsonUtil.fromJson(sb.toString(), UMSLog.class);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    return u;//json转化成对象
                })  // 反序列化 JSON

                .keyBy("updateTime", "logType") // 按照 日期和页面 进行 keyBy
                .map(new RichMapFunction<UMSLog, Tuple2<String, Long>>() {
                    // 存储当前 key 对应的 userId 集合
                    private MapState<String, Boolean> userIdState;
                    // 存储当前 key 对应的 UV 值
                    private ValueState<Long> uvState;

                    @Override
                    public Tuple2<String, Long> map(UMSLog userVisitWebEvent) throws Exception {
                        // 初始化 uvState
                        //System.out.println("---------------");
                        if (null == uvState.value()) {
                            uvState.update(0L);
                        }
                        // userIdState 中不包含当前访问的 userId，说明该用户今天还未访问过该页面
                        // 则将该 userId put 到 userIdState 中，并把 UV 值 +1
                        if (!userIdState.contains(userVisitWebEvent.getUserid().toString())) {
                            userIdState.put(userVisitWebEvent.getUserid().toString(), null);
                            uvState.update(uvState.value() + 1);
                        }
                        // 生成 Redis key，格式为 日期_pageId，如: 20191026_0
                        String redisKey = userVisitWebEvent.getUpdateTime() + "_" + userVisitWebEvent.getLogType();
                        System.out.println(redisKey + "   :::   " + uvState.value());
                        System.out.println("由" + userVisitWebEvent.getUserid() + "号用户，在" + userVisitWebEvent.getUpdateTime() +
                                "时间操作了地址【" + userVisitWebEvent.getRequestUrl() + "】1次！" + "；数据库的序号是：" + userVisitWebEvent.getId());
                        System.out.println("【" + userVisitWebEvent.getRequestUrl() + "】，共操作了" + uvState.value() + "次！");

                        return Tuple2.of(redisKey, uvState.value());
                    }

                    @Override
                    public void open(Configuration parameters) throws Exception {
                        super.open(parameters);
                        // 从状态中恢复 userIdState
                        userIdState = getRuntimeContext().getMapState(
                                new MapStateDescriptor<>("userIdState",
                                        TypeInformation.of(new TypeHint<String>() {
                                        }),
                                        TypeInformation.of(new TypeHint<Boolean>() {
                                        })));
                        // 从状态中恢复 uvState
                        uvState = getRuntimeContext().getState(
                                new ValueStateDescriptor<>("uvState",
                                        TypeInformation.of(new TypeHint<Long>() {
                                        })));
                    }
                })
                .addSink(new RedisSink<>(conf, new RedisSetSinkMapper()));//存入到redis下

        env.execute("Redis Set UV Stat");
    }

    // 数据与 Redis key 的映射关系，并指定将数据 set 到 Redis
    public static class RedisSetSinkMapper implements RedisMapper<Tuple2<String, Long>> {
        @Override
        public RedisCommandDescription getCommandDescription() {
            // 这里必须是 set 操作，通过 MapState 来维护用户集合，
            // 输出到 Redis 仅仅是为了展示结果供其他系统查询统计结果
            return new RedisCommandDescription(RedisCommand.SET);
        }

        @Override
        public String getKeyFromData(Tuple2<String, Long> data) {
            return data.f0;
        }

        @Override
        public String getValueFromData(Tuple2<String, Long> data) {
            return data.f1.toString();
        }
    }
}