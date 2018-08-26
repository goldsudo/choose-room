import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;

import java.util.ArrayList;
import java.util.List;

public class Test {
    public static JedisPool pool = new JedisPool("127.0.0.1", 6379);
    public static final String BED_PREFIX = "BED_";
    public static final String STUDENT_PREFIX = "STUDENT_";

    /**
     * 初始化4个床位
     */
    public static void initRoomBed() {
        Jedis jedis = pool.getResource();
        try {
            jedis.set(BED_PREFIX + "1", "");
            jedis.set(BED_PREFIX + "2", "");
            jedis.set(BED_PREFIX + "3", "");
            jedis.set(BED_PREFIX + "4", "");
        } finally {
            jedis.close();
        }
        System.out.println("床位初始化成功，一共4个空床位：BED_1 BED_2 BED_3 BED_4");
    }

    /**
     * 初始化8个学生
     */
    public static List<Student> initStudent() {
        List<Student> students = new ArrayList<Student>();
        for (int i = 1; i <= 8; ++i) {
            String name = STUDENT_PREFIX + i;
            String targetBed;
            if (i <= 4) {
                targetBed = BED_PREFIX + i;
            } else {
                targetBed = BED_PREFIX + (8 - i + 1);
            }
            Student student = new Student(name, targetBed);
            students.add(student);
        }
        return students;
    }

    /**
     * 打印最终选房结果
     */
    public static void chooseRoomBed(List<Student> students) throws InterruptedException {
        //创建每个学生的抢房线程
        List<ChooseThread> chooseThreads = new ArrayList<ChooseThread>();
        for (Student student : students) {
            ChooseThread chooseThread = new ChooseThread(pool, student);
            chooseThreads.add(chooseThread);
        }
        for (ChooseThread chooseThread : chooseThreads) {
            chooseThread.start();
        }

        for (ChooseThread chooseThread : chooseThreads) {
            chooseThread.join();
        }
        System.out.println("模拟选房执行完毕");
    }

    public static void printResult() {
        System.out.println("-----本次模拟选房结果-----");
        Jedis jedis = pool.getResource();
        try {
            String bed1 = BED_PREFIX + "1";
            System.out.println("床位：" + bed1 + " 入住学生：" + jedis.get(bed1));
            String bed2 = BED_PREFIX + "2";
            System.out.println("床位：" + bed2 + " 入住学生：" + jedis.get(bed2));
            String bed3 = BED_PREFIX + "3";
            System.out.println("床位：" + bed3 + " 入住学生：" + jedis.get(bed3));
            String bed4 = BED_PREFIX + "4";
            System.out.println("床位：" + bed4 + " 入住学生：" + jedis.get(bed4));
        } finally {
            jedis.close();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        initRoomBed();
        List<Student> students = initStudent();
        //8个学生抢4个床位（即一个宿舍）
        chooseRoomBed(students);
        printResult();
    }
}
