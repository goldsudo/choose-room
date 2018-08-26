import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;

import java.util.ArrayList;
import java.util.List;

public class ChooseThread extends Thread {
    private JedisPool pool;

    private Student student;

    public ChooseThread() {
    }

    public ChooseThread(JedisPool pool, Student student) {
        this.pool = pool;
        this.student = student;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    @Override
    public void run() {
        //为了公平，都先sleep 100ms
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Jedis jedis = pool.getResource();
        String current = student.getName() + "进行选房-------";
        System.out.println(current + "目标床位为 " + student.getTargetBed());
        try {
            //0.判断所选床位是否已被抢走
            String result = jedis.get(student.getTargetBed());
            if (!("".equals(result))) {
                System.out.println(current + "目标床位：" + student.getTargetBed() + " 已被 " + result + "抢走");
                return;
            }
            //1.开启pipeline
            Pipeline pipeline = jedis.pipelined();
            //2.watch床位key
            pipeline.watch(student.getTargetBed());
            //3.开启multi事务
            pipeline.multi();
            //4.床位key加锁
            String lock = "CHOOSING_" + student.getTargetBed();
            pipeline.setnx(lock, student.getName());
            pipeline.expire(lock, 3);
            //5.提交事务，判断是否获取到锁
            pipeline.exec();
            /**
             * 获取pipeline所有执行结果：
             * 结果一共由5个元素组成：1)watch--OK,2)mulit--OK,3)setnx--QUEUED,4)expire--QUEUED,5)[1,1]
             * 其中第5个返回的数组为setnx与expire的返回值，1代表成功，0代表失败
             */
            List<Object> resultList = pipeline.syncAndReturnAll();
            //取第5个结果的第1个元素，即为setnx的结果
            List<Long> r = (ArrayList<Long>) resultList.get(4);
            //获取到锁
            if (r.get(0) == 1) {
                System.out.println(current + "获取到床位锁");
                //6.再次判断床位是否已被抢走
                String bed = jedis.get(student.getTargetBed());
                if (!("".equals(bed))) {
                    System.out.println(current + "目标床位：" + student.getTargetBed() + " 已被 " + bed + "抢走");
                    //释放锁
                    jedis.del(lock);
                    return;
                }
                //7.将床位key对应的val修改为当前学生姓名
                jedis.set(student.getTargetBed(), student.getName());
                System.out.println(current + "选房成功");
                //8.删除床位key锁
                jedis.del(lock);
            }
            //没获取到锁
            else {
                System.out.println(current + "没有获取到床位锁，该床位已有学生正在选择");
            }
        } finally {
            jedis.close();
        }
    }
}
