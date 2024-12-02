package org.zlt.utils;

import org.zlt.bean.User;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FormatResult {
    public static void extractInfo(String ocrResult, User user) {
        List<String> processedLines = processString(ocrResult);
        // System.out.println(processedLines);
        for (int i = 0; i < processedLines.size(); i++) {
            if (i == 0) {
                String name = extractName(processedLines.get(i));
                if (name != null) {
                    user.setName(name);
                }
            } else if (i == 1) {
                String nationality = extractNationality(processedLines.get(i));
                if (nationality != null) {
                    user.setNational(nationality);
                }
            } else if (i == 3) {
                String address = extractAddress(processedLines.get(i));
                if (address != null) {
                    user.setAddress(address);
                }
            } else if (i == 4) {
                String idCard = extractIdCard(processedLines.get(i));
                if (idCard != null) {
                    user.setIdCard(idCard);
                }
            }
        }
        // 根据身份证设置性别
        setSexFromIdCard(user);
        // 根据身份证设置生日
        setBirthDateFromIdCard(user);
        if (user.getSex() == null) {
            String sex = extractSex(processedLines.get(1));
            user.setSex(sex);
        }
        if (user.getBirth() == null) {
            String birth = extractBirth(processedLines.get(2));
            user.setBirth(birth);
        }
    }


    // 处理字符串
    public static List<String> processString(String ocrResult) {
        if (ocrResult == null || ocrResult.trim().isEmpty()) {
            return null;
        }

        ocrResult = ocrResult.trim(); // 去除首尾空白
        // 使用换行符分割字符串
        String[] lines = ocrResult.split("\\r?\\n");

        // 创建一个动态列表用于存储非空行
        List<String> filteredLines = new ArrayList<>();

        // 遍历分割后的结果，过滤掉空行并去除符号
        for (String line : lines) {
            line = line.replaceAll("[^\\w\\u4e00-\\u9fa5]", ""); // 去除所有符号
            line = line.replaceAll("_", ""); // 去除所有符号

            if (!line.trim().isEmpty()) { // 过滤空行
                filteredLines.add(line.trim()); // 将非空行添加到列表
            }
        }

        // 合并下标 3 和 4 的内容（如果存在）
        if (filteredLines.size() > 5) {
            String mergedLine = filteredLines.get(3) + filteredLines.get(4); // 合并内容
            filteredLines.set(3, mergedLine); // 更新第 3 项
            filteredLines.remove(4); // 移除第 4 项
        }

        // 将处理后的内容按行拼接回字符串
        return filteredLines;
    }

    // 提取姓名
    public static String extractName(String input) {
        // 文件路径
        String surnamePath = "D:\\Develop\\Java\\JavaProject\\OCR_Final\\src\\main\\resources\\surname\\surname.txt";

        // 读取姓氏文件
        Set<String> surnames = readSurnames(surnamePath);
        if (surnames.isEmpty()) {
            System.err.println("姓氏文件为空或未找到有效姓氏！");
            return null;
        }

        // 使用正则表达式匹配所有的中文字符
        String nameRegex = "[\\u4e00-\\u9fa5]{2,}";
        Pattern pattern = Pattern.compile(nameRegex);
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            // 获取匹配到的姓名
            String fullName = matcher.group();

            // 检查第一个字是否是姓氏，直到找到一个有效姓氏为止
            while (fullName.length() > 1) {
                String firstChar = fullName.substring(0, 1);
                if (surnames.contains(firstChar)) {
                    return fullName; // 找到有效的姓氏，返回姓名
                } else {
                    fullName = fullName.substring(1); // 删除第一个字，继续查找
                }
            }
        }

        return input; // 如果没有找到有效的姓名
    }

    // 提取性别
    public static String extractSex(String input) {
        String sexRegex = "男|女";
        Pattern pattern = Pattern.compile(sexRegex);
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            return matcher.group();
        } else {
            return input;
        }

    }

    // 提取生日
    public static String extractBirth(String input) {
        // 定义正则表达式，匹配多种日期格式
        String regex = "\\b(\\d{4})[\\s年./-]*(\\d{1,2})[\\s月./-]*(\\d{2,3})\\b";

        // 编译正则表达式
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        // 查找符合格式的日期
        StringBuilder results = new StringBuilder();
        while (matcher.find()) {
            int year = Integer.parseInt(matcher.group(1));
            int month = Integer.parseInt(matcher.group(2));
            int day = Integer.parseInt(matcher.group(3));

            // 校正月份和日期
            if (month < 1 || month > 12) continue; // 无效月份跳过
            if (day > 31) day = day / 10; // 如果日期超过两位数，修正为两位
            if (day < 1 || day > 31) continue; // 日期无效，跳过

            // 格式化为标准日期（确保日期部分是两位）
            String formattedDate = String.format("%04d年%02d月%02d日", year, month, day);
            results.append(formattedDate).append("\n");
        }

        // 如果没有找到符合的日期，返回提示信息
        return results.length() > 0 ? results.toString().trim() : "No valid birth date found";
    }
    public static String extractNationality(String input) {
        // 定义正则表达式，匹配民族
        String nationalityRegex = "汉|蒙古|回|藏|维吾尔|苗|彝|壮|布依|朝鲜|满|侗|瑶|白|土家|哈尼|哈萨克|傣|黎|傈僳|佤|畲|高山|拉祜|水|东乡|纳西|景颇|柯尔克孜|土|达斡尔|仫佬|羌|布朗|撒拉|毛南|仡佬|锦西|乌孜别克|保安|裕固|俄罗斯|鄂温克|德昂|赫哲";
        Pattern pattern = Pattern.compile(nationalityRegex);
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            return matcher.group();
        } else {
            return input;
        }
    }

    public static String extractIdCard(String input) {
        // 定义正则表达式，匹配身份证号码
        String idCardRegex = "\\b\\d{6}(19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[0-9Xx]\\b";
        Pattern pattern = Pattern.compile(idCardRegex);
        Matcher matcher = pattern.matcher(input);

        // 如果找到匹配结果，返回第一个匹配到的身份证号
        if (matcher.find()) {
            return matcher.group();
        }

        // 如果未匹配到，返回空字符串
        return input;
    }

    public static String extractAddress(String input) {
        // 定义正则表达式，匹配省份及其后面的内容
        String addressRegex = ".*?(河北|山西|辽宁|吉林|黑龙江|江苏|浙江|安徽|福建|江西|山东|河南|湖北|湖南|广东|海南|四川|贵州|云南|陕西|甘肃|青海|台湾|内蒙古|广西|西藏|宁夏|新疆|北京市|上海市|天津市|重庆市|香港特别行政区|澳门特别行政区)(.*)";
        Pattern pattern = Pattern.compile(addressRegex);
        Matcher matcher = pattern.matcher(input);
        // 如果匹配成功，返回完整匹配结果
        if (matcher.find()) {
            return matcher.group(1) + matcher.group(2); // 返回省份名称及后续内容
        } else {
            return input;
        }
    }


    // 设置性别
    public static void setSexFromIdCard(User user) {
        if (user.getIdCard() != null && user.getIdCard().length() == 18) {
            // 获取身份证第17位（性别位）
            char genderChar = user.getIdCard().charAt(16);  // 第17位为索引16
            // 判断性别
            if (Character.isDigit(genderChar)) {
                int genderDigit = Character.getNumericValue(genderChar);  // 将字符转换为数字
                if (genderDigit % 2 == 0) {
                    user.setSex("女");  // 如果是偶数，设置性别为女性
                } else {
                    user.setSex("男");    // 如果是奇数，设置性别为男性
                }
            }
        }
    }

    // 根据身份证号设置出生日期
    public static void setBirthDateFromIdCard(User user) {
        String birthDate = null;
        if (user.getIdCard() != null && user.getIdCard().length() == 18) {
            // 提取身份证的出生日期部分 (第7到14位，格式为yyyyMMdd)
            String birthDateStr = user.getIdCard().substring(6, 14);

            try {
                // 解析为日期对象
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                Date birthDateObj = sdf.parse(birthDateStr);

                // 格式化为 "yyyy年MM月dd日" 格式
                SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy年MM月dd日");
                birthDate = outputFormat.format(birthDateObj);
            } catch (Exception e) {
                birthDate = "1970年01月01日";  // 处理日期解析错误
            }
        }
        user.setBirth(birthDate);
    }

    // 从文件中读取姓氏并处理为正则表达式
    private static Set<String> readSurnames(String filePath) {
        Set<String> surnames = new HashSet<>();
        try {
            // 读取文件内容
            String content = new String(Files.readAllBytes(Paths.get(filePath)), "UTF-8");
            // 根据逗号分割姓氏
            String[] surnameArray = content.split(",");
            for (String surname : surnameArray) {
                surname = surname.trim(); // 去掉可能的多余空格
                if (!surname.isEmpty()) {
                    surnames.add(surname); // 将非空的姓氏添加到集合
                }
            }
        } catch (IOException e) {
            System.err.println("读取姓氏文件失败：" + e.getMessage());
        }
        return surnames;
    }

    public static void main(String[] args) {
        String testInput = """
            1992 年8月186
            """;

        System.out.println(extractBirth(testInput));
    }

}
