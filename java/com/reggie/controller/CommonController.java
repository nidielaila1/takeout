package com.reggie.controller;

import com.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * 实现对于文件的上传与下载
 */
@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController {

    @Value("${reggie.path}")
    private String basePath;

    /**
     * 实现对于文件的上传操作
     * spring框架在spring-web包中对文件上传进行了封装，大大简化了服务端代码，
     * 我们只需要在Controller的方法中声明一个MultipartFile类型的参数即可接收上传的文件
     *
     * @param file 必须定义为file否则会报错 在前端中的file中的name标签就是file
     * @return
     */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) {
//        file是一个临时文件，需要对于当前这个文件进行一个转存，如果没有进行转存，那么就会在本次请求结束之后消失
        log.info("上传文件为：{}", file.toString());

//        对于文件名进行配置，取出原始文件名
//        但是对于这种方式会产生覆盖的问题
        String originalFilename = file.getOriginalFilename();

//        截取后缀名  根据最后的.来截取  例：abc.jpg
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));

//        使用UUID对于新文件名进行随机生成，防止文件名重复导致覆盖
        String filename = UUID.randomUUID().toString() + suffix;//但是只是生成文件名，没有后缀名，需要再原始文件中动态截取

//        创建一个目录（防止路径中有一个不存在的目录导致系统崩溃）
        File dir = new File(basePath);
//        判断路径中的目录是否存在
        if (!dir.exists()) {
//            不存在就创建出来
            dir.mkdirs();
        }
        try {
            //        将文件转存到一个指定的位置
//        如果不想写的耦合那么就需要在application.yml中进行进行上传路径配置
            file.transferTo(new File(basePath + filename));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return R.success(filename);
    }

    /**
     * 文件下载
     *
     * @param name
     * @param response 文件输出流需要通过response获取
     */
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response) {
        try {
//            通过输入流，读取文件内容
            FileInputStream fileInputStream = new FileInputStream(new File(basePath + name));
            //        通过输出流写回浏览器,在浏览器展示图片
            ServletOutputStream outputStream = response.getOutputStream();

//            设置文件的格式
            response.setContentType("image/jpeg");

//            通过输入流来读数据，通过输出流来写数据
//            相互配合
            int len = 0;
            byte[] bytes = new byte[1024];
            while ((len = fileInputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, len);
//                写完了进行刷新
                outputStream.flush();
            }

//            用完了关闭资源
            fileInputStream.close();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
