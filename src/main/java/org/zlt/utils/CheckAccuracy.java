package org.zlt.utils;

import org.zlt.bean.User;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CheckAccuracy {
    public static List<User> generateUser() {
        User user1 = new User();
        user1.setName("王飞");
        user1.setSex("女");
        user1.setNational("汉");
        user1.setAddress("辽宁省大连市甘井子区");
        user1.setIdCard("522530199208180048");
        user1.setBirth("1992年08月18日");
        User user2 = new User();
        user2.setName("苏海峰");
        user2.setSex("男");
        user2.setNational("汉");
        user2.setBirth("1988年09月15日");
        user2.setAddress("安徽省天长市千秋新村三巷10号");
        user2.setIdCard("341181198809150011");
        User user3 = new User();
        user3.setName("梁爱梅");
        user3.setSex("女");
        user3.setNational("汉");
        user3.setBirth("1974年11月19日");
        user3.setAddress("山东省德州市德城区乐园街106号院331号");
        user3.setIdCard("372423197411194924");
        User user4 = new User();
        user4.setName("陈朋涛");
        user4.setSex("男");
        user4.setNational("汉");
        user4.setAddress("山东省滕州市龙阳镇");
        user4.setIdCard("532101198906010015");
        user4.setBirth("1989年06月01日");
        User user5 = new User();
        user5.setName("代用名");
        user5.setSex("女");
        user5.setNational("汉");
        user5.setBirth("1989年08月13日");
        user5.setAddress("湖南省长沙市开福区巡道街幸福小区居民组");
        user5.setIdCard("430512198908131367");
        User user6 = new User();
        user6.setName("张路通");
        user6.setNational("汉");
        user6.setSex("男");
        user6.setIdCard("371427200207114914");
        user6.setAddress("山东省夏津县双庙镇后柳村513号3");
        user6.setBirth("2002年07月11日");
        User user7 = new User();
        user7.setName("张路通");
        user7.setNational("汉");
        user7.setSex("男");
        user7.setIdCard("371427200207114914");
        user7.setAddress("山东省夏津县双庙镇后柳村513号3");
        user7.setBirth("2002年07月11日");
        User user8 = new User();
        user8.setName("张路通");
        user8.setNational("汉");
        user8.setSex("男");
        user8.setIdCard("371427200207114914");
        user8.setAddress("山东省夏津县双庙镇后柳村513号3");
        user8.setBirth("2002年07月11日");
        User user9 = new User();
        user9.setName("张路通");
        user9.setNational("汉");
        user9.setSex("男");
        user9.setIdCard("371427200207114914");
        user9.setAddress("山东省夏津县双庙镇后柳村513号3");
        user9.setBirth("2002年07月11日");
        User user10 = new User();
        user10.setName("张路通");
        user10.setNational("汉");
        user10.setSex("男");
        user10.setIdCard("371427200207114914");
        user10.setAddress("山东省夏津县双庙镇后柳村513号3");
        user10.setBirth("2002年07月11日");
        User user11 = new User();
        user11.setName("张路通");
        user11.setNational("汉");
        user11.setSex("男");
        user11.setIdCard("371427200207114914");
        user11.setAddress("山东省夏津县双庙镇后柳村513号3");
        user11.setBirth("2002年07月11日");
        User user12 = new User();
        user12.setName("张路通");
        user12.setNational("汉");
        user12.setSex("男");
        user12.setIdCard("371427200207114914");
        user12.setAddress("山东省夏津县双庙镇后柳村513号3");
        user12.setBirth("2002年07月11日");
        User user13 = new User();
        user13.setName("夏帅");
        user13.setNational("汉");
        user13.setSex("男");
        user13.setIdCard("320382199412250230");
        user13.setAddress("江苏省邳州市运河镇朝阳村588号");
        user13.setBirth("1994年12月25日");
        User user14 = new User();
        user14.setName("杨春吉");
        user14.setNational("汉");
        user14.setSex("男");
        user14.setIdCard("120110194610173034");
        user14.setAddress("天津市东丽区华明镇李明庄村六区花园街12条4号");
        user14.setBirth("1946年10月17日");
        return Arrays.asList(user1, user2, user3, user4, user5, user6, user7, user8, user9, user10, user11, user12, user13, user14);
    }

    public static double checkSingleAccuracy(User user, int num) {
        List<User> usersList = generateUser();
        User inputUser = usersList.get(num - 1);
        double totalAccuracy = 0.0; // 总相似度
        System.out.println("原来的用户信息：" + inputUser);

        int totalFields = 0;
        // 比较字段
        totalAccuracy += compareStrings(inputUser.getName(), user.getName());
        totalAccuracy += compareStrings(inputUser.getSex(), user.getSex());
        totalAccuracy += compareStrings(inputUser.getNational(), user.getNational());
        totalAccuracy += compareStrings(inputUser.getAddress(), user.getAddress());
        totalAccuracy += compareStrings(inputUser.getBirth(), user.getBirth());
        totalAccuracy += compareStrings(inputUser.getIdCard(), user.getIdCard());
        totalFields += 6;  // 每个用户有6个字段进行比较
        // 计算平均相似度（即准确率）
        return totalAccuracy / totalFields;
    }

    // 计算准确率的方法
    public static double checkAllAccuracy(List<User> user, int totalNum) {
        List<User> usersList = generateUser();


        int totalFields = 0;   // 总字段数
        double totalAccuracy = 0.0; // 总相似度

        // 比较每个用户的数据
        for (int i = 0; i < totalNum; i++) {
            User inputUser = user.get(i);
            User referenceUser = usersList.get(i);

            // 比较字段
            totalAccuracy += compareStrings(inputUser.getName(), referenceUser.getName());
            totalAccuracy += compareStrings(inputUser.getSex(), referenceUser.getSex());
            totalAccuracy += compareStrings(inputUser.getNational(), referenceUser.getNational());
            totalAccuracy += compareStrings(inputUser.getAddress(), referenceUser.getAddress());
            totalAccuracy += compareStrings(inputUser.getBirth(), referenceUser.getBirth());
            totalAccuracy += compareStrings(inputUser.getIdCard(), referenceUser.getIdCard());

            totalFields += 6;  // 每个用户有6个字段进行比较
        }

        // 计算平均相似度（即准确率）
        return totalAccuracy / totalFields;
    }

    // 计算两个字符串的准确率（逐字符比较）
    public static double compareStrings(String str1, String str2) {
        // 处理空格和两端的空白字符
        str1 = str1.trim();
        str2 = str2.trim();

        // 计算两个字符串的最小长度
        int minLength = Math.min(str1.length(), str2.length());
        int matchCount = 0;

        // 逐个字符比较
        for (int i = 0; i < minLength; i++) {
            if (str1.charAt(i) == str2.charAt(i)) {
                matchCount++;
            }
        }

        // 计算准确率：匹配的字符数 / 最短字符串的长度
        double accuracy = (double) matchCount / minLength;

        // 还可以根据实际情况扩展，比如处理长度不一致的情况
        return accuracy;
    }

    // 将字符串转换为字符集
    private static Set<Character> stringToSet(String str) {
        Set<Character> set = new HashSet<>();
        for (char c : str.toCharArray()) {
            set.add(c);  // 将每个字符添加到集合中
        }
        return set;
    }

    public static void main(String[] args) {
        List<User> usersList = generateUser();

        for (User user : usersList) {
            System.out.println(user);
        }
    }
}


