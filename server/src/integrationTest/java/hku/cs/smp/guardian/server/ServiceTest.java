package hku.cs.smp.guardian.server;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ServiceTest {
    @Autowired
    CallsService callsService;

    @Test
    public void test() {
        Map<String, Map<String, Integer>> result = new HashMap<>();
        List<String> phones;
        Random random = new Random();
        int phoneNum = 10, tagNum = 5, iteration = 1000;
        LongStream ls = random.longs(phoneNum, 1L << 30, 1L << 40);
        phones = ls.boxed().map(String::valueOf).collect(Collectors.toList());
        List<String> tags;
        ls = random.longs(tagNum, 1L << 30, 1L << 40);
        tags = ls.boxed().map(String::valueOf).collect(Collectors.toList());

        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            Thread t = new Thread(() -> {
                for (int j = 0; j < iteration; j++) {
                    String number = phones.get(random.nextInt(phoneNum));
                    String tag = tags.get(random.nextInt(tagNum));
                    callsService.tag(number, tag);
                    synchronized (result) {
                        Map<String, Integer> tagMap = result.get(number);
                        if (tagMap == null)
                            tagMap = new HashMap<>();
                        result.put(number, tagMap);
                        Integer times = tagMap.get(tag);
                        if (times == null)
                            times = 0;
                        times += 1;
                        tagMap.put(tag, times);
                    }
                }
            });
            threads.add(t);
            t.start();
        }

        threads.forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });


        phones.forEach(number -> {
            Map<String, Integer> db = callsService.inquiry(number);
            Map<String, Integer> mem = result.get(number);
            mem.keySet().forEach(key ->
                    Assert.isTrue(Objects.equals(db.get(key), mem.get(key)),
                            String.format("Answer for [number=%s,tag=%s]is not correct, [mem=%s, db=%s]",
                                    number, key, mem.get(key), db.get(key))));
        });

    }
}
