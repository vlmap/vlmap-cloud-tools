package com.github.vlmap.spring.loadbalancer.core.cli;

import org.apache.commons.cli.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GrayAttachCommandLineParser {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    public static final String HEADER = "header";
    public static final String URI = "uri";
    public static final String COOKIE = "cookie";
    public static final String PARAM = "param";
    public static final String JSON_PATH = "json-path";
    public static final String METHOD = "method";
    public static final String STRICT = "strict";

    public static final String VALUE = "value";

    Options options = new Options();

    public GrayAttachCommandLineParser() {

        initOptions();
    }


    public void initOptions() {
        Option.Builder builder = Option.builder("H").longOpt(HEADER).argName("name:value").hasArg(true).desc("匹配HEADER信息。示例：\n--header=referer:https://www.baidu.com\n--header \"referer:https://www.baidu.com\"");
        options.addOption(builder.build());


        builder = Option.builder("U").longOpt(URI).hasArg(true).desc("URI匹配，支持ANT格式URI. 示例：--uri=/test/** ");
        options.addOption(builder.build());

        builder = Option.builder("C").longOpt(COOKIE).argName("name:value").hasArg(true).desc("COOKIE匹配. 示例：--cookie=a:1\n--cookie b:2");
        options.addOption(builder.build());


        builder = Option.builder("P").longOpt(PARAM).argName("name:value").hasArg(true).desc("参数匹配. 示例：--param=a:1\n--param \"b:2\"");
        options.addOption(builder.build());

        builder = Option.builder("J").longOpt(JSON_PATH).argName("name:value").hasArg(true).desc("JsonPath匹配. 示例：--json-path=$.data.el[0]:abc\n--json-path \"$.data.el[0]:abc\"");
        options.addOption(builder.build());

        builder = Option.builder("M").longOpt(METHOD).hasArg(true).desc("Method匹配. 示例：--method=POST \n--method  GET ");
        options.addOption(builder.build());

        builder = Option.builder("S").longOpt(STRICT).hasArg(false).desc("启用严格模式.默认不启用.\n启用时请求必须匹配全部条件，不启用只需匹配其中一个条件  ");
        options.addOption(builder.build());

        builder = Option.builder("V").longOpt(VALUE).required().hasArg(true).desc("条件匹配配返回的值");
        options.addOption(builder.build());
    }

    public GaryAttachParamater parser(String command) {


        CommandLineTokenizer tokenizer = new CommandLineTokenizer(command);
        List<String> args = new ArrayList<>();
        while (tokenizer.hasMoreTokens()) {
            args.add(tokenizer.nextToken());
        }
        CommandLine commandLine = null;
        try {
            commandLine = new DefaultParser().parse(options, args.toArray(new String[0]));

        } catch (Exception e) {
            logger.error("parse commandline error:" + e.getMessage());
            logger.info(getHelpString());
        }

        GaryAttachParamater result = new GaryAttachParamater();
        String[] values = null;

        values = commandLine.getOptionValues(HEADER);
        Map<String, String> map = new LinkedHashMap<>();
        result.setHeaders(map);
        addAll(map, values, ":");


        map = new LinkedMultiValueMap();
        result.setCookies(map);
        values = commandLine.getOptionValues(COOKIE);
        addAll(map, values, ":");


        map = new LinkedMultiValueMap();
        result.setParams(map);
        values = commandLine.getOptionValues(PARAM);
        addAll(map, values, ":");


        map = new LinkedMultiValueMap();
        result.setJsonpath(map);
        values = commandLine.getOptionValues(JSON_PATH);
        addAll(map, values, ":");

        List<String> list = new ArrayList<>();
        result.setUris(list);
        values = commandLine.getOptionValues(URI);
        if (ArrayUtils.isNotEmpty(values)) {
            for (String value : values) {
                if (StringUtils.isNotBlank(value)) {
                    list.add(value);
                }
            }
        }

        list = new ArrayList<>();
        result.setUris(list);
        values = commandLine.getOptionValues(METHOD);
        if (ArrayUtils.isNotEmpty(values)) {
            for (String value : values) {
                HttpMethod method = HttpMethod.resolve(value);
                if (method != null) {
                    list.add(method.name());
                }

            }
        }
        String value = commandLine.getOptionValue(VALUE);
        if (StringUtils.isBlank(value)) {
            return null;
        }
        result.setValue(value);


        result.setStrict(commandLine.hasOption(STRICT));

        return result;

    }

    /**
     * get string of help usage
     *
     * @return help string
     */
    public String getHelpString() {

        HelpFormatter helpFormatter = new HelpFormatter();

        String help = null;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PrintWriter printWriter = new PrintWriter(byteArrayOutputStream);
        try {
            helpFormatter.printHelp(printWriter, HelpFormatter.DEFAULT_WIDTH, "格式说明", null,
                    options, HelpFormatter.DEFAULT_LEFT_PAD, HelpFormatter.DEFAULT_DESC_PAD, null);
            printWriter.flush();
            help = new String(byteArrayOutputStream.toByteArray());
        } finally {

            IOUtils.closeQuietly(printWriter);
            IOUtils.closeQuietly(byteArrayOutputStream);

        }
        return help;


    }

    protected void addAll(Map<String, String> map, String[] values, String separator) {

        if (ArrayUtils.isNotEmpty(values)) {
            for (String value : values) {
                String key = StringUtils.substringBefore(value, separator);
                String val = StringUtils.substringBefore(value, separator);
                map.put(key, val);
            }
        }
    }

}
