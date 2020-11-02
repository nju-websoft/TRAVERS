package Structures;

import java.io.*;

public class fileModel {

    // 文件复制
    public static boolean copyFile(String source, String copy) throws Exception {
        source = source.replace("\\", "/");
        copy = copy.replace("\\", "/");

        File source_file = new File(source);
        File copy_file = new File(copy);

        // BufferedStream缓冲字节流

        if (!source_file.exists()) {
            throw new IOException("文件复制失败：源文件（" + source_file + "） 不存在");
        }
        if (copy_file.isDirectory()) {
            throw new IOException("文件复制失败：复制路径（" + copy_file + "） 错误");
        }
        File parent = copy_file.getParentFile();
        // 创建复制路径
        if (!parent.exists()) {
            parent.mkdirs();
        }
        // 创建复制文件
        if (!copy_file.exists()) {
            copy_file.createNewFile();
        }

        FileInputStream fis = new FileInputStream(source_file);
        FileOutputStream fos = new FileOutputStream(copy_file);

        BufferedInputStream bis = new BufferedInputStream(fis);
        BufferedOutputStream bos = new BufferedOutputStream(fos);

        byte[] KB = new byte[1024];
        int index;
        while ((index = bis.read(KB)) != -1) {
            bos.write(KB, 0, index);
        }

        bos.close();
        bis.close();
        fos.close();
        fis.close();

        if (!copy_file.exists()) {
            return false;
        } else if (source_file.length() != copy_file.length()) {
            return false;
        } else {
            return true;
        }

    }

    // 文件重命名
    public static boolean renameFile(String url, String new_name) throws Exception {
        String old_url = url;
        old_url = old_url.replace("\\", "/");
        File old_file = new File(old_url);
        if (!old_file.exists()) {
            throw new IOException("文件重命名失败，文件（"+old_file+"）不存在");
        }
        System.out.println(old_file.exists());

        String old_name = old_file.getName();
        // 获得父路径
        String parent = old_file.getParent();
        // 重命名
        String new_url = parent + "/" + new_name;
        File new_file = new File(new_url);
        old_file.renameTo(new_file);

        System.out.println("原文件：" + old_file.getName());
        System.out.println("新文件：" + new_file.getName());
        new_name = new_file.getName();
        old_name = old_file.getName();
        if (new_name.equals(old_name)) {
            return false;
        } else {
            return true;
        }

    }

    // 文件删除
    public static boolean deleteFile(String url) throws Exception {
        url = url.replace("\\", "/");
        File file = new File(url);

        if (file.isFile()) {
            if (file.exists()) {
                file.delete();
            }
        }else{
            throw new IOException("文件删除失败：（"+file+"）错误");
        }
        if (file.exists()) {
            return false;
        } else {
            return true;
        }
    }

    // 创建文件夹
    public static boolean createPath(String url) throws Exception {
        url = url.replace("\\", "/");
        File folder = new File(url);
        if(!folder.isDirectory()){
            throw new IOException("创建文件夹失败：（"+folder+"）不是文件夹路径");
        }

        if (!folder.isFile()) {
            if (!folder.exists()) {
                folder.mkdirs();
            }
        }
        // 检测是否创建成功
        if (folder.isDirectory() && folder.exists()) {
            return true;
        } else {
            return false;
        }

    }

    public static void CreateFolder(String add)
    {
        File dir = new File(add);
        if (dir.exists()) return; else dir.mkdirs();
    }

}
